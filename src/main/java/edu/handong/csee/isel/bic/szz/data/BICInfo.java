package edu.handong.csee.isel.bic.szz.data;

import edu.handong.csee.isel.bic.szz.model.Line;

public class BICInfo implements Comparable<BICInfo> {

	String BISha1;
	String biPath;
	String FixSha1;
	String path;
	String BIDate;
	String FixDate;
	int biLineIdx; // line idx in BI file
	String BIContent = "";
	String commiter;
	String author;

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

	public String getBISha1() {
		return BISha1;
	}

	public String getBiPath() {
		return biPath;
	}

	public String getFixSha1() {
		return FixSha1;
	}

	public String getPath() {
		return path;
	}

	public String getBIDate() {
		return BIDate;
	}

	public String getFixDate() {
		return FixDate;
	}

	public int getBiLineIdx() {
		return biLineIdx;
	}

	public String getBIContent() {
		return BIContent;
	}

	public String getCommiter() {
		return commiter;
	}

	public String getAuthor() {
		return author;
	}

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

		// order by BIDate, path, FixDate, lineNum
//		if (BIDate.compareTo(o.BIDate) < 0)
//			return -1;
//		else if (BIDate.compareTo(o.BIDate) > 0)
//			return 1;
//		else {
//			if (path.compareTo(o.path) < 0)
//				return -1;
//			else if (path.compareTo(o.path) > 0)
//				return 1;
//			else {
//				if (FixDate.compareTo(o.FixDate) < 0)
//					return -1;
//				else if (FixDate.compareTo(o.FixDate) > 0)
//					return 1;
//				else {
//					if (biLineIdx < o.biLineIdx)
//						return -1;
//					else if (biLineIdx > o.biLineIdx)
//						return 1;
//				}
//			}
//		}

		return 0;
	}
}
