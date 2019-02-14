package edu.handong.csee.isel.commitUnitMetrics;

public class MetricVariable {
	
	static public final int numberOfMetrics = 5;
	
	int[] values = new int[numberOfMetrics];
	
	int numOfModifyFiles;
	int numOfModifyLines;
	int numOfDeleteLines;
	int numOfAddLines;
	int DistributionOfModifiedLines;

	public int getDistributionOfModifiedLines() {
		return values[4];
	}

	public void setDistributionOfModifiedLines(int distributionOfModifiedLines) {
		values[4] = distributionOfModifiedLines;
	}

	public int getNumOfDeleteLines() {
		return values[3];
	}

	public void setNumOfDeleteLines(int numOfDeleteLines) {
		values[3] = numOfDeleteLines;
	}

	public int getNumOfAddLines() {
		return values[2];
	}

	public void setNumOfAddLines(int numOfAddLines) {
		values[2] = numOfAddLines;
	}

	public int getNumOfModifyLines() {
		return values[1];
	}

	public void setNumOfModifyLines(int numOfModifyLines) {
		values[1] = numOfModifyLines;
	}

	public int getNumOfModifyFiles() {
		return values[0];
	}

	public void setNumOfModifyFiles(int numOfModifyFiles) {
		values[0] = numOfModifyFiles;
	}
	
}
