package edu.handong.csee.isel.bfc.collector;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.eclipse.jgit.revwalk.RevCommit;

import edu.handong.csee.isel.bfc.BFCCollectable;
import edu.handong.csee.isel.bfc.collector.github.CommitParser;
import edu.handong.csee.isel.bfc.collector.github.IssueLinkParser;
import edu.handong.csee.isel.bfc.collector.github.NoIssuePagesException;

/**
 * This class is Collector to find BUG FIX COMMITS using label in Github.
 */
public class BFCGitHubCollector implements BFCCollectable {

	private String gitURL;
	private String label;

	public BFCGitHubCollector(String gitURL, String label) {
		this.gitURL = gitURL;
		this.label = label;
	}

	/**
	 * Github Issues use labels to manage Bug issues.
	 * Since the bug label name is different for each repository,
	 * it is a method to collect the commits in the closed issue with the input label name.
	 * @param commitList
	 * @return bfcList
	 */
	@Override
	public List<String> collectFrom(List<RevCommit> commitList) {

		try {
			IssueLinkParser iss = new IssueLinkParser();
			CommitParser co = new CommitParser();

			iss.parseIssueAddress(gitURL, label);
			if (IssueLinkParser.issueAddress.size() == 0) {
				throw new NoIssuePagesException("There is no bug issue at " + gitURL);
			}
			co.parseCommitAddress(gitURL);

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
