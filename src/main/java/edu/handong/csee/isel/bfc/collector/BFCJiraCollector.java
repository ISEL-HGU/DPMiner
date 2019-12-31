package edu.handong.csee.isel.bfc.collector;

import java.util.List;

import org.eclipse.jgit.revwalk.RevCommit;

import edu.handong.csee.isel.bfc.BFCCollector;
import edu.handong.csee.isel.bfc.collector.jira.JiraBugIssueCrawler;

public class BFCJiraCollector extends BFCCollector {

	String url;
	String key;

	public BFCJiraCollector() {

	}

	public List<String> collectFrom(List<RevCommit> commitList) {

		JiraBugIssueCrawler jiraCrawler = new JiraBugIssueCrawler();
		jiraCrawler.setURL(url);
		jiraCrawler.setKey(key);

		jiraCrawler.crawling();
		List<String> issueKeys = jiraCrawler.collectIssueKeys();

		return issueKeys;
	}

	@Override
	public void setJiraProjectKey(String key) {
		this.key = key;
	}

	@Override
	public void setJiraURL(String url) {
		this.url = url;
	}

}
