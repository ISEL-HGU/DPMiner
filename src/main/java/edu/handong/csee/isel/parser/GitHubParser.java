package edu.handong.csee.isel.parser;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.diff.DiffAlgorithm;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.diff.EditList;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.util.io.DisabledOutputStream;

import edu.handong.csee.isel.bic.BIChange;
import edu.handong.csee.isel.bic.Blamer;
import edu.handong.csee.isel.patch.Patch;
import edu.handong.csee.isel.runner.Input;
import edu.handong.csee.isel.utils.CSVmaker;
import edu.handong.csee.isel.utils.Utils;

public class GitHubParser extends Parser {
	final HashSet<String> keyHashes; // bug commit id
	final static String[] Patchheaders = { "Project", "fix-commit", "fix-shortMessage", "fix-date", "fix-author",
			"patch" };
	final static String[] BICheaders = { "BIShal1", "BIpath", "fixPath", "fixShal1", "numLineBI", "numLinePrefix",
			"content" };

	public GitHubParser(Input input, HashSet<String> keyHashes) {
		super(input);
		for (String key : keyHashes)
			System.out.println(key);
		this.keyHashes = keyHashes;
	}

	public void parse() throws InvalidRemoteException, TransportException, GitAPIException, IOException {
		super.parse();
		CSVmaker writer;

		Repository repo = input.git.getRepository();
		int max = input.conditionMax;
		int min = input.conditionMin;
		String projectName = input.projectName;

		int total = 0;
		int cnt = 0;
		for (RevCommit commit : walk)
			total ++;
		
		if (input.isBI) {
			writer = new CSVmaker(new File(input.outPath + "BIC_" + input.projectName + ".csv"), BICheaders);
			
			for (RevCommit commit : walk) {
				try {
					
					if(100*(cnt-1)/total < 10) {
						System.out.println("\b\b\b");
					} else if (100*(cnt-1)/total < 100){
						System.out.println("\b\b\b\b");
					}
					
					System.out.println(GREEN_BACKGROUND+RED+cnt*100/total+"%"+RESET);
					
					RevCommit parent = commit.getParent(0);

					if (!keyHashes.contains(commit.getId().name()))
						continue;

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
						List<BIChange> bis = new ArrayList<BIChange>();
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
								numTotalLine += (edit.getEndA() - edit.getBeginA())
										+ (edit.getEndB() - edit.getBeginB());

								for (int i = edit.getBeginA(); i < edit.getEndA(); i++)
									removedLineList.add(i);
							}
							// condition of lineChangeCount
							if (numTotalLine > max || numTotalLine < min) {
								valid = false; // when out of line range
								break;
							}
							// blame old file
							Blamer blamer = new Blamer(repo, parent.getId(), oldPath);
							for (int numLine : removedLineList) {
								Blamer.OneLine blamed = blamer.blameOneLine(numLine);
								BIChange bi = new BIChange(blamed.commit.getId().name(), blamed.path, newPath,
										commit.getId().name(), blamed.num + 1, numLine + 1, sourceLines[numLine]);
								bis.add(bi);
							}
						}
						if (valid) {
							for (BIChange bi : bis) {
								writer.write(bi);
							}
						}

					} catch (Exception e) {
						e.printStackTrace();
					}

				} catch (ArrayIndexOutOfBoundsException e) {
					break; // last parent commit does not exist
				}
				cnt ++;
			}

		} else {
			writer = new CSVmaker(new File(input.outPath + "BPatch_" + input.projectName + ".csv"), Patchheaders);
			for (RevCommit commit : walk) {
				try {
					
					if(100*(cnt-1)/total < 10) {
						System.out.println("\b\b\b");
					} else if (100*(cnt-1)/total < 100){
						System.out.println("\b\b\b\b");
					}
					
					System.out.println(GREEN_BACKGROUND+RED+cnt*100/total+"%"+RESET);
					
					RevCommit parent = commit.getParent(0);

					if (!keyHashes.contains(commit.getId().name()))
						continue;

					final List<DiffEntry> diffs = git.diff()
							.setOldTree(Utils.prepareTreeParser(repo, parent.getId().name()))
							.setNewTree(Utils.prepareTreeParser(repo, commit.getId().name())).call();
					int patchSize = 0;
					ArrayList<String> patches = new ArrayList<String>();

					for (DiffEntry diff : diffs) {
						String patch = getPatch(diff, repo);
						if (patch == null)
							continue;
						patches.add(patch);
						int numLines = Utils.parseNumOfDiffLine(patch);
						patchSize += numLines;
						if (patchSize > max) // for speed
							break;
					}

					if (patchSize < min || patchSize > max)
						continue;

					for (String patch : patches) {

						Patch data = new Patch(projectName, commit.name(), commit.getShortMessage(),
								commit.getAuthorIdent().getWhen(), commit.getAuthorIdent().getName(), patch);
						writer.write(data);
					}

				} catch (ArrayIndexOutOfBoundsException e) {
					break;
				}
				cnt++;
			}
		}

	}

	public static String getPatch(DiffEntry diff, Repository repository) throws IOException {

		String patch = null;
		if (!diff.getNewPath().endsWith(".java")) // only .java format
			return null;

		ByteArrayOutputStream output = new ByteArrayOutputStream();
		try (DiffFormatter formatter = new DiffFormatter(output)) {
			formatter.setRepository(repository);
			formatter.format(diff);
		}
		output.flush();
		output.close();
		patch = output.toString("UTF-8");

		return patch;
	}
}
