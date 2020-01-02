package edu.handong.csee.isel.metric.collector;

import java.io.File;
import java.util.List;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;

public class CharacteristicVectorCollector {
	private Git git;
	private Repository repo;
	private List<String> bfcList;
	private List<RevCommit> commitList;

	private File arff;

	public void collect() {
		//1. make two directories (buggy|clean) 
		
		
		
		//2. fill two directories using jgit
		
		//3. call weka Bag-of-words
	}

	public void setCommitList(List<RevCommit> commitList) {
		this.commitList = commitList;
	}

	public File getArff() {
		return arff;
	}

	public void setGit(Git git) {
		this.git = git;
	}

	public void setRepository(Repository repo) {
		this.repo = repo;
	}

	public void setBFC(List<String> bfcList) {
		this.bfcList = bfcList;
	}
}
