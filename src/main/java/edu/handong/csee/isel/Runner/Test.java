package edu.handong.csee.isel.Runner;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.diff.DiffAlgorithm;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.diff.EditList;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.util.io.DisabledOutputStream;

import edu.handong.csee.isel.utils.BIChange;
import edu.handong.csee.isel.utils.Blamer;
import edu.handong.csee.isel.utils.CSVmaker;
import edu.handong.csee.isel.utils.Utils;

public class Test {

	final static String[] headers = { "BIShal1", "BIpath", "fixPath", "fixShal1", "numLineBI", "numLinePrefix",
			"content" };

	public static void main(String[] args) throws IOException, NoHeadException, GitAPIException {

		Git git = Git.open(new File("/Users/imseongbin/java/Temp"));
		Repository repo = git.getRepository();
		File result = new File("/Users/imseongbin/Desktop/temp.csv");
		if (result.exists())
			result.delete();
		CSVmaker writer = new CSVmaker(result, headers);
		boolean hasChangedLineRange = true;
		final int min = 0;
		final int max = 5;

		Iterable<RevCommit> logs = git.log().call();

		for (RevCommit commit : logs) {
			try {
				RevCommit parent = commit.getParent(0);

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
							System.out.println(bi);
						}
					}

				} catch (Exception e) {
					e.printStackTrace();
				}

			} catch (ArrayIndexOutOfBoundsException e) {
				break;
			}
		}
	}

}
