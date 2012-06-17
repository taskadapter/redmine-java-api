package com.taskadapter.redmineapi;

public class RedmineFormatException extends RedmineCommunicationException {
	private static final long serialVersionUID = 4024202727798727085L;

	public RedmineFormatException(String message) {
		super(message);
	}

	public RedmineFormatException(Throwable cause) {
        super(cause);
    }

	public RedmineFormatException(String message, Throwable cause) {
		super(message, cause);
	}
}
