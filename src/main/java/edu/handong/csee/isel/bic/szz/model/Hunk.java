package edu.handong.csee.isel.bic.szz.model;

public class Hunk {
	private String hunkType;
	private int beginOfParent;
	private int endOfParent;
	private int beginOfChild;
	private int endOfChild;

	public Hunk(String hunkType, int beginA, int endA, int beginB, int endB) {
		super();
		this.hunkType = hunkType;
		this.beginOfParent = beginA;
		this.endOfParent = endA;
		this.beginOfChild = beginB;
		this.endOfChild = endB;
	}

	public String getHunkType() {
		return hunkType;
	}

	public int getBeginOfParent() {
		return beginOfParent;
	}

	public int getEndOfParent() {
		return endOfParent;
	}

	public int getBeginOfChild() {
		return beginOfChild;
	}

	public int getEndOfChild() {
		return endOfChild;
	}

	public int getRangeOfParent() {
		return Math.abs(this.beginOfParent - this.endOfParent);
	}

	public int getRangeOfChild() {
		return Math.abs(this.beginOfChild - this.endOfChild);
	}
}