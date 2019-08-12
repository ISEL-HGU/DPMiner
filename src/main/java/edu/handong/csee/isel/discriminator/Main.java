package edu.handong.csee.isel.discriminator;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.diff.DiffAlgorithm;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.diff.EditList;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.util.io.DisabledOutputStream;

import edu.handong.csee.isel.bic.BIChange;
import edu.handong.csee.isel.bic.Blamer;
import edu.handong.csee.isel.patch.parser.PatchParseType;
import edu.handong.csee.isel.patch.parser.githubparser.NoIssuePagesException;
import edu.handong.csee.isel.utils.CSVmaker;
import edu.handong.csee.isel.utils.Utils;

public class Main {
	final static String REMOTE_URI = "https://github.com/zxing/zxing.git";
	final static String projectName = "zxing";
	final static String outPath = "/Users/imseongbin/Desktop/";
	final static String[] headers = { "BICcommit","Date"};
	static PatchParseType type = PatchParseType.GitHub;
	static private boolean hasChangedLineRange = true;
	static private String reference; //null
	static private String label = "bug";
	final static String URL = "https://github.com/zxing/zxing";
	static int max = 5;
	static int min = 0;
	
	
	public static void main(String[] args) throws InvalidRemoteException, TransportException, GitAPIException, IOException {

		/* settings */
		HashSet<String> keywords = null;
		HashSet<String> keyHashes = null;

		Git git = Utils.gitClone(REMOTE_URI);
		Repository repo = git.getRepository();
		RevWalk walk = new RevWalk(repo);
		CSVmaker writer = new CSVmaker(new File(outPath + "BID_" + projectName + ".csv"), headers);

		for (Map.Entry<String, Ref> entry : repo.getAllRefs().entrySet()) {
			if (entry.getKey().contains("refs/heads/master")) { // only master
				Ref ref = entry.getValue();
				RevCommit commit = walk.parseCommit(ref.getObjectId());
				walk.markStart(commit);
			}
		}

		switch (type) {
		case Jira:
			keywords = Utils.parseReference(reference);
			break;
		case GitHub:
			try {
				keyHashes = Utils.parseGithubIssues(URL, label);
			} catch (NoIssuePagesException e) {
				type = PatchParseType.Keywords;
			}
			break;
		default:
			break;
		}

		Pattern keyPattern = Pattern.compile("\\[?(\\w+\\-\\d+)\\]?");
		Pattern bugMessagePattern = Pattern.compile("fix|bug|resolved", Pattern.CASE_INSENSITIVE);

		/* start */
		for (RevCommit commit : walk) {
			try {
				RevCommit parent = commit.getParent(0);

				switch (type) {
				case Jira:
					Matcher m = null;
					if (commit.getShortMessage().length() > 20)
						m = keyPattern.matcher(commit.getShortMessage().substring(0, 20)); // check if have keyword in
																							// Short message
					else
						m = keyPattern.matcher(commit.getShortMessage()); // check if have keyword in Short message
					if (!m.find())
						continue;
					String key = m.group(1);
					if (!keywords.contains(key))
						continue;
					break;

				case GitHub:
					if (!keyHashes.contains(commit.getId().name()))
						continue;
					break;

				case Keywords:
					m = bugMessagePattern.matcher(commit.getFullMessage());
					if (!m.find())
						continue;
					break;
				}

				DiffFormatter df = new DiffFormatter(DisabledOutputStream.INSTANCE);
				df.setRepository(repo);
				df.setDiffAlgorithm(DiffAlgorithm.getAlgorithm(DiffAlgorithm.SupportedAlgorithm.MYERS));
				df.setDiffComparator(RawTextComparator.WS_IGNORE_ALL);
				df.setDetectRenames(true);

				List<DiffEntry> diffs;
				

				try {

					diffs = df.scan(parent.getTree(), commit.getTree());
					boolean valid = true;
					int numTotalLine = 0;
//					List<BIChange> bis = new ArrayList<BIChange>();
					HashSet<BI_D> bis = new HashSet<BI_D>();
					for (DiffEntry diff : diffs) {

						String oldPath = diff.getOldPath();
						String newPath = diff.getNewPath();
						List<Integer> removedLineList = new ArrayList<Integer>();
						String prevFileSource = Utils
								.removeComments(Utils.fetchBlob(repo, commit.getId().getName() + "~1", oldPath));
						String fileSource = Utils
								.removeComments(Utils.fetchBlob(repo, commit.getId().getName(), newPath));
						String[] sourceLines = prevFileSource.split("\n");

						EditList editList = Utils.getEditListFromDiff(prevFileSource, fileSource);

						for (Edit edit : editList) {
							numTotalLine += (edit.getEndA() - edit.getBeginA()) + (edit.getEndB() - edit.getBeginB());

							for (int i = edit.getBeginA(); i < edit.getEndA(); i++)
								removedLineList.add(i);
						}
						// condition of lineChangeCount
						if (hasChangedLineRange && (numTotalLine > max || numTotalLine < min)) {
							valid = false; // when out of line range
							break;
						}
						// blame old file
						Blamer blamer = new Blamer(repo, parent.getId(), oldPath);
//						System.out.println(numTotalLine + ": " + hasChangedLineRange + " " + commit.getId().name());
						for (int numLine : removedLineList) {
							Blamer.OneLine blamed = blamer.blameOneLine(numLine);
							BI_D bi = new BI_D(blamed.commit.getId().name(), blamed.commit.getAuthorIdent().getWhen().toGMTString());
							bis.add(bi);
						}
					}
					if (valid) {
						for (BI_D bi : bis) {
							writer.write(bi);
							System.out.println(bi.id + "    " + bi.date);
						}
					}

				} catch (Exception e) {
					e.printStackTrace();
				}

			} catch (ArrayIndexOutOfBoundsException e) {
				break; // last parent commit does not exist
			}
		}
	}
}
