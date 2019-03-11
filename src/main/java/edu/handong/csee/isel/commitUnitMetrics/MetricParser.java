package edu.handong.csee.isel.commitUnitMetrics;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MetricParser {
	static HashMap<String,SourceFileInfo> sourceFileInfo = new HashMap<String,SourceFileInfo>();
	
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
	
	public String computeParsonIdent(String personIdent) {
		Pattern pattern = Pattern.compile(".+\\[(.+),.+,.+\\]");
		Matcher matcher = pattern.matcher(personIdent);
		while(matcher.find()) {
			String name = matcher.group(1);
			if(name.contains("%") && name.contains(".com")){
				int num = name.indexOf("%");
				return name.substring(0,num).toUpperCase();
			}else if(name.contains("@")) {
				int num = name.indexOf("@");
				return name.substring(0,num).toUpperCase();
			}
			return name.toUpperCase();
		}
		return personIdent.toUpperCase();
	}
	
	public void computeDirectory(String commitHash, TreeSet<String> rawPathOfDirectory) {
		MetricVariable metricVariable = CommitCollector.metricVariables.get(commitHash);
		TreeSet<String> pathOfDirectory = new TreeSet<String>();
		Pattern pattern = Pattern.compile("(.+)/.+\\..+");
		
		for(String line : rawPathOfDirectory) {
			if(!line.contains("/")) {
				pathOfDirectory.add(line);
				break;
			}
			Matcher matcher = pattern.matcher(line);
			while(matcher.find()) {
				if(!matcher.group(1).contains("/dev/null") && !matcher.group(1).contains("test")) {
				pathOfDirectory.add(matcher.group(1));
				System.out.println(matcher.group(1));
				}
			}
		}
		metricVariable.setNumOfDirectories(pathOfDirectory.size());
	}
	
	public void computeSourceInfo(String commitHash, String sourceFileName, String authorId) {
		MetricVariable metricVariable = CommitCollector.metricVariables.get(commitHash);
		Pattern pattern = Pattern.compile(".+/(.+\\..+)");
		SourceFileInfo aSourceFileInfo;
		
		Matcher matcher = pattern.matcher(sourceFileName);
		while(matcher.find()) {
			if(sourceFileInfo.get(matcher.group(1)) == null) {
				aSourceFileInfo = new SourceFileInfo();
				sourceFileInfo.put(matcher.group(1), aSourceFileInfo);
			}else {
				aSourceFileInfo = sourceFileInfo.get(matcher.group(1));
			}
			
			aSourceFileInfo.setNumOfModify();
			aSourceFileInfo.setDeveloper(authorId);
			
			metricVariable.setSumOfSourceRevision(aSourceFileInfo.getNumOfModify());
			metricVariable.setSumOfDeveloper(aSourceFileInfo.getDeveloper().size());
			
		}
		
		
//		System.out.println(sourceFileName);
//		System.out.println(authorId);
	}

}
