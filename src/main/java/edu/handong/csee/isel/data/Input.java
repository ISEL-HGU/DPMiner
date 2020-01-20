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

	public ReferenceType referecneType;

	public Mode mode;
	
	public String BICpath;
	
	public String gitDirectory;

	public static enum ReferenceType {
		JIRA, GITHUB, KEYWORD
	}

	public static enum Mode {
		PATCH, BIC, METRIC
	}

}
