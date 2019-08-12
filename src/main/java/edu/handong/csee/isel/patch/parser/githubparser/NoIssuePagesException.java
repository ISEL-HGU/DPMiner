package edu.handong.csee.isel.patch.parser.githubparser;

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
