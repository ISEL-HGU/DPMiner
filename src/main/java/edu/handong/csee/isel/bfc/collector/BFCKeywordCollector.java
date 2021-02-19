package edu.handong.csee.isel.bfc.collector;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jgit.revwalk.RevCommit;

import edu.handong.csee.isel.bfc.BFCCollectable;

public class BFCKeywordCollector implements BFCCollectable {
	private String[] bugKeywords;

	public BFCKeywordCollector(String issueKeyWord) {
		super();
		if(issueKeyWord != null) {
//			System.out.println(issueKeyWord);
			this.bugKeywords = new String[1];
			this.bugKeywords[0]="("+issueKeyWord+")";
		}
		else {
			this.bugKeywords = new String[2];
			this.bugKeywords[0]="(bug)";
			this.bugKeywords[1]="(fix)"; 
		}
	}

	@Override
	public List<String> collectFrom(List<RevCommit> commitList) {

		List<String> bfcList = new ArrayList<>();

		final Pattern bugMessagePattern = Pattern.compile(String.join("|", bugKeywords), Pattern.CASE_INSENSITIVE);
//		System.out.println("bug Isskey in BFCKEYWORDS: "+ bugMessagePattern);

		for (RevCommit commit : commitList) {
			Matcher bugKeyMatcher = bugMessagePattern.matcher(commit.getShortMessage());
//			System.out.println("bug Isskey in BFCKEYWORDS: "+ bugMessagePattern);
			if (bugKeyMatcher.find()) {
				bfcList.add(commit.getName());
			}
		}
		return bfcList;
	}

}
