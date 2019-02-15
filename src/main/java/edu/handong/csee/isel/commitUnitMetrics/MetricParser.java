package edu.handong.csee.isel.commitUnitMetrics;

import java.util.Arrays;
import java.util.List;

public class MetricParser {
	MetricVariable metricVariable = new MetricVariable();
	
	public void computeLine(String diffContent) {
		int numOfDeleteLines = 0;
		int numOfAddLines = 0;
		int distributionOfModifiedLines = 0;
		
		List<String> diffLines = Arrays.asList(diffContent.split("\\n"));
		
		for(int i = 4; i < diffLines.size(); i++) {
			String line = diffLines.get(i);
			if(line.startsWith("-")) numOfDeleteLines++;
			else if(line.startsWith("+")) numOfAddLines++;
			else if(line.startsWith("@@")) distributionOfModifiedLines++;
		}
		
		metricVariable.setNumOfModifyLines(numOfDeleteLines + numOfAddLines);
		metricVariable.setNumOfAddLines(numOfAddLines);
		metricVariable.setNumOfDeleteLines(numOfDeleteLines);
		metricVariable.setDistributionOfModifiedLines(distributionOfModifiedLines);
	}
	
	public void resetValues() {
		metricVariable.setNumOfModifyLines(0);
		metricVariable.setNumOfAddLines(0);
		metricVariable.setNumOfDeleteLines(0);
		metricVariable.setDistributionOfModifiedLines(0);
	}
	
	public void computeParsonIdent(String personIdent) {
		
	}

}
