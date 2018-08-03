package edu.handong.csee.isel.patch;

import java.util.ArrayList;

public class CommitStatus {
	public String getProject() {
		return project;
	}
	public String getShortMessage() {
		return shortMessage;
	}
	public String getCommitHash() {
		return commitHash;
	}
	public int getDate() {
		return date;
	}
	public String getAuthor() {
		return Author;
	}
	public String getPatch() {
		return patch;
	}
	public String getPath() {
		return path;
	}
	public CommitStatus(String project, String shortMessage, String commitHash, int date, String author,
			String path,String patch) {
		this.project = project;
		this.shortMessage = shortMessage;
		this.commitHash = commitHash;
		this.date = date;
		Author = author;
		this.patch = patch;
	}
	String project;
	String shortMessage;
	String commitHash;
	int date;
	String Author;
	String path;
	String patch;
}
