package edu.handong.csee.isel.java;

public class Data {
	public String getBranch() {
		return branch;
	}

	public void setBranch(String branch) {
		this.branch = branch;
	}

	public String getCommitHash() {
		return commitHash;
	}

	public void setCommitHash(String commitHash) {
		this.commitHash = commitHash;
	}

	String branch;
	String commitHash;
	
	public Data(String branch, String commitHash) {
		this.branch = branch;
		this.commitHash = commitHash;
	}
}
