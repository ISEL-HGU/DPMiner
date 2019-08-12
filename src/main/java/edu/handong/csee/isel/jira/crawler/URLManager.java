package edu.handong.csee.isel.jira.crawler;

public class URLManager {
	private String domain;
	
	private static final String URL_FRAGMENT1 = "https://";
//	private static final String URL_FRAGMENT2 = "/sr/jira.issueviews:searchrequest-csv-all-fields/temp/SearchRequest.csv?jqlQuery=";
	private static final String URL_FRAGMENT2 = "/sr/jira.issueviews:searchrequest-csv-current-fields/temp/SearchRequest.csv?jqlQuery=";
	
	public URLManager(String domain) {
		super();
		this.domain = domain;
	}
	
	public String getURL(String encodedJql) {
		return URL_FRAGMENT1 + this.domain + URL_FRAGMENT2 + encodedJql;
	}

}
