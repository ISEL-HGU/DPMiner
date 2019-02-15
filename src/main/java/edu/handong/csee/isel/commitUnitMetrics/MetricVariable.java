package edu.handong.csee.isel.commitUnitMetrics;

public class MetricVariable {
	
	static public final int numberOfMetrics = 7;
	
	String[] values = new String[numberOfMetrics];
	
	String commitHash;
	String commitAuthor;
	int numOfModifyFiles;
	int numOfModifyLines;
	int numOfDeleteLines;
	int numOfAddLines;
	int DistributionOfModifiedLines;
	
	public String getCommitAuthor() {
		return values[6];
	}

	public void setCommitAuthor(String commitAuthor) {
		values[6] = commitAuthor;
	}
	
	public String getCommitHash() {
		return values[0];
	}

	public void setCommitHash(String commitHash) {
		values[0] = commitHash;
	}
	
	public String getDistributionOfModifiedLines() {
		return values[4];
	}

	public void setDistributionOfModifiedLines(int distributionOfModifiedLines) {
		values[4] = values[4]+String.valueOf(distributionOfModifiedLines);
	}

	public String getNumOfDeleteLines() {
		return values[3];
	}

	public void setNumOfDeleteLines(int numOfDeleteLines) {
		values[3] = values[3]+String.valueOf(numOfDeleteLines);
	}

	public String getNumOfAddLines() {
		return values[2];
	}

	public void setNumOfAddLines(int numOfAddLines) {
		values[2] = values[2]+String.valueOf(numOfAddLines);
	}

	public String getNumOfModifyLines() {
		return values[1];
	}

	public void setNumOfModifyLines(int numOfModifyLines) {
		values[1] = values[1]+String.valueOf(numOfModifyLines);
	}

	public String getNumOfModifyFiles() {
		return values[5];
	}

	public void setNumOfModifyFiles(int numOfModifyFiles) {
		values[5] = String.valueOf(numOfModifyFiles);
	}
	
}
