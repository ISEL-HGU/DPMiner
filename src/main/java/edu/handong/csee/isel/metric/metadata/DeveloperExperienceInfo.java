package edu.handong.csee.isel.metric.metadata;

import java.util.TreeMap;

public class DeveloperExperienceInfo {
	private TreeMap<Integer, Integer> recentExperiences;
	private int numOfCommits;
	private float REXP;
	
	public DeveloperExperienceInfo() {
		this.recentExperiences = new TreeMap<Integer, Integer>();
		this.numOfCommits = 0;
		this.REXP = (float) 0.0;
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
}
