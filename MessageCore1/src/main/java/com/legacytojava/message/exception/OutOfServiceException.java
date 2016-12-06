package com.legacytojava.message.exception;

public class OutOfServiceException extends Exception {
	
	private static final long serialVersionUID = 2022445049012992320L;

	public OutOfServiceException() {
		super();
	}
	
	public OutOfServiceException(String message) {
		super(message);
	}
	
    public OutOfServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
