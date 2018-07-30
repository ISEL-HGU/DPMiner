package edu.handong.csee.isel.csvProcessors;

import java.io.File;
import java.util.ArrayList;

import edu.handong.csee.isel.patch.CommitStatus;

/*
 * String project;
	String shortMessage;
	String commitHash;
	int date;
	String Author;
	ArrayList<String> pathes;*/

public class CSVsetter {
	public void set(File newFile) {
		this.newFile = newFile;
	}

	public CSVsetter(File newFile) {
		this.newFile = newFile;
	}

	File newFile;

	public void makeCSVfromCommits(ArrayList<CommitStatus> commits) {
		File folder = newFile.getParentFile();
		if (!folder.exists()) {
			folder.mkdirs();
		}
		
		for(CommitStatus commit : commits) {
			String project = commit.getProject();
			String commitHash = commit.getShortMessage();
			int date = commit.getDate();
			String author = commit.getAuthor();
			ArrayList<String> patches = commit.getPathes();
			
			/* csv 만드는 로직~. */
		}
		
	}

}
