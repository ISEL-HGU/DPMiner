package edu.handong.csee.isel.bfc.collector.jira;

/**
 * JQL을 검색할 수 있는 url을 관리한다. <br>
 * Manages the url to search for JQL.
 */
public class URLManager {
	private String domain;
	
	private static final String URL_FRAGMENT1 = "https://";
//	private static final String URL_FRAGMENT2 = "/sr/jira.issueviews:searchrequest-csv-all-fields/temp/SearchRequest.csv?jqlQuery=";
	private static final String URL_FRAGMENT2 = "/sr/jira.issueviews:searchrequest-csv-current-fields/temp/SearchRequest.csv?jqlQuery=";
	
	public URLManager(String domain) {
		super();
		this.domain = domain;
	}

	/**
	 *
	 * @param encodedJql The result of encoding jql grammar
	 * @return	Returns the url to search for jql grammar and the input encoded jql grammar.
	 */
	public String getURL(String encodedJql) {
		return URL_FRAGMENT1 + this.domain + URL_FRAGMENT2 + encodedJql;
	}

}
