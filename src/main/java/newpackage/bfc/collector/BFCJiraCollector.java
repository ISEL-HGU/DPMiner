package newpackage.bfc.collector;

import java.util.List;

import org.eclipse.jgit.revwalk.RevCommit;

import newpackage.bfc.BFCCollector;

public class BFCJiraCollector implements BFCCollector {

	public BFCJiraCollector(List<String> bugIssueKeys) {
		// TODO Auto-generated constructor stub
	}

	@Override
	public List<String> collectFrom(List<RevCommit> commitList) {
		return null;
	}

}
