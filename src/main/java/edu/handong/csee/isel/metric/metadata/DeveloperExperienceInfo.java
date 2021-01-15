package edu.handong.csee.isel.metric.metadata;

import java.util.TreeMap;
import java.util.TreeSet;

public class DeveloperExperienceInfo {
	TreeMap<Integer, Integer> recentExperiences;
	int numOfCommits;
	TreeSet<String> numOfSubsystem;
	float REXP;
	
	public DeveloperExperienceInfo() {
		this.recentExperiences = new TreeMap<Integer, Integer>();
		this.numOfCommits = 0;
		this.REXP = (float) 0.0;
		this.numOfSubsystem = new TreeSet<String>();
	}

	public TreeMap<Integer, Integer> getRecentExperiences() {
		return recentExperiences;
	}

	public void setRecentExperiences(TreeMap<Integer, Integer> recentExperiences) {
		this.recentExperiences = recentExperiences;
	}

	public int getNumOfCommits() {
		return numOfCommits;
	}

	public void setNumOfCommits() {
		this.numOfCommits++;
	}

	public float getREXP() {
		return REXP;
	}

	public void setREXP(float rEXP) {
		REXP = rEXP;
	}

	public TreeSet<String> getNumOfSubsystem() {
		return numOfSubsystem;
	}

	public void setNumOfSubsystem(String numOfSubsystem) {
		this.numOfSubsystem.add(numOfSubsystem);
	}
	
}
