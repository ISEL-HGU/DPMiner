package edu.handong.csee.isel.bfc;

import java.util.List;

import org.eclipse.jgit.revwalk.RevCommit;

public abstract class BFCCollector {

	public List<String> collectFrom(List<RevCommit> commitList) {
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
