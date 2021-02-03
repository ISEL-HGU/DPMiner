package edu.handong.csee.isel.bic.szz.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.diff.DiffAlgorithm;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.EditList;
import org.eclipse.jgit.diff.RawText;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.errors.AmbiguousObjectException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.LargeObjectException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.errors.RevisionSyntaxException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathSuffixFilter;
import org.eclipse.jgit.util.io.DisabledOutputStream;

import edu.handong.csee.isel.bic.szz.model.PathRevision;
import edu.handong.csee.isel.bic.szz.model.RevsWithPath;

public class GitUtils {

	public static DiffAlgorithm diffAlgorithm = DiffAlgorithm.getAlgorithm(DiffAlgorithm.SupportedAlgorithm.MYERS);
	public static RawTextComparator diffComparator = RawTextComparator.WS_IGNORE_ALL;
	
	public static EditList getEditListFromDiff(String file1, String file2) {
		RawText rt1 = new RawText(file1.getBytes());
		RawText rt2 = new RawText(file2.getBytes());
		EditList diffList = new EditList();

		diffList.addAll(diffAlgorithm.diff(diffComparator, rt1, rt2));
		return diffList;
	}
	
	
	public static List<DiffEntry> diff(Repository repo, RevTree parentTree, RevTree childTree) throws IOException {
		List<DiffEntry> diffs;

		DiffFormatter df = new DiffFormatter(DisabledOutputStream.INSTANCE);
		df.setRepository(repo);
		df.setDiffAlgorithm(GitUtils.diffAlgorithm);
		df.setDiffComparator(GitUtils.diffComparator);
		df.setPathFilter(PathSuffixFilter.create(".java"));
		
		// parentTree => 부모 커밋을 가지고 있다. 
		diffs = df.scan(parentTree, childTree);

		df.close();

		return diffs;  // paths 를 리턴한다. 
	}

	public static List<PathRevision> configurePathRevisionList(Repository repo, List<RevCommit> commits)
			throws IOException {
		List<PathRevision> paths = new ArrayList<>();

		for (RevCommit commit : commits) {
			// Skip when there are no parents
			if (commit.getParentCount() == 0)
				continue;

			RevCommit parent = commit.getParent(0);
			if (parent == null)
				break;

			List<DiffEntry> diffs = GitUtils.diff(repo, parent.getTree(), commit.getTree());

			// get changed paths
			for (DiffEntry diff : diffs) {
				String path = diff.getNewPath(); 

				// contains only files which are java files and not test files
				if (path.endsWith(".java") && !path.contains("test")) {
					paths.add(new PathRevision(path, commit));
				}
			}
		}

		return paths;
	}

	public static RevsWithPath collectRevsWithSpecificPath(List<PathRevision> pathRevisions, List<String> targetPaths) {
		RevsWithPath revsWithPath = new RevsWithPath();

		for (PathRevision pr : pathRevisions) {
			String path = pr.getPath();

			// Skip when the path is not a target
			if (!targetPaths.contains(path))
				continue;

			if (revsWithPath.containsKey(path)) {
				List<RevCommit> lst = revsWithPath.get(path);
				lst.add(pr.getCommit());
				revsWithPath.replace(path, lst);
			} else {
				List<RevCommit> lst = new ArrayList<>();
				lst.add(pr.getCommit());
				revsWithPath.put(path, lst);
			}
		}

		return revsWithPath;
	}

	public static String fetchBlob(Repository repo, RevCommit commit, String path)
			throws LargeObjectException, MissingObjectException, IOException {

		// Makes it simpler to release the allocated resources in one go
		ObjectReader reader = repo.newObjectReader();

		// Get the revision's file tree
		RevTree tree = commit.getTree();
		// .. and narrow it down to the single file's path
		TreeWalk treewalk = TreeWalk.forPath(reader, path, tree);

		if (treewalk != null) {
			// use the blob id to read the file's data
			byte[] data = reader.open(treewalk.getObjectId(0)).getBytes();
			reader.close();
			return new String(data, "utf-8");
		} else {
			return "";
		}
	}

	public static String fetchBlob(Repository repo, String revSpec, String path)
			throws RevisionSyntaxException, AmbiguousObjectException, IncorrectObjectTypeException, IOException {

		// Resolve the revision specification
		final ObjectId id = repo.resolve(revSpec);

		// Makes it simpler to release the allocated resources in one go
		ObjectReader reader = repo.newObjectReader();

		// Get the commit object for that revision
		RevWalk walk = new RevWalk(reader);
		RevCommit commit = walk.parseCommit(id);
		walk.close();

		// Get the revision's file tree
		RevTree tree = commit.getTree();
		// .. and narrow it down to the single file's path
		TreeWalk treewalk = TreeWalk.forPath(reader, path, tree);

		if (treewalk != null) {
			// use the blob id to read the file's data
			byte[] data = reader.open(treewalk.getObjectId(0)).getBytes();
			reader.close();
			return new String(data, "utf-8");
		} else {
			return "";
		}

	}

	public static List<RevCommit> getRevs(Git git) throws NoHeadException, GitAPIException {
		List<RevCommit> commits = new ArrayList<>();

		Iterable<RevCommit> logs;

		logs = git.log().call();

		for (RevCommit rev : logs) {
			//System.out.println(RevCommit.toString(rev));
			commits.add(rev);
		}

		return commits;
	}

	public static ArrayList<RevCommit> getBFCList(List<String> issueKeys, List<RevCommit> revs) {
		// To avoid duplicate BFCs
		HashSet<RevCommit> BFCSet = new HashSet<RevCommit>();

		for (String issueKey : issueKeys) {
			for (RevCommit rev : revs)
				if (rev.getFullMessage().contains(issueKey))
					BFCSet.add(rev);
		}
		
		return new ArrayList<RevCommit>(BFCSet);
	}
	
	public static ArrayList<RevCommit> getBFCLIST(List<String> BFCID, List<RevCommit> revs) {
		// To avoid duplicate BFCs
		HashSet<RevCommit> BFCSet = new HashSet<RevCommit>();

		for (String commitID : BFCID) {
			for (RevCommit rev : revs)
				if (rev.getFullMessage().contains(commitID))
					BFCSet.add(rev);
		}
		
		return new ArrayList<RevCommit>(BFCSet);
	}

	public static List<String> getTargetPaths(Repository repo, List<RevCommit> BFCList) throws IOException {
		List<String> targetPaths = new ArrayList<>();

		for (RevCommit bfc : BFCList) {
			// Skip when there are no parents
			if (bfc.getParentCount() == 0)
				continue;

			RevCommit parent = bfc.getParent(0);
			if (parent == null)
				break;

			List<DiffEntry> diffs = GitUtils.diff(repo, parent.getTree(), bfc.getTree());

			// get changed paths
			for (DiffEntry diff : diffs) {
				String path = diff.getNewPath();

				// contains only files which are java files and not test files
				if (path.endsWith(".java") && !path.contains("test")) {
					targetPaths.add(path);
				}
			}
		}

		return targetPaths;
	}

}