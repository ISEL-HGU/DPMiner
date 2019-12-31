package edu.handong.csee.isel.bfc.collector;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.eclipse.jgit.revwalk.RevCommit;

import edu.handong.csee.isel.bfc.BFCCollector;
import edu.handong.csee.isel.bfc.collector.github.CommitParser;
import edu.handong.csee.isel.bfc.collector.github.IssueLinkParser;
import edu.handong.csee.isel.bfc.collector.github.NoIssuePagesException;

public class BFCGitHubCollector extends BFCCollector {

	String url;
	String label;

	public BFCGitHubCollector() {
	}

	@Override
	public void setGitHubURL(String url) {
		this.url = url;
	}

	@Override
	public void setGitHubLabel(String label) {
		this.label = label;
	}

	public List<String> collectFrom(List<RevCommit> commitList) {

		try {
			IssueLinkParser iss = new IssueLinkParser();
			CommitParser co = new CommitParser();

			iss.parseIssueAddress(url, label);
			if (IssueLinkParser.issueAddress.size() == 0) {
				throw new NoIssuePagesException("There is no bug issue at " + url);
			}
			co.parseCommitAddress(url);

			HashSet<String> keywordSet = co.getCommitAddress();
			List<String> bfcList = new ArrayList<>(keywordSet);

			return bfcList;
		} catch (Exception e) {
			e.printStackTrace();
			// TODO: Throw new exception
		}
		return null;

	}

}
