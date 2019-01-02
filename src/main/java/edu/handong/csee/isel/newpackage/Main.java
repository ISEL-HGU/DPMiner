package edu.handong.csee.isel.newpackage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.diff.DiffEntry;
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
				
				
				
				
				

			} catch (ArrayIndexOutOfBoundsException e) {
				break;
			}

		}

	}

	
}
