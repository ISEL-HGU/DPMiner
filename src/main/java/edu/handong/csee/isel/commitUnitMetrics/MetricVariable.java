package edu.handong.csee.isel.commitUnitMetrics;

public class MetricVariable {
	String commitHash;
	String commitAuthor;
	int numOfModifyFiles;
	int numOfModifyLines;
	int numOfDeleteLines;
	int numOfAddLines;
	int distributionOfModifiedLines;
	int numOfDirectories;
	int sumOfSourceRevision;
	int avgOfSourceRevision;
	int sumOfDeveloper;
	int avgOfDeveloper;
	
	public MetricVariable() {
		this.commitHash = null;
		this.commitAuthor = null;
		this.numOfModifyFiles = 0;
		this.numOfModifyLines = 0;
		this.numOfDeleteLines = 0;
		this.numOfAddLines = 0;
		this.distributionOfModifiedLines = 0;
		this.numOfDirectories = 0;
	}
	
	public String getCommitHash() {
		return commitHash;
	}
	public void setCommitHash(String commitHash) {
		this.commitHash = commitHash;
	}
	public String getCommitAuthor() {
		return commitAuthor;
	}
	public void setCommitAuthor(String commitAuthor) {
		this.commitAuthor = commitAuthor;
	}
	public int getNumOfModifyFiles() {
		return numOfModifyFiles;
	}
	public void setNumOfModifyFiles(int numOfModifyFiles) {
		this.numOfModifyFiles = numOfModifyFiles;
	}
	public int getNumOfModifyLines() {
		return numOfModifyLines;
	}
	public void setNumOfModifyLines(int numOfModifyLines) {
		this.numOfModifyLines = numOfModifyLines;
	}
	public int getNumOfDeleteLines() {
		return numOfDeleteLines;
	}
	public void setNumOfDeleteLines(int numOfDeleteLines) {
		this.numOfDeleteLines = numOfDeleteLines;
	}
	public int getNumOfAddLines() {
		return numOfAddLines;
	}
	public void setNumOfAddLines(int numOfAddLines) {
		this.numOfAddLines = numOfAddLines;
	}
	public int getDistributionOfModifiedLines() {
		return distributionOfModifiedLines;
	}
	public void setDistributionOfModifiedLines(int distributionOfModifiedLines) {
		this.distributionOfModifiedLines = distributionOfModifiedLines;
	}
	public int getNumOfDirectories() {
		return numOfDirectories;
	}
	public void setNumOfDirectories(int numOfDirectories) {
		this.numOfDirectories = numOfDirectories;
	}

	public int getSumOfSourceRevision() {
		return sumOfSourceRevision;
	}

	public void setSumOfSourceRevision(int sumOfSourceRevision) {
		this.sumOfSourceRevision = sumOfSourceRevision;
	}

	public int getAvgOfSourceRevision() {
		return avgOfSourceRevision;
	}

	public void setAvgOfSourceRevision(int avgOfSourceRevision) {
		this.avgOfSourceRevision = avgOfSourceRevision;
	}

	public int getSumOfDeveloper() {
		return sumOfDeveloper;
	}

	public void setSumOfDeveloper(int sumOfDeveloper) {
		this.sumOfDeveloper = sumOfDeveloper;
	}

	public int getAvgOfDeveloper() {
		return avgOfDeveloper;
	}

	public void setAvgOfDeveloper(int avgOfDeveloper) {
		this.avgOfDeveloper = avgOfDeveloper;
	}
	
}
