package edu.handong.csee.isel.jira.crawler;

@SuppressWarnings("serial")
public class InvalidDomainException extends Exception{

	public InvalidDomainException() {
		this("Domain is invalid.");
	}

	public InvalidDomainException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		// TODO Auto-generated constructor stub
	}

	public InvalidDomainException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	public InvalidDomainException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	public InvalidDomainException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

}
