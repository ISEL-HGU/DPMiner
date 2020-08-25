package edu.handong.csee.isel.bic.collector;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jgit.api.BlameCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.blame.BlameResult;
import org.eclipse.jgit.diff.DiffAlgorithm;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.diff.EditList;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.errors.RevisionSyntaxException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.util.io.DisabledOutputStream;

import edu.handong.csee.isel.Utils;
import edu.handong.csee.isel.bic.BICCollector;
import edu.handong.csee.isel.data.CSVInfo;
import edu.handong.csee.isel.data.Input;
import edu.handong.csee.isel.data.csv.BICInfo;

public class CBICCollector implements BICCollector {  //도대체 왜 여기서 에러가 뜨는지가 모르겠다. 건든게 일도 없는데?

//	Input input;
	List<String> bfcList = null;

	Git git;
	Repository repo;

//	public CBICCollector(Input input) {
//		this.input = input;
//	}

	@Override
	public void setBFC(List<String> bfcList) {
		this.bfcList = bfcList;

	}

	@Override
	public List<CSVInfo> collectFrom(List<RevCommit> commitList) {

		try {
			git = Git.open(edu.handong.csee.isel.Main.getGitDirectory());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		repo = git.getRepository();
		
//		System.out.println("bfcList:");
//		System.out.println(bfcList);
		
		List<BICInfo> lstBIChanges = new ArrayList<BICInfo>();
		for (RevCommit commit : commitList) {

			if (commit.getParentCount() < 1) {
				System.err.println("WARNING: Parent commit does not exist: " + commit.name());
				continue;
			}
			
			if(!Utils.isBFC(commit, bfcList)) {
				continue;
			}
			

			RevCommit parent = commit.getParent(0);

			List<DiffEntry> diffs = Utils.diff(parent, commit, repo);
			// check the change size in a patch
			int numLinesChanges = 0; // deleted + added
			String id = commit.name() + "";
			for (DiffEntry diff : diffs) {
				String oldPath = diff.getOldPath();
				String newPath = diff.getNewPath();

				// ignore when no previous revision of a file, Test files, and non-java files.
				if (oldPath.equals("/dev/null") || newPath.indexOf("Test") >= 0 || !newPath.endsWith(".java"))
					continue;

				// get preFixSource and fixSource without comments
				String prevFileSource = Utils.removeComments(Utils.fetchBlob(repo, id + "~1", oldPath));
				String fileSource = Utils.removeComments(Utils.fetchBlob(repo, id, newPath));

				// get line indices that are related to BI lines.
				EditList editList = Utils.getEditListFromDiff(prevFileSource, fileSource);
				for (Edit edit : editList) {

					int beginA = edit.getBeginA();
					int endA = edit.getEndA();
					int beginB = edit.getBeginB();
					int endB = edit.getEndB();

					numLinesChanges += (endA - beginA) + (endB - beginB);
				}
			}

			// if minPathsize is defined, check the size and exit a loop if the changes are
			// bigger than minPatchSize
			// only consider min <= size <=max
			if (numLinesChanges < Input.minSize || numLinesChanges > Input.maxSize) {
				continue;
			}

			// actual loop to get BI Changes
			for (DiffEntry diff : diffs) {
				ArrayList<Integer> lstIdxOfDeletedLinesInPrevFixFile = new ArrayList<Integer>();
				ArrayList<Integer> lstIdxOfOnlyInsteredLinesInFixFile = new ArrayList<Integer>();
				String oldPath = diff.getOldPath();
				String newPath = diff.getNewPath();

				// ignore when no previous revision of a file, Test files, and non-java files.
				if (oldPath.equals("/dev/null") || newPath.indexOf("Test") >= 0 || !newPath.endsWith(".java"))
					continue;

				// get preFixSource and fixSource without comments
				String prevFileSource = Utils.removeComments(Utils.fetchBlob(repo, id + "~1", oldPath));
				String fileSource = Utils.removeComments(Utils.fetchBlob(repo, id, newPath));

				EditList editList = Utils.getEditListFromDiff(prevFileSource, fileSource);

				// get line indices that are related to BI lines.
				for (Edit edit : editList) {

					if (edit.getType() != Edit.Type.INSERT) {

						int beginA = edit.getBeginA();
						int endA = edit.getEndA();

						for (int i = beginA; i < endA; i++)
							lstIdxOfDeletedLinesInPrevFixFile.add(i);

					} else {
						int beginB = edit.getBeginB();
						int endB = edit.getEndB();

						for (int i = beginB; i < endB; i++)
							lstIdxOfOnlyInsteredLinesInFixFile.add(i);
					}
				}

				// get BI commit from lines in lstIdxOfOnlyInsteredLines
				lstBIChanges.addAll(getBIChangesFromBILineIndices(id, commit, newPath, oldPath,
						prevFileSource, lstIdxOfDeletedLinesInPrevFixFile));
//					if(!unTrackDeletedBIlines)
//						lstBIChanges.addAll(getBIChangesFromDeletedBILine(id,rev.getCommitTime(),mapDeletedLines,fileSource,lstIdxOfOnlyInsteredLinesInFixFile,oldPath,newPath));
			}
		}
		Collections.sort(lstBIChanges);

		ArrayList<CSVInfo> csvInfoList = new ArrayList<>();
		csvInfoList.addAll(lstBIChanges);

		return csvInfoList;
	}

	private ArrayList<BICInfo> getBIChangesFromBILineIndices(String fixSha1, RevCommit fixCommit, String path,
			String prevPath, String prevFileSource, ArrayList<Integer> lstIdxOfDeletedLinesInPrevFixFile) {

		ArrayList<BICInfo> biChanges = new ArrayList<BICInfo>();

		// do Blame
		BlameCommand blamer = new BlameCommand(repo);
		ObjectId commitID;
		try {
			commitID = repo.resolve(fixSha1 + "~1");
			blamer.setStartCommit(commitID);
			blamer.setFilePath(prevPath);
			BlameResult blame = blamer.setDiffAlgorithm(Utils.diffAlgorithm).setTextComparator(Utils.diffComparator)
					.setFollowFileRenames(true).call();

			ArrayList<Integer> arrIndicesInOriginalFileSource = lstIdxOfDeletedLinesInPrevFixFile; // getOriginalLineIndices(origPrvFileSource,prevFileSource,lstIdxOfDeletedLines);
			for (int lineIndex : arrIndicesInOriginalFileSource) {
				RevCommit commit = blame.getSourceCommit(lineIndex);

				String BISha1 = commit.name();
				String biPath = blame.getSourcePath(lineIndex);
				String FixSha1 = fixSha1;
				String BIDate = Utils.getStringDateTimeFromCommit(commit);
				String FixDate = Utils.getStringDateTimeFromCommit(fixCommit);
				int lineNum = blame.getSourceLine(lineIndex) + 1;
				int lineNumInPrevFixRev = lineIndex + 1;

				String[] splitLinesSrc = prevFileSource.split("\n");

				// split("\n") ignore last empty lines so lineIndex can be out-of-bound and
				// ignore empty line (this happens as comments are removed)
				if (splitLinesSrc.length <= lineIndex || splitLinesSrc[lineIndex].trim().equals(""))
					continue;

				BICInfo biChange = new BICInfo(BISha1, biPath, FixSha1, path, BIDate, FixDate, lineNum,
						lineNumInPrevFixRev, prevFileSource.split("\n")[lineIndex].trim(), true);
				biChanges.add(biChange);
			}

		} catch (RevisionSyntaxException | IOException | GitAPIException e) {
			e.printStackTrace();
		}

		return biChanges;
	}

}
