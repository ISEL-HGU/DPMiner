package edu.handong.csee.isel.data;


public class Input {

	public static String gitURL = null;

	public static String outPath;

	public static String gitRemoteURI;

	public static String jiraProjectKey;

	public static String jiraURL;
	
	public static String projectName;
	
	public static int maxSize;
	
	public static int minSize;
	
	public static String label;

	public static TaskType taskType; // -t 옵션(메인 전체 큰 옵션) 

	public static Mode mode;// BIC 랑 Path일 경우 (ij, ig, ik 옵션 정하기 위해)  
	
	public static String issueKeyWord; //이슈키 추가한 부분 //고쳐야 한다
	
	public static String BICpath;
	
	public static String gitDirectory;
	
	public static String startDate;
	
	public static String endDate;
	
	public static int percent;
	
	
	public static enum TaskType {
		PATCH, BIC, METRIC, DEVELOPERMETRIC
	}

	public static enum Mode {
		GITHUB, JIRA, KEYWORD
	}

}

