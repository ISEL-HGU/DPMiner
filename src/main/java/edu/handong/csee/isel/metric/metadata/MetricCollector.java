package edu.handong.csee.isel.metric.metadata;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.handong.csee.isel.metric.metadata.CommitUnitInfo;
import edu.handong.csee.isel.metric.metadata.DeveloperExperienceInfo;
import edu.handong.csee.isel.metric.metadata.Metrics;
import edu.handong.csee.isel.metric.metadata.SourceFileInfo;
import edu.handong.csee.isel.metric.metadata.Utils;

public class MetricCollector { //metric Collector
	public void parsePatchContents(Metrics metric,CommitUnitInfo commitUnitInfo, String commitHash,String diffContent) {

		int numOfDeleteLines = 0; // metricVariable.getNumOfDeleteLines();
		int numOfAddLines = 0; // metricVariable.getNumOfAddLines();
		
		int numOfAddChunk = 0;
		int numOfDeleteChunk = 0;
		
		String beforeLine = "E";

		List<String> diffLines = Arrays.asList(diffContent.split("\\n"));

		for(int i = 5; i < diffLines.size(); i++) {
			String line = diffLines.get(i);
			
			if(line.startsWith("-")) {
				if(beforeLine.equals("+")) {
					numOfDeleteChunk++;
				}
				numOfDeleteLines++;
				beforeLine = "-";
			}
			else if(line.startsWith("+")) {
				if(beforeLine.equals("-")) {
					numOfDeleteChunk++;
				}
				numOfAddLines++;
				beforeLine = "+";
			}
			else if(line.startsWith("@@")){
				beforeLine = "E";
			}else {
				if(beforeLine.equals("-")) {
					numOfDeleteChunk++;
					beforeLine = "E";
				}else if(beforeLine.equals("+")) {
					numOfAddChunk++;
					beforeLine = "E";
				}
			}
		}
		
		if(beforeLine.equals("-")) {
			numOfDeleteChunk++;
		}else if(beforeLine.equals("+")) {
			numOfAddChunk++;
		}
		
		int numOfModifyLines = numOfDeleteLines + numOfAddLines;
		metric.setNumOfModifyLines(numOfModifyLines);
		metric.setNumOfAddLines(numOfAddLines);
		metric.setNumOfDeleteLines(numOfDeleteLines);
		metric.setNumOfAddChunk(numOfAddChunk);
		metric.setNumOfDeleteChunk(numOfDeleteChunk);
		metric.setNumOfModifyChunk(numOfAddChunk + numOfDeleteChunk);
		commitUnitInfo.setModifiedLines(numOfModifyLines);
		commitUnitInfo.setEntropy(commitUnitInfo.getEntropy() + (double)numOfModifyLines);
	}


	public void parseSourceInfo(Metrics metaDataInfo, HashMap<String,SourceFileInfo> sourceFileInfo, String sourceFileName, String authorId,boolean isBuggyCommit,String commitTime,String commitHash, CommitUnitInfo commitUnitInfo, String fileSource) throws Exception {
		SourceFileInfo aSourceFileInfo;//소스파일 정보 인스탄스 

		if(sourceFileInfo.containsKey(sourceFileName) == false) {//처음 만들어진 소스 파일 일 때 
			aSourceFileInfo = new SourceFileInfo();
			sourceFileInfo.put(sourceFileName, aSourceFileInfo);
			aSourceFileInfo.setMakeDate(commitTime);
			aSourceFileInfo.setPreviousCommitDate(commitTime);
			aSourceFileInfo.setPreviousCommitHash(commitHash);
		}else { //이미 존재하는 소스 파일 일 때 
			aSourceFileInfo = sourceFileInfo.get(sourceFileName);//정보 가져옴 
		}

		aSourceFileInfo.setNumOfModify();//소스파일 수정 횟수 +
		aSourceFileInfo.setDeveloper(authorId);//소스파일 수정한 개발자 이름 저장(tree set)

		if(isBuggyCommit == true) {//소스파일이 버그를 가진 전적 횟수 +
			aSourceFileInfo.setNumOfBIC();
		}
		
		metaDataInfo.setFileAge(Utils.calDate(aSourceFileInfo.getMakeDate(),commitTime));//오늘 날짜와 생성 날짜 비교하여 파일나이 저장
		metaDataInfo.setSumOfSourceRevision(aSourceFileInfo.getNumOfModify());//소스파일 총 수정 횟수 
		metaDataInfo.setSumOfDeveloper(aSourceFileInfo.getDeveloper().size());//소스파일을 수정한 개발자 수(중복 허용 안함)
		metaDataInfo.setNumOfBIC(aSourceFileInfo.getNumOfBIC());//(버그를 수정한 횟수)
		metaDataInfo.setTimeBetweenLastAndCurrentCommitDate(Utils.calDate(aSourceFileInfo.getPreviousCommitDate(),commitTime));
		metaDataInfo.setLinesOfCodeBeforeTheChange(Arrays.asList(fileSource.split("\\n")).size());
		
		commitUnitInfo.setPreviousCommitHashs(aSourceFileInfo.getPreviousCommitHash());

		aSourceFileInfo.setPreviousCommitDate(commitTime);
		aSourceFileInfo.setPreviousCommitHash(commitHash);
	}

	public void parseDeveloperInfo(Metrics metaDataInfo, HashMap<String,Integer> developerExperience, String authorId) {

		if(developerExperience.containsKey(authorId) == false) {
			developerExperience.put(authorId, 1);
		}else {
			developerExperience.put(authorId, developerExperience.get(authorId) + 1);
		}

		metaDataInfo.setDeveloperExperience(developerExperience.get(authorId));

	}

	public void parseCommitUnitInfo(CommitUnitInfo commitUnitInfo, String sourcePath, String key,HashMap<String,DeveloperExperienceInfo> developerExperience,String authorId) {
		String[] pathToken = sourcePath.split("/");
		String subsystem = pathToken[0];
		String file = pathToken[pathToken.length-1];
		String directorie = null;

		Pattern pattern = Pattern.compile("(.+)/.+");
		Matcher matcher = pattern.matcher(sourcePath);
		while(matcher.find()) {
			directorie = matcher.group(1);
		}
		
		if(!subsystem.startsWith("src")) {
			commitUnitInfo.setSubsystems(subsystem);
			developerExperience.get(authorId).setNumOfSubsystem(subsystem);
		}

		commitUnitInfo.setKey(key);
		commitUnitInfo.setDirectories(directorie);
		commitUnitInfo.setFiles(file);
	}
	
	public void computeDeveloperInfo(HashMap<String,DeveloperExperienceInfo> developerExperience,String authorId, String commitTime, int numOfSubsystem) {
		String[] TimeToken = commitTime.split("-");
		int year =  Integer.parseInt(TimeToken[0]) + 1;
		float REXP = 0;
		
		if(developerExperience.containsKey(authorId) == false) return;
		
		TreeMap<Integer, Integer> recentExperience = developerExperience.get(authorId).getRecentExperiences();
		
		Set<Map.Entry<Integer, Integer>> entries = recentExperience.entrySet();
		for (Map.Entry<Integer,Integer> entry : entries) {
			int key = entry.getKey();
			int denominator = year - key;
			int numerator = entry.getValue();
			REXP = REXP + (float)numerator/denominator;
		}
		developerExperience.get(authorId).setREXP(REXP);
	}
	
	public void computeEntropy(CommitUnitInfo commitUnitInfo) {
		ArrayList<Integer> modifiedLines = commitUnitInfo.getModifiedLines();

		double allNum = commitUnitInfo.getEntropy();
		double entropy = 0.0;

		for(int modifiedLine : modifiedLines) {
			if(modifiedLine == 0) continue;
			double value = (double)modifiedLine/(double)allNum;
			double log = Math.log(value) / Math.log(2);
			entropy += (value * log) * -1;
		}
		commitUnitInfo.setEntropy((double)Math.round(entropy*1000)/1000);

	}
}
