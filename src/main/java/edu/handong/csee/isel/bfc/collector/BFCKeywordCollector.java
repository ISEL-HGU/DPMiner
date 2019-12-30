package edu.handong.csee.isel.bfc.collector;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jgit.revwalk.RevCommit;

import edu.handong.csee.isel.bfc.BFCCollector;

public class BFCKeywordCollector extends BFCCollector {
	static String[] bugKeywords = { "bug", "fix" };

	public List<String> collectFrom(List<RevCommit> commitList) {

		List<String> bfsList = new ArrayList<>();

		final Pattern bugMessagePattern = Pattern.compile(String.join("|", bugKeywords), Pattern.CASE_INSENSITIVE);
		final Pattern keyPattern = Pattern.compile("\\[?(\\w+\\-\\d+)\\]?");

		for (RevCommit commit : commitList) {
			Matcher keyMatcher = keyPattern.matcher(commit.getShortMessage());
			if (keyMatcher.find()) {

				String issueKey = keyMatcher.group(0);
				Matcher bugKeyMatcher = bugMessagePattern.matcher(issueKey);

				if (bugKeyMatcher.find()) {
					bfsList.add(commit.getId().toString());
				}
			}
		}

		return bfsList;
	}

}
