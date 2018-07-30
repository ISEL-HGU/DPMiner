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
	public ArrayList<String> getPathes() {
		return pathes;
	}
	public CommitStatus(String project, String shortMessage, String commitHash, int date, String author,
			ArrayList<String> pathes) {
		this.project = project;
		this.shortMessage = shortMessage;
		this.commitHash = commitHash;
		this.date = date;
		Author = author;
		this.pathes = pathes;
	}
	String project;
	String shortMessage;
	String commitHash;
	int date;
	String Author;
	ArrayList<String> pathes;
	
	
	
}
