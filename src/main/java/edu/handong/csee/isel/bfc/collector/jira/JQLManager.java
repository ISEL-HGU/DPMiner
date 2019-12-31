package edu.handong.csee.isel.bfc.collector.jira;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class JQLManager {
	private String projectKey;
	
	private static final String JQL_FRAGMENT1 = "project = ";
	private static final String JQL_FRAGMENT2 = " AND issuetype = Bug AND resolution = fixed AND created <= startOfDay(";
	private static final String JQL_FRAGMENT3 = ")";
	private static final String JQL_FRAGMENT4 = " AND created > startOfDay(";
	private static final String JQL_FRAGMENT5 = ")";
	
	public JQLManager(String projectKey) {
		super();
		this.projectKey = projectKey;
	}
	
	public String getJQL1(int end) {
		return JQL_FRAGMENT1 + this.projectKey + JQL_FRAGMENT2 + end + JQL_FRAGMENT3;
	}
	
	public String getJQL2(int start, int end) {
		return JQL_FRAGMENT1 + this.projectKey + JQL_FRAGMENT2 + end + JQL_FRAGMENT3 + JQL_FRAGMENT4 + start + JQL_FRAGMENT5;
	}
	
	public String getEncodedJQL(String jql) throws UnsupportedEncodingException {
		return URLEncoder.encode(jql, "UTF-8");
	}
}
