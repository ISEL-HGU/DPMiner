package edu.handong.csee.isel.commitUnitMetrics;

public class MetricVariable {
	
	static public final int numberOfMetrics = 1;
	
	int[] values = new int[numberOfMetrics];
	
	int numberOfModifyFiles;

	public int getNumberOfModifyFiles() {
		return numberOfModifyFiles;
	}

	public void setNumberOfModifyFiles(int numberOfModifyFiles) {
		this.numberOfModifyFiles = numberOfModifyFiles;
	}
	

}
