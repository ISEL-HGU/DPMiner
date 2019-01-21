package edu.handong.csee.isel.newpackage;

public class NoIssuePagesException extends Exception {
	String message;
	
	public NoIssuePagesException(String message) {
		this.message = message;
	}

}
