package edu.handong.csee.isel.utils;

public class BIChange {
	String BIShal1;
	String prePath;
	String newPath;
	String FixShal1;
	int numLineBIC;
	int numLinePreFix;
	String content;
	public BIChange(String bIShal1, String prePath, String newPath, String fixShal1, int numLineBIC, int numLinePreFix,
			String content) {
		super();
		this.BIShal1 = bIShal1;
		this.prePath = prePath;
		this.newPath = newPath;
		this.FixShal1 = fixShal1;
		this.numLineBIC = numLineBIC;
		this.numLinePreFix = numLinePreFix;
		this.content = content;
	}
}
