package edu.handong.csee.isel.bfc.collector;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.revwalk.RevCommit;

import edu.handong.csee.isel.bfc.BFCCollectable;
import edu.handong.csee.isel.bfc.collector.jira.JiraBugIssueCrawler;
import edu.handong.csee.isel.data.Input;

public class BFCJiraCollector implements BFCCollectable {

	String url = Input.jiraURL;
	String key = Input.jiraProjectKey;
	String path = Input.outPath;

	public BFCJiraCollector() {

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
						System.out.println("Message: " + message);
						System.out.println("commit: " + commit.getName());
					}
				}
			}
		}

		return bfcList;
	}

}
