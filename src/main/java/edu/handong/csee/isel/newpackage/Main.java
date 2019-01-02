package edu.handong.csee.isel.newpackage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;

public class Main {

	public static void main(String[] args)
			throws InvalidRemoteException, TransportException, GitAPIException, IOException {
		// 1. 3가지 방법으로 commit list를 가져온다.
		// - git fetch로 가져옴
		// 2. keywords를 가져온다.
		// 3.

//		final String REMOTE_URL = "https://github.com/apache/zookeeper.git";
		final String REMOTE_URL = "https://github.com/HGUISEL/BugPatchCollector.git";
		
		Git git = gitClone(REMOTE_URL);
		Repository repo = git.getRepository();
		RevWalk walk = new RevWalk(repo);

		for (Map.Entry<String, Ref> entry : repo.getAllRefs().entrySet()) {
			if (entry.getKey().contains("refs/heads/master")) { // only master
				Ref ref = entry.getValue();
				RevCommit commit = walk.parseCommit(ref.getObjectId());
				walk.markStart(commit);
			}
		}

		for (RevCommit commit : walk) {
			try {
				RevCommit parent = commit.getParent(0);

				listDiff(repo, git,
                        commit.getId().name(),
                        parent.getId().name());

			} catch (ArrayIndexOutOfBoundsException e) {
				break;
			}

		}

	}

	public static Git gitClone(String REMOTE_URL)
			throws InvalidRemoteException, TransportException, GitAPIException, IOException {
		Pattern p = Pattern.compile(".*/(\\w+)\\.git");
		Matcher m = p.matcher(REMOTE_URL);
		m.find();
		File repositoriesDir = new File("repositories" + File.separator + m.group(1));

		if (repositoriesDir.exists()) {
			return Git.open(repositoriesDir);
		}

		repositoriesDir.mkdirs();
		return Git.cloneRepository().setURI(REMOTE_URL).setDirectory(repositoriesDir)
//				  .setBranch("refs/heads/master") // only master
				.setCloneAllBranches(true).call();
	}

	private static AbstractTreeIterator prepareTreeParser(Repository repository, String objectId) throws IOException {
        // from the commit we can build the tree which allows us to construct the TreeParser
        //noinspection Duplicates
        try (RevWalk walk = new RevWalk(repository)) {
            RevCommit commit = walk.parseCommit(repository.resolve(objectId));
            RevTree tree = walk.parseTree(commit.getTree().getId());

            CanonicalTreeParser treeParser = new CanonicalTreeParser();
            try (ObjectReader reader = repository.newObjectReader()) {
                treeParser.reset(reader, tree.getId());
            }

            walk.dispose();

            return treeParser;
        }
    }

	public static ArrayList<String> getPathList(RevCommit commit, Repository repository) throws IOException {
		ArrayList<String> pathList = new ArrayList<String>();

		RevWalk walk = new RevWalk(repository);
		RevTree tree = commit.getTree();
		TreeWalk treeWalk = new TreeWalk(repository);
		treeWalk.addTree(tree);
		treeWalk.setRecursive(true);
		while (treeWalk.next()) {
			pathList.add(treeWalk.getPathString());
			// System.out.println(" found: " + treeWalk.getPathString());
		}

		walk.dispose();
		return pathList;
	}
	private static void listDiff(Repository repository, Git git, String oldCommit, String newCommit) throws GitAPIException, IOException {
        final List<DiffEntry> diffs = git.diff()
                .setOldTree(prepareTreeParser(repository, oldCommit))
                .setNewTree(prepareTreeParser(repository, newCommit))
                .call();

        System.out.println("Found: " + diffs.size() + " differences");
        for (DiffEntry diff : diffs) {
            
            switch(diff.getChangeType().ordinal()) {
            case 0: //ADD
            	continue;
            case 1: //MODIFY
            	if(!diff.getNewPath().endsWith(".java"))
            		continue;
//                File newFile = new File("temp" + File.separator + newCommit + "-" + String.valueOf(i) + ".txt");
//                if(newFile.getParentFile().exists())
//                	newFile.getParentFile().delete();
//                newFile.getParentFile().mkdirs();
                
                ByteArrayOutputStream output = new ByteArrayOutputStream();

        		try (DiffFormatter formatter = new DiffFormatter(output)) {
        			formatter.setRepository(repository);
        			formatter.format(diff);
        		}
        		output.flush();
        		output.close();
        		System.out.println(output.toString("UTF-8"));
            case 2: //DELETE
            	continue;
            }
            
        }
        
        
    }
}
