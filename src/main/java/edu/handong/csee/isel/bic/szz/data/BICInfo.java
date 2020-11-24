package edu.handong.csee.isel.bic.szz.data;

import edu.handong.csee.isel.bic.szz.model.Line;

/**
 * The {@code BICInfo} class<br>
 * the bic information 
 * 
 * @author SJ
 * @author JY
 *
 */
public class BICInfo implements Comparable<BICInfo> {

	private String BISha1;
	private String biPath;
	private String FixSha1;
	private String path;
	private String BIDate;
	private String FixDate;
	private int biLineIdx; // line idx in BI file
	private String BIContent = "";
	private String commiter;
	private String author;

	public BICInfo(String fixSha1, String path, String fixDate, Line line) {
		super();
		this.FixSha1 = fixSha1;
		this.path = path;
		this.FixDate = fixDate;
		this.BISha1 = line.getRev();
		this.biPath = line.getPath();
		this.BIDate = line.getCommitDate();
		this.biLineIdx = line.getIdx();
		this.BIContent = line.getContent();
		this.commiter = line.getCommiter();
		this.author = line.getAuthor();
	}

//	public void setLine(Line line) {
//		this.BISha1 = line.getRev();
//		this.biPath = line.getPath();
//		this.BIDate = line.getCommitDate();
//		this.biLineIdx = line.getIdx();
//		this.BIContent = line.getContent();
//		this.commiter = line.getCommiter();
//		this.author = line.getAuthor();
//	}
	/**
	 * 
	 * @return BISha1 bic commit name
	 */
	public String getBISha1() {
		return BISha1;
	}

	/**
	 * 
	 * @return biPath bic path 
	 */
	public String getBiPath() {
		return biPath;
	}

	/**
	 * 
	 * @return FixSha1 bfc commit name 
	 */
	public String getFixSha1() {
		return FixSha1;
	}

	/** 
	 * 
	 * @return path bfc path
	 */
	public String getPath() {
		return path;
	}

	/**
	 * 
	 * @return BIDate date bug created 
	 */
	public String getBIDate() {
		return BIDate;
	}

	/**
	 * 
	 * @return FixDate bug fixing date
	 */
	public String getFixDate() {
		return FixDate;
	}

	/**
	 * 
	 * @return biLineIdx bug line index
	 */
	public int getBiLineIdx() {
		return biLineIdx;
	}

	/**
	 * 
	 * @return BIContent bug content
	 */
	public String getBIContent() {
		return BIContent;
	}

	/**
	 * 
	 * @return commiter Who made the commit
	 */
	public String getCommiter() {
		return commiter;
	}

	/**
	 * 
	 * @return getAuthor
	 */
	public String getAuthor() {
		return author;
	}

	/**
	 * 
	 * @param compareWith bic information 
	 * @return Whether or not the bug information matches
	 */
	public boolean equals(BICInfo compareWith) {
		if (!BISha1.equals(compareWith.BISha1))
			return false;
		if (!biPath.equals(compareWith.biPath))
			return false;
		if (!path.equals(compareWith.path))
			return false;
		if (!FixSha1.equals(compareWith.FixSha1))
			return false;
		if (!BIDate.equals(compareWith.BIDate))
			return false;
		if (!FixDate.equals(compareWith.FixDate))
			return false;
		if (biLineIdx != compareWith.biLineIdx)
			return false;
		if (!BIContent.equals(compareWith.BIContent))
			return false;
		if (!commiter.equals(compareWith.commiter))
			return false;
		if (!author.equals(compareWith.author))
			return false;

		return true;
	}

	@Override
	public int compareTo(BICInfo o) {
		// order by FixSha1, BISha1, BIContent, biLineIdx
		if (FixSha1.compareTo(o.FixSha1) < 0)
			return -1;
		else if (FixSha1.compareTo(o.FixSha1) > 0)
			return 1;
		else {
			if (BISha1.compareTo(o.BISha1) < 0)
				return -1;
			else if (BISha1.compareTo(o.BISha1) > 0)
				return 1;
			else {
				if (BIContent.compareTo(o.BIContent) < 0)
					return -1;
				else if (BIContent.compareTo(o.BIContent) > 0)
					return 1;
				else {
					if (biLineIdx < o.biLineIdx)
						return -1;
					else if (biLineIdx > o.biLineIdx)
						return 1;
				}
			}
		}

		return 0;
	}
}
