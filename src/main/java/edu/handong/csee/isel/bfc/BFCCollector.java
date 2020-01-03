package edu.handong.csee.isel.bfc;

import java.io.IOException;
import java.util.List;

import org.eclipse.jgit.revwalk.RevCommit;

import edu.handong.csee.isel.bfc.collector.jira.InvalidDomainException;
import edu.handong.csee.isel.bfc.collector.jira.InvalidProjectKeyException;

public abstract class BFCCollector {

	public List<String> collectFrom(List<RevCommit> commitList) throws IOException, InvalidProjectKeyException, InvalidDomainException {
		return null;
	}

	public void setJiraProjectKey(String key) {
		
	}
	
	public void setJiraURL(String url) {
		
	}

	public void setGitHubURL(String url) {
		
	}
	
	public void setGitHubLabel(String label ) {
		
	}

	public void setOutPath(String outPath) {
		
	}
}
