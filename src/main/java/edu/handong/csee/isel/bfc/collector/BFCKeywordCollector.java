package edu.handong.csee.isel.bfc.collector;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jgit.revwalk.RevCommit;

import edu.handong.csee.isel.bfc.BFCCollector;

public class BFCKeywordCollector extends BFCCollector {
	public static String[] bugKeywords;

	public List<String> collectFrom(List<RevCommit> commitList) {

		List<String> bfcList = new ArrayList<>();

		final Pattern bugMessagePattern = Pattern.compile(String.join("|", bugKeywords), Pattern.CASE_INSENSITIVE);
//		System.out.println("bug Isskey in BFCKEYWORDS: "+ bugMessagePattern);

		for (RevCommit commit : commitList) {
			Matcher bugKeyMatcher = bugMessagePattern.matcher(commit.getShortMessage());

			if (bugKeyMatcher.find()) {
				bfcList.add(commit.getName());
			}
		}
		return bfcList;
	}

}
