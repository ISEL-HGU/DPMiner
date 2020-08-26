package edu.handong.csee.isel.bic.szz.exception;

public class EmptyHunkTypeException extends Exception {

	public EmptyHunkTypeException() {
		this("ERROR - Unknown Hunk Type");
	}

	public EmptyHunkTypeException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	public EmptyHunkTypeException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	public EmptyHunkTypeException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	public EmptyHunkTypeException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		// TODO Auto-generated constructor stub
	}

}
