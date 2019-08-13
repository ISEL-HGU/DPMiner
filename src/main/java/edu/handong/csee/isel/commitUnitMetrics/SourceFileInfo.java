package edu.handong.csee.isel.commitUnitMetrics;

import java.util.TreeSet;

public class SourceFileInfo {
	
	TreeSet<String> developer;
	String modifiedDate;
	int numOfModify;
	
	public SourceFileInfo() {
		this.developer = new TreeSet<String>();
		this.numOfModify = 0;
		this.modifiedDate = null;
	}
	public TreeSet<String> getDeveloper() {
		return developer;
	}
	public void setDeveloper(String developer) {
		this.developer.add(developer);
	}
	public int getNumOfModify() {
		return numOfModify;
	}
	public void setNumOfModify() {
		this.numOfModify++;
	}
	public String getModifiedDate() {
		return modifiedDate;
	}
	public void setModifiedDate(String modifiedDate) {
		this.modifiedDate = modifiedDate;
	}
	

}
