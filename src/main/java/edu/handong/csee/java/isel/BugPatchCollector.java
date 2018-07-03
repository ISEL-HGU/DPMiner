package edu.handong.csee.java.isel;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.eclipse.jgit.errors.AmbiguousObjectException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.errors.RevisionSyntaxException;

public class BugPatchCollector {

	public static void main(String[] args) {
		File directory = new File("/Users/imseongbin/documents/Java/BugPatchCollector");
		BugPatchCollector bc = new BugPatchCollector();
		String selectedBranch = "";

		// selectedBranch = "Test2";
		
		
		try {

			Git git = Git.open(new File(directory.toString() + "/.git"));
			Repository repository = git.getRepository();

			Iterable<RevCommit> logs = git.log().call();

			String oldCommitHash = "45c754d81e119a4b1d51116454b9717e64c77be7";
			String newCommitHash = "47eb740a401d0d2cb8b6423e0af2bff8a51dfb4a";

			List<String> paths = bc.readElementsAt(repository, oldCommitHash,
					"src/main/java/edu/handong/csee/java/isel");

			System.out.println("Had paths for commit: " + paths);

			/* print patch between two commits */
			// bc.printPatch(oldCommitHash, newCommitHash, repository);

			/* Commit message */
			// ArrayList<String> messages = bc.getCommitMessages(git);
			// for (String message : messages) {
			// System.out.println(message);
			// }
			/* git-diff */
			// bc.printGitDiff(git);

			/* pull commits list and print them */
			// bc.printLog(logs, repository, selectedBranch);

			/* commit Hash */
			// ArrayList<String> commitHashList = ne w ArrayList<String>();
			// commitHashList = bc.loadCommitHash(logs, repository, selectedBranch);
			// System.out.println("Commit Hash: ");
			// for (String hash : commitHashList) {
			// System.out.println(hash);
			// }

			/* commit Message */
			// ArrayList<String> commitMessageList = new ArrayList<String>();
			// commitMessageList = bc.loadCommitMessages(logs, repository, selectedBranch);
			// System.out.println("Commit Messages: ");
			// for(String message : commitMessageList) {
			// System.out.println(message);
			// }

		} catch (Exception e) {
			System.out.println(e.fillInStackTrace());
		}
	}
	
	private static RevCommit buildRevCommit(Repository repository, String commit) throws IOException {
        // a RevWalk allows to walk over commits based on some filtering that is defined
        try (RevWalk revWalk = new RevWalk(repository)) {
            return revWalk.parseCommit(ObjectId.fromString(commit));
        }
    }

    private static TreeWalk buildTreeWalk(Repository repository, RevTree tree, final String path) throws IOException {
        TreeWalk treeWalk = TreeWalk.forPath(repository, path, tree);

        if(treeWalk == null) {
            throw new FileNotFoundException("Did not find expected file '" + path + "' in tree '" + tree.getName() + "'");
        }

        return treeWalk;
    }
	
	private List<String> readElementsAt(Repository repository, String commit, String path) throws IOException {
        RevCommit revCommit = buildRevCommit(repository, commit);

        // and using commit's tree find the path
        RevTree tree = revCommit.getTree();
        //System.out.println("Having tree: " + tree + " for commit " + commit);

        List<String> items = new ArrayList<>();

        // shortcut for root-path
        if(path.isEmpty()) {
            try (TreeWalk treeWalk = new TreeWalk(repository)) {
                treeWalk.addTree(tree);
                treeWalk.setRecursive(false);
                treeWalk.setPostOrderTraversal(false);

                while(treeWalk.next()) {
                    items.add(treeWalk.getPathString());
                }
            }
        } else {
            // now try to find a specific file
            try (TreeWalk treeWalk = buildTreeWalk(repository, tree, path)) {
                if((treeWalk.getFileMode(0).getBits() & FileMode.TYPE_TREE) == 0) {
                    throw new IllegalStateException("Tried to read the elements of a non-tree for commit '" + commit + "' and path '" + path + "', had filemode " + treeWalk.getFileMode(0).getBits());
                }

                try (TreeWalk dirWalk = new TreeWalk(repository)) {
                    dirWalk.addTree(treeWalk.getObjectId(0));
                    dirWalk.setRecursive(false);
                    while(dirWalk.next()) {
                        items.add(dirWalk.getPathString());
                    }
                }
            }
        }

        return items;
    }
	
	private void printPatch(String oldCommitHash, String newCommitHash, Repository repository)
			throws IOException, GitAPIException {

		Git git = new Git(repository);

		AbstractTreeIterator oldTreeParser = prepareTreeParser(repository, oldCommitHash);
		AbstractTreeIterator newTreeParser = prepareTreeParser(repository, newCommitHash);

		List<DiffEntry> diff = git.diff().setOldTree(oldTreeParser).setNewTree(newTreeParser)
				.setPathFilter(PathFilter.create("src/main/java/edu/handong/csee/java/isel/TestClass.java")).
				// to filter on Suffix use the following insteadF
				// setPathFilter(PathSuffixFilter.create(".java")).
				call();
		for (DiffEntry entry : diff) {
			System.out.println("Entry: " + entry + ", from: " + entry.getOldId() + ", to: " + entry.getNewId());
			try (DiffFormatter formatter = new DiffFormatter(System.out)) {
				formatter.setRepository(repository);
				formatter.format(entry);
			}
		}

	}

	private ArrayList<String> loadCommitHash(Iterable<RevCommit> logs, Repository repository, String selectedBranch)
			throws RevisionSyntaxException, NoHeadException, MissingObjectException, IncorrectObjectTypeException,
			AmbiguousObjectException, GitAPIException, IOException {
		Git git = new Git(repository);
		ArrayList<String> commitHashList = new ArrayList<String>();

		int count = 0;
		for (RevCommit rev : logs) {
			// System.out.println("Commit: " + rev /* + ", name: " + rev.getName() + ", id:
			// " + rev.getId().getName() */);
			count++;
		}

		logs = git.log().add(repository.resolve("remotes/origin/" + selectedBranch)).call();
		count = 0;
		for (RevCommit rev : logs) {
			// System.out.println("Commit: " + rev /* + ", name: " + rev.getName() + ", id:
			// " + rev.getId().getName() */);

			commitHashList.add(rev.name());
			count++;
		}
		return commitHashList;
	}

	private ArrayList<String> loadCommitMessages(Iterable<RevCommit> logs, Repository repository, String selectedBranch)
			throws RevisionSyntaxException, NoHeadException, MissingObjectException, IncorrectObjectTypeException,
			AmbiguousObjectException, GitAPIException, IOException {

		Git git = new Git(repository);
		ArrayList<String> commitMessageList = new ArrayList<String>();

		int count = 0;
		for (RevCommit rev : logs) {
			// System.out.println("Commit: " + rev /* + ", name: " + rev.getName() + ", id:
			// " + rev.getId().getName() */);
			count++;
		}

		logs = git.log().add(repository.resolve("remotes/origin/" + selectedBranch)).call();
		count = 0;
		for (RevCommit rev : logs) {
			// System.out.println("Commit: " + rev /* + ", name: " + rev.getName() + ", id:
			// " + rev.getId().getName() */);

			commitMessageList.add(rev.getShortMessage());
			count++;
		}
		return commitMessageList;
	}

	private void printLog(Iterable<RevCommit> logs, Repository repository, String selectedBranch)
			throws RevisionSyntaxException, NoHeadException, MissingObjectException, IncorrectObjectTypeException,
			AmbiguousObjectException, GitAPIException, IOException {
		Git git = new Git(repository);

		int count = 0;
		for (RevCommit rev : logs) {
			// System.out.println("Commit: " + rev /* + ", name: " + rev.getName() + ", id:
			// " + rev.getId().getName() */);
			count++;
		}
		System.out.println("Had " + count + " commits overall on " + selectedBranch + " branch");

		logs = git.log().add(repository.resolve("remotes/origin/" + selectedBranch)).call();
		count = 0;
		for (RevCommit rev : logs) {
			// System.out.println("Commit: " + rev /* + ", name: " + rev.getName() + ", id:
			// " + rev.getId().getName() */);

			System.out.println("Message: " + rev.getShortMessage());
			System.out.println("Commit: " + rev.name());
			count++;
		}

	}

	private ArrayList<String> getCommitMessages(Git git) throws Exception {
		ArrayList<String> commitMessages = new ArrayList<String>();

		Iterable<RevCommit> log = git.log().call();
		for (Iterator<RevCommit> iterator = log.iterator(); iterator.hasNext();) {
			RevCommit rev = iterator.next();
			commitMessages.add(rev.getFullMessage());
		}

		return commitMessages;
	}

	private void printGitDiff(Git git) throws GitAPIException {
		git.diff().setOutputStream(System.out).call();
	}

	private static AbstractTreeIterator prepareTreeParser(Repository repository, String objectId) throws IOException {
		// from the commit we can build the tree which allows us to construct the
		// TreeParser
		// noinspection Duplicates
		try (RevWalk walk = new RevWalk(repository)) {
			RevCommit commit = walk.parseCommit(ObjectId.fromString(objectId));
			RevTree tree = walk.parseTree(commit.getTree().getId());

			CanonicalTreeParser treeParser = new CanonicalTreeParser();
			try (ObjectReader reader = repository.newObjectReader()) {
				treeParser.reset(reader, tree.getId());
			}

			walk.dispose();

			return treeParser;
		}
	}
}
