package edu.handong.csee.isel.patch;

import java.io.File;
import java.io.IOException;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;

public class MyExecutor extends Thread{
	private CommitStatus commitStatus;
	private String oldCommitHash;
	private String newCommitHash;
	private Git git;
	private Repository repository;
	
	public CommitStatus getCommitStatus() {
		return commitStatus;
	}


	public MyExecutor(String gitRepositoryPath, String oldCommitHash, String newCommitHash) throws IOException {
		this.oldCommitHash = oldCommitHash;
		this.newCommitHash = newCommitHash;
		this.git = Git.open(new File(gitRepositoryPath));
		this.repository = git.getRepository();
	}
	
	@Override
	public void run() {
		CommitStatus newCommitStatus = null;
		
		RevWalk walk = new RevWalk(repository);
		ObjectId id = repository.resolve(hashList[i]);
		RevCommit commit = walk.parseCommit(id);
		
		
		
		this.commitStatus = newCommitStatus;
	}
	
}
