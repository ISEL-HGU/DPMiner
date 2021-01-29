package edu.handong.csee.isel.data;

import java.util.HashMap;

public class Input {

	public static String getGitURL() {
		return gitURL;
	}

	public static void setGitURL(String gitURL) {
		Input.gitURL = gitURL;
	}

	public static String getOutPath() {
		return outPath;
	}

	public static void setOutPath(String outPath) {
		Input.outPath = outPath;
	}

	public static String getGitRemoteURI() {
		return gitRemoteURI;
	}

	public static void setGitRemoteURI(String gitRemoteURI) {
		Input.gitRemoteURI = gitRemoteURI;
	}

	public static String getJiraProjectKey() {
		return jiraProjectKey;
	}

	public static void setJiraProjectKey(String jiraProjectKey) {
		Input.jiraProjectKey = jiraProjectKey;
	}

	public static String getJiraURL() {
		return jiraURL;
	}

	public static void setJiraURL(String jiraURL) {
		Input.jiraURL = jiraURL;
	}

	public static String getProjectName() {
		return projectName;
	}

	public static void setProjectName(String projectName) {
		Input.projectName = projectName;
	}

	public static int getMaxSize() {
		return maxSize;
	}

	public static void setMaxSize(int maxSize) {
		Input.maxSize = maxSize;
	}

	public static int getMinSize() {
		return minSize;
	}

	public static void setMinSize(int minSize) {
		Input.minSize = minSize;
	}

	public static String getLabel() {
		return label;
	}

	public static void setLabel(String label) {
		Input.label = label;
	}

	public static TaskType getTaskType() {
		return taskType;
	}

	public static void setTaskType(TaskType taskType) {
		Input.taskType = taskType;
	}

	public static Mode getMode() {
		return mode;
	}

	public static void setMode(Mode mode) {
		Input.mode = mode;
	}

	public static SZZMode getSzzMode() {
		return szzMode;
	}

	public static void setSzzMode(SZZMode szzMode) {
		Input.szzMode = szzMode;
	}

	public static String getIssueKeyWord() {
		return issueKeyWord;
	}

	public static void setIssueKeyWord(String issueKeyWord) {
		Input.issueKeyWord = issueKeyWord;
	}

	public static String getBICpath() {
		return BICpath;
	}

	public static void setBICpath(String bICpath) {
		BICpath = bICpath;
	}

	public static String getGitDirectory() {
		return gitDirectory;
	}

	public static void setGitDirectory(String gitDirectory) {
		Input.gitDirectory = gitDirectory;
	}

	public static String getStartDate() {
		return startDate;
	}

	public static void setStartDate(String startDate) {
		Input.startDate = startDate;
	}

	public static String getEndDate() {
		return endDate;
	}

	public static void setEndDate(String endDate) {
		Input.endDate = endDate;
	}

	public static int getPercent() {
		return percent;
	}

	public static void setPercent(int percent) {
		Input.percent = percent;
	}

	public static String getAuthToken() {
		return authToken;
	}

	public static void setAuthToken(String authToken) {
		Input.authToken = authToken;
	}

	public static HashMap<String, String> getFindRepoOpt() {
		return findRepoOpt;
	}

	public static void setFindRepoOpt(HashMap<String, String> findRepoOpt) {
		Input.findRepoOpt = findRepoOpt;
	}

	public static String getCommitCountBase() {
		return commitCountBase;
	}

	public static void setCommitCountBase(String commitCountBase) {
		Input.commitCountBase = commitCountBase;
	}

	public static String gitURL;
	
	public static String resultKeyWord;

	public static String getResultKeyWord() {
		return resultKeyWord;
	}

	public static void setResultKeyWord(String resultKeyWord) {
		Input.resultKeyWord = resultKeyWord;
	}

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
	
	public static SZZMode szzMode;
	
	public static String issueKeyWord; //이슈키 추가한 부분 //고쳐야 한다
	
	public static String BICpath;
	
	public static String gitDirectory;
	
	public static String startDate;
	
	public static String endDate;
	
	public static int percent;
	
	


	public static String getRecentDateBefore() {
		return recentDateBefore;
	}

	public static void setRecentDateBefore(String recentDateBefore) {
		Input.recentDateBefore = recentDateBefore;
	}

	public static String getRecentDateAfter() {
		return recentDateAfter;
	}

	public static void setRecentDateAfter(String recentDateAfter) {
		Input.recentDateAfter = recentDateAfter;
	}

	public static String getCreateDateBefore() {
		return createDateBefore;
	}

	public static void setCreateDateBefore(String createDateBefore) {
		Input.createDateBefore = createDateBefore;
	}

	public static String getCreateDateAfter() {
		return createDateAfter;
	}

	public static void setCreateDateAfter(String createDateAfter) {
		Input.createDateAfter = createDateAfter;
	}

	public static String getLanguageType() {
		return languageType;
	}

	public static void setLanguageType(String languageType) {
		Input.languageType = languageType;
	}

	public static String getForkNumMin() {
		return forkNumMin;
	}

	public static void setForkNumMin(String forkNumMin) {
		Input.forkNumMin = forkNumMin;
	}

	public static String getForkNumMax() {
		return forkNumMax;
	}

	public static void setForkNumMax(String forkNumMax) {
		Input.forkNumMax = forkNumMax;
	}

	public static String recentDateBefore;
	public static String recentDateAfter;
	
	public static String createDateBefore;
	public static String createDateAfter;
	
	
	public static String authToken;
	
	public static HashMap<String, String> findRepoOpt;
	
	public static String commitCountBase;
	
	public static String languageType;
	
	public static String forkNumMin;
	public static String forkNumMax;
	
	
	
	public static enum TaskType {
		PATCH, BIC, METRIC, DEVELOPERMETRIC, FINDREPO
	}

	public static enum Mode {
		GITHUB, JIRA, KEYWORD
	}
	
	public static enum SZZMode {
		BSZZ, AGSZZ
	}

}

