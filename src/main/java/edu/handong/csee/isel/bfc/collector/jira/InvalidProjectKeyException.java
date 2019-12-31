package edu.handong.csee.isel.bfc.collector.jira;

@SuppressWarnings("serial")
public class InvalidProjectKeyException extends Exception{

	public InvalidProjectKeyException() {
		this("Project Key is invalid");
	}

	public InvalidProjectKeyException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		// TODO Auto-generated constructor stub
	}

	public InvalidProjectKeyException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	public InvalidProjectKeyException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	public InvalidProjectKeyException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

}
