package edu.handong.csee.isel.metric.metadata;

import java.util.TreeSet;

public class SourceFileInfo {
	private TreeSet<String> developer;
	private int numOfModify;
	private int numOfBIC;
	private String makeDate;
	private String previousCommitDate;
	private String previousCommitHash;

	public SourceFileInfo() {
		this.developer = new TreeSet<String>();
		this.numOfModify = 0;
		this.numOfBIC = 0;
		this.makeDate = null;
		this.previousCommitDate = null;
		this.previousCommitHash = null;
	}
	public TreeSet<String> getDeveloper() {
		return developer;
	}
	public void setDeveloper(String developer) {
		this.developer.add(developer);
	}
	public int getNumOfModify() {
		return numOfModify;
	}
	public void setNumOfModify() {
		this.numOfModify++;
	}
	public int getNumOfBIC() {
		return numOfBIC;
	}
	public void setNumOfBIC() {
		this.numOfBIC++;
	}
	public String getMakeDate() {
		return makeDate;
	}
	public void setMakeDate(String makeDate) {
		this.makeDate = makeDate;
	}
	public String getPreviousCommitDate() {
		return previousCommitDate;
	}
	public void setPreviousCommitDate(String previousCommitDate) {
		this.previousCommitDate = previousCommitDate;
	}
	public String getPreviousCommitHash() {
		return previousCommitHash;
	}
	public void setPreviousCommitHash(String previousCommitHash) {
		this.previousCommitHash = previousCommitHash;
	}

}
