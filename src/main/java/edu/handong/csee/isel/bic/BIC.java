package edu.handong.csee.isel.bic;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.diff.DiffAlgorithm;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.diff.EditList;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.util.io.DisabledOutputStream;

import edu.handong.csee.isel.runner.Input;
import edu.handong.csee.isel.utils.Utils;

public class BIC {

	public static List<BIChange> collect(RevCommit parent, RevCommit commit, Input input) throws IOException {
		List<BIChange> bis = new ArrayList<BIChange>();
//		CSVmaker writer = new CSVmaker(new File(input.outPath + "BIC_" + input.projectName + ".csv"), BICheaders);

		int min = input.conditionMin;
		int max = input.conditionMax;
		Git git = input.git;
		Repository repo = git.getRepository();

		DiffFormatter df = new DiffFormatter(DisabledOutputStream.INSTANCE);
		df.setRepository(repo);
		df.setDiffAlgorithm(DiffAlgorithm.getAlgorithm(DiffAlgorithm.SupportedAlgorithm.MYERS));
		df.setDiffComparator(RawTextComparator.WS_IGNORE_ALL);
		df.setDetectRenames(true);

		List<DiffEntry> diffs;

		try {

			diffs = df.scan(parent.getTree(), commit.getTree());
			int numTotalLine = 0;
			for (DiffEntry diff : diffs) {

				String oldPath = diff.getOldPath();
				String newPath = diff.getNewPath();
				
				if(!newPath.endsWith(".java") || newPath.contains("test")) continue;
				
				List<Integer> removedLineList = new ArrayList<Integer>();
				String prevFileSource = Utils
						.removeComments(Utils.fetchBlob(repo, commit.getId().getName() + "~1", oldPath));
				String fileSource = Utils.removeComments(Utils.fetchBlob(repo, commit.getId().getName(), newPath));
				String[] sourceLines = prevFileSource.split("\n");

				EditList editList = Utils.getEditListFromDiff(prevFileSource, fileSource);

				for (Edit edit : editList) {
					numTotalLine += (edit.getEndA() - edit.getBeginA()) + (edit.getEndB() - edit.getBeginB());

					for (int i = edit.getBeginA(); i < edit.getEndA(); i++)
						removedLineList.add(i);
				}
				// condition of lineChangeCount
				if (numTotalLine > max || numTotalLine < min) {
					return null;
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
			return bis;

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
