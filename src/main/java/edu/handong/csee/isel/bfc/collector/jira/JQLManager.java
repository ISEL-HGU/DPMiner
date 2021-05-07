package edu.handong.csee.isel.bfc.collector.jira;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * 지라에서 버그 이슈를 추출하기 위한 Jira Query Language 문법을 제공하는 클래스이다. <br>
 * This class provides Jira Query Language syntax to extract bug issues from Jira.
 */
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

	/**
	 *
	 * @param end end date.
	 * @return
	 */
	public String getJQL1(int end) {
		return JQL_FRAGMENT1 + this.projectKey + JQL_FRAGMENT2 + end + JQL_FRAGMENT3;
	}

	/**
	 *
	 * @param start start date
	 * @param end end date
	 * @return
	 */
	public String getJQL2(int start, int end) {
		return JQL_FRAGMENT1 + this.projectKey + JQL_FRAGMENT2 + end + JQL_FRAGMENT3 + JQL_FRAGMENT4 + start + JQL_FRAGMENT5;
	}

	/**
	 *
	 * @param jql Receive iql grammar.
	 * @return Returns the result of encoding iql grammar as UTF-8.
	 * @throws UnsupportedEncodingException
	 */
	public String getEncodedJQL(String jql) throws UnsupportedEncodingException {
		return URLEncoder.encode(jql, "UTF-8");
	}
}
