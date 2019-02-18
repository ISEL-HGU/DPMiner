package edu.handong.csee.isel.commitUnitMetrics;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MetricParser {
	
	public void computeLine(String commitHash,String diffContent) {
		MetricVariable metricVariable = CommitCollector.metricVariables.get(commitHash);
		int numOfDeleteLines = metricVariable.getNumOfDeleteLines();
		int numOfAddLines = metricVariable.getNumOfAddLines();
		int distributionOfModifiedLines = metricVariable.getDistributionOfModifiedLines();
		
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
	
	public void computeParsonIdent(String commitHash, String personIdent) {
		Pattern pattern = Pattern.compile(".+\\[(.+),.+,.+\\]");
		Matcher matcher = pattern.matcher(personIdent);
		while(matcher.find()) {
			MetricVariable metricVariable = CommitCollector.metricVariables.get(commitHash);
			metricVariable.setCommitAuthor(matcher.group(1));
		}
	}

}
