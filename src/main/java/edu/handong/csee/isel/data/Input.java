package edu.handong.csee.isel.data;

public class Input {

	public String gitURL = null;

	public String outPath;

	public String gitRemoteURI;

	public String jiraProjectKey;

	public String jiraURL;

	public String projectName;
	
	public int maxSize;
	public int minSize;
	
	public String label;

	public TaskType taskType; // -t 옵션(메인 전체 큰 옵션) 

	public Mode mode;// BIC 랑 Path일 경우 (ij, ig, ik 옵션 정하기 위해)  
	
	public String Issue_keyWord; //이슈키 추가한 부분 //고쳐야 한다
	
	public String BICpath;
	
	public String gitDirectory;
	
	public String startDate;
	
	public String endDate;
	
	public int percent;
	
	
	public static enum TaskType {
		Patch,BIC, Metric, Develop_Metirc
	}

	public static enum Mode {
		GitHub, Jira, KeyWord
	}

}
