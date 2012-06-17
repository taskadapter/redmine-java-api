package com.taskadapter.redmineapi;

public class RedmineTransportException extends RedmineCommunicationException {
	private static final long serialVersionUID = 3463778589975943695L;

	public RedmineTransportException(Throwable cause) {
        super(cause);
    }

	public RedmineTransportException(String message, Throwable cause) {
		super(message, cause);
	}

	public RedmineTransportException(String message) {
		super(message);
	}

}
