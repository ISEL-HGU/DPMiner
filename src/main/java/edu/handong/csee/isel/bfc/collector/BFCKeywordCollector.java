package edu.handong.csee.isel.bfc.collector;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jgit.revwalk.RevCommit;

import edu.handong.csee.isel.bfc.BFCCollector;
import edu.handong.csee.isel.data.Input;

public class BFCKeywordCollector extends BFCCollector {
	public String[] bugKeywords;

	public BFCKeywordCollector() {
		super();
		if(Input.issueKeyWord != null) {
			this.bugKeywords = new String[1];
			this.bugKeywords[0]="("+Input.issueKeyWord+")";
		}
		else {
			this.bugKeywords = new String[2];
			this.bugKeywords[0]="(bug)";
			this.bugKeywords[1]="(fix)"; 
		}
	}

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
