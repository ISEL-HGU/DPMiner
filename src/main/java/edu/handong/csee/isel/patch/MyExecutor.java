package edu.handong.csee.isel.patch;

public class MyExecutor extends Thread{
	private CommitStatus commitStatus;
	private String commitHash;
	
	public CommitStatus getCommitStatus() {
		return commitStatus;
	}


	public MyExecutor(String commitHash) {
		this.commitHash = commitHash;
	}
	
	@Override
	public void run() {
		
	}
	
}
