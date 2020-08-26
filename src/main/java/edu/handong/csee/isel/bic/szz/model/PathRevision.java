package edu.handong.csee.isel.bic.szz.model;

import org.eclipse.jgit.revwalk.RevCommit;

public class PathRevision {
	private String path;
	private RevCommit commit;

	public PathRevision() {

	}

	public PathRevision(String path, RevCommit commit) {
		super();
		this.path = path;
		this.commit = commit;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public RevCommit getCommit() {
		return commit;
	}

	public void setCommit(RevCommit commit) {
		this.commit = commit;
	}

}
