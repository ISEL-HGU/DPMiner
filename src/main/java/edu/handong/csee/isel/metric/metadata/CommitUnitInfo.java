package edu.handong.csee.isel.metric.metadata;

import java.util.ArrayList;
import java.util.TreeSet;

public class CommitUnitInfo {
	ArrayList<String> key;
	String authorId;
	TreeSet<String> subsystems;
	TreeSet<String> directories;
	TreeSet<String> files;
	TreeSet<String> previousCommitHashs;
	ArrayList<Integer> modifiedLines;
	double Entropy;
	
	
	public CommitUnitInfo() {
		this.key = new ArrayList<String>();
		this.authorId = null;
		this.subsystems = new TreeSet<String>();
		this.directories = new TreeSet<String>();
		this.files = new TreeSet<String>();
		this.previousCommitHashs = new TreeSet<String>();
		this.modifiedLines = new ArrayList<Integer>();
		this.Entropy = 0.0;
	}
	
	public ArrayList<String> getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key.add(key);
	}
	public String getAuthorId() {
		return authorId;
	}
	public void setAuthorId(String authorId) {
		this.authorId = authorId;
	}
	public TreeSet<String> getSubsystems() {
		return subsystems;
	}
	public void setSubsystems(String subsystem) {
		this.subsystems.add(subsystem);
	}
	public TreeSet<String> getDirectories() {
		return directories;
	}
	public void setDirectories(String directorie) {
		this.directories.add(directorie);
	}
	public TreeSet<String> getFiles() {
		return files;
	}
	public void setFiles(String file) {
		this.files.add(file);
	}

	public TreeSet<String> getPreviousCommitHashs() {
		return previousCommitHashs;
	}
	public void setPreviousCommitHashs(String previousCommitHash) {
		this.previousCommitHashs.add(previousCommitHash);
	}
	public ArrayList<Integer> getModifiedLines() {
		return modifiedLines;
	}

	public void setModifiedLines(int modifiedLine) {
		this.modifiedLines.add(modifiedLine);
	}

	public double getEntropy() {
		return Entropy;
	}

	public void setEntropy(double entropy) {
		Entropy = entropy;
	}

}
