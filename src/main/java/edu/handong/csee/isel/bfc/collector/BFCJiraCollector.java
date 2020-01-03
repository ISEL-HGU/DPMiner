package edu.handong.csee.isel.bfc.collector;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.revwalk.RevCommit;

import edu.handong.csee.isel.bfc.BFCCollector;
import edu.handong.csee.isel.bfc.collector.jira.JiraBugIssueCrawler;

public class BFCJiraCollector extends BFCCollector {

	String url;
	String key;
	String path;

	public BFCJiraCollector() {

	}

	public List<String> collectFrom(List<RevCommit> commitList) {

		try {
			JiraBugIssueCrawler jiraCrawler = new JiraBugIssueCrawler(url, key, path);
			File savedFile = jiraCrawler.getJiraBugs();

			String content = FileUtils.readFileToString(savedFile, "UTF-8");

			String[] lines = content.split("\n");
			HashSet<String> keywordSet = new HashSet<>();

			for (String line : lines) {
				keywordSet.add(line);
			}

			return new ArrayList<>(keywordSet); // convert HashSet to List
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	@Override
	public void setJiraProjectKey(String key) {
		this.key = key;
	}

	@Override
	public void setJiraURL(String url) {
		this.url = url;
	}

	@Override
	public void setOutPath(String outPath) {
		this.path = outPath;
	}

}
