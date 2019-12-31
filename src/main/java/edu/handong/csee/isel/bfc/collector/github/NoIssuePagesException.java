package edu.handong.csee.isel.bfc.collector.github;

public class NoIssuePagesException extends Exception {
	String message;

	public NoIssuePagesException(String message) {
		this.message = message;
	}

	@Override
	public String getMessage() {
		return message;
	}

}
