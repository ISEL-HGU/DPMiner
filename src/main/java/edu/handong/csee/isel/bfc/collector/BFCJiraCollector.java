package edu.handong.csee.isel.bfc.collector;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.revwalk.RevCommit;

import edu.handong.csee.isel.bfc.BFCCollectable;
import edu.handong.csee.isel.bfc.collector.jira.JiraBugIssueCrawler;

public class BFCJiraCollector implements BFCCollectable {

	private String url;
	private String key;
	private String path;

	public BFCJiraCollector(String jiraURL, String jiraProjectKey, String outPath) {
		this.url = jiraURL;
		this.key = jiraProjectKey;
		this.path = outPath;
	}

	@Override
	public List<String> collectFrom(List<RevCommit> commitList) {
		
		Pattern pattern = Pattern.compile(key + "-\\d+", Pattern.CASE_INSENSITIVE);
		
		// JUDDI-1013 을 뜻한다. juddi or JUDDI
		
		List<String> bfcList = new ArrayList<>();
		List<String> keywordList = new ArrayList<>();

		try {
			JiraBugIssueCrawler jiraCrawler = new JiraBugIssueCrawler(url, key, path);
			File savedFile = jiraCrawler.getJiraBugs();

			String content = FileUtils.readFileToString(savedFile, "UTF-8");

			String[] lines = content.split("\n");

			for (String line : lines) {
				keywordList.add(line);
			}

		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}

		for (RevCommit commit : commitList) {
			String message;

			message = commit.getShortMessage();
			
			Matcher matcher = pattern.matcher(message);

			if (matcher.find()) {
				String key = matcher.group();
				
				if(keywordList.contains(key)) {
					
					bfcList.add(commit.getName());
					if(commit.getName().equals("53a3d5530bd337625374396199ab985e115025ed")) {
//						System.out.println("Message: " + message);
//						System.out.println("commit: " + commit.getName());
					}
				}
			}
		}

		return bfcList;
	}

}
