package edu.handong.csee.isel.utils;

public class BIChange {
	public String BIShal1;
	public String BIpath;
	public String Fixpath;
	public String FixShal1;
	public int numLineBIC;
	public int numLinePreFix;
	public String content;
	public BIChange(String bIShal1, String oldPath, String newPath, String fixShal1, int numLineBIC, int numLinePreFix,
			String content) {
		super();
		this.BIShal1 = bIShal1;
		this.BIpath = oldPath;
		this.Fixpath = newPath;
		this.FixShal1 = fixShal1;
		this.numLineBIC = numLineBIC;
		this.numLinePreFix = numLinePreFix;
		this.content = content;
	}
	@Override
	public String toString() {
		return "BIShal1=" + BIShal1 + ", BIpath=" + BIpath + ", Fixpath=" + Fixpath + ", FixShal1=" + FixShal1
				+ ", numLineBIC=" + numLineBIC + ", numLinePreFix=" + numLinePreFix + ", content=" + content;
	}
}
