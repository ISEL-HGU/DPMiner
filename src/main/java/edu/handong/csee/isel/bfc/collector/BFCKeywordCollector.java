package edu.handong.csee.isel.bfc.collector;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jgit.revwalk.RevCommit;

import edu.handong.csee.isel.bfc.BFCCollectable;

/**
 *  This class is Collector to find BUG FIX COMMITS by using Github commits message.
 *
 */
public class BFCKeywordCollector implements BFCCollectable {
	private String[] bugKeywords;

	/**
	 * Github Commit message에서 찾을 keyword를 설정해 주는 메소드 이다.
	 * null일 경우 "bug" 와 "fix"를 기본 keyword로 설정한다.
	 * null이 아닐 경우, 받은 String을 Keyword로 설정한다.
	 *
	 * @param issueKeyWord
	 */
	public BFCKeywordCollector(String issueKeyWord) {
		super();
		if(issueKeyWord != null) {
			this.bugKeywords = new String[1];
			this.bugKeywords[0]="("+issueKeyWord+")";
		}
		else {
			this.bugKeywords = new String[2];
			this.bugKeywords[0]="(bug)";
			this.bugKeywords[1]="(fix)"; 
		}
	}

	/**
	 * Github commit message에서 해당 Keyword가 포함된 Commits을 수집하는 method이다.
	 *
	 * @param commitList
	 * @return bfcList
	 */
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
