package edu.handong.csee.isel.patch;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;

public class MyExecutor extends Thread {
	private String gitRepositoryPath;
	private CommitStatus commitStatus;
	private String oldCommitHash;
	private String newCommitHash;
	private Git git;
	private Repository repository;
	private ArrayList<String> issueHashList;
	public CommitStatus getCommitStatus() {
		return commitStatus;
	}

	public MyExecutor(String gitRepositoryPath, String oldCommitHash, String newCommitHash, ArrayList<String> issueHashList) throws IOException {
		this.gitRepositoryPath = gitRepositoryPath;
		this.oldCommitHash = oldCommitHash;
		this.newCommitHash = newCommitHash;
		this.git = Git.open(new File(gitRepositoryPath));
		this.repository = git.getRepository();
		this.issueHashList = issueHashList;
	}

	@Override
	public void run() {
		CommitStatus newCommitStatus = null;

		try {
			RevWalk walk = new RevWalk(repository);
			ObjectId id = repository.resolve(newCommitHash);
			RevCommit commit = walk.parseCommit(id);

			 

			// HashList에 있는 커밋인지 확인하는 중.
			boolean con = true;
			for (String issueHash : issueHashList) {

				if (commit.getShortMessage().contains(issueHash)) {
//					System.out.println("issue: " + issueHash + "\nshortMessage: " + commit.getShortMessage());
					con = false;
				}
			}
			if (con) {
				newCommitStatus = null;
			}
			else {
				Patch p = new Patch(gitRepositoryPath);
				ArrayList<File> diffFiles = null;
				diffFiles = p.pullDiffs(oldCommitHash, newCommitHash);
				
				String project = "Hbase";
				String shortMessage = commit.getShortMessage();
				String commitHash = newCommitHash;
				int date = commit.getCommitTime();
				String Author = commit.getAuthorIdent().getName();
				ArrayList<String> patches = p.getStringFromFiles(diffFiles);
				
				newCommitStatus = new CommitStatus(project, shortMessage, commitHash, date, Author, patches);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		this.commitStatus = newCommitStatus;
	}

}
