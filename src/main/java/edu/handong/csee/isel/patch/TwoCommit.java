package edu.handong.csee.isel.patch;

public class TwoCommit {
	
	public String getOldCommitHash() {
		return oldCommitHash;
	}
	public String getNewCommitHash() {
		return newCommitHash;
	}
	public TwoCommit(String oldCommitHash, String newCommitHash) {
		this.oldCommitHash = oldCommitHash;
		this.newCommitHash = newCommitHash;
	}
	
	String oldCommitHash;
	String newCommitHash;
}
