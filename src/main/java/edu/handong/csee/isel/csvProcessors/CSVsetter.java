package edu.handong.csee.isel.csvProcessors;

import java.io.File;
import java.util.ArrayList;

import edu.handong.csee.isel.patch.CommitStatus;

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
		
		
		
	}
	
}
