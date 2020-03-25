package edu.handong.csee.isel.data.csv;

import java.util.Date;

import edu.handong.csee.isel.data.CSVInfo;

public class PatchInfo implements CSVInfo {

	final static String[] headers = { "Project", "fix-commit", "fix-shortMessage", "fix-date", "fix-author", "patch" };

	public String project;
	public String commitName;
	public String commitMessage;
	public String date;
	public String author;
	public String patch;
	
	@Override
	public String[] getHeaders() {
		return headers;
	}

}
