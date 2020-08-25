package edu.handong.csee.isel.bic.szz.model;

public enum LineType {
	INSERT, DELETE, REPLACE, CONTEXT;

	public String toString() {
		switch (this) {
		case INSERT:
			return "INSERT";
		case DELETE:
			return "DELETE";
		case REPLACE:
			return "REPLACE";
		case CONTEXT:
			return "CONTEXT";
		default:
			throw new IllegalArgumentException();
		}
	}

}
