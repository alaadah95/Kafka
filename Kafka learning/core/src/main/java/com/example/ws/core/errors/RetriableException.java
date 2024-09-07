package com.example.ws.core.errors;

public class RetriableException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public RetriableException() {
		super();
	}

	public RetriableException(String message) {
		super(message);
	}

}
