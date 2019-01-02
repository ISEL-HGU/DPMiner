package edu.handong.csee.isel.newpackage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;

public class Main {

	public static void main(String[] args)
			throws InvalidRemoteException, TransportException, GitAPIException, IOException {
		// 1. 3가지 방법으로 commit list를 가져온다.
		// - git fetch로 가져옴
		// 2. keywords를 가져온다.
		// 3.

//		final String REMOTE_URL = "https://github.com/apache/zookeeper.git";
		final String REMOTE_URL = "https://github.com/HGUISEL/BugPatchCollector.git";
		int min = 0;
		int max = 5;
		
		Git git = Utils.gitClone(REMOTE_URL);
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

//				List<DiffEntry> diffs = Utils.listDiff(repo, git,
//                        commit.getId().name(),
//                        parent.getId().name(),min,max);
				
				final List<DiffEntry> diffs = git.diff()
		                .setOldTree(Utils.prepareTreeParser(repo, commit.getId().name()))
		                .setNewTree(Utils.prepareTreeParser(repo, parent.getId().name()))
		                .call();
				
				for(DiffEntry diff : diffs) {
					String patch = null;
					if((patch = passConditions(diff,repo,min,max))==null) // it cannot pass on conditions
						continue;
					
					diff.getNewPath();
					diff.getOldPath();
					diff.getOldId();
					diff.getNewId();
					//patch
					commit.getAuthorIdent();
					parent.getAuthorIdent();
				}
				
				
				
				

			} catch (ArrayIndexOutOfBoundsException e) {
				break;
			}

		}

	}

	public static String passConditions(DiffEntry diff,Repository repository,int min,int max) throws IOException {
		
		String patch = null;
		switch(diff.getChangeType().ordinal()) {
        case 0: //ADD
        	break;
        case 1: //MODIFY
        	if(!diff.getNewPath().endsWith(".java"))
        		break;
        	
            ByteArrayOutputStream output = new ByteArrayOutputStream();
    		try (DiffFormatter formatter = new DiffFormatter(output)) {
    			formatter.setRepository(repository);
    			formatter.format(diff);
    		}
    		output.flush();
    		output.close();
    		patch = output.toString("UTF-8");
    		if (patch.equals("") || (max != -1) && (min != -1)
					&& Utils.isExceedcondition(patch, max, min))
    			return null;
    		
        case 2: //DELETE
        	break;
        }
		
		return patch;
	}
	
}
