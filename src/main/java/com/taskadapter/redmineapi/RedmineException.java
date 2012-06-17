package com.taskadapter.redmineapi;

public class RedmineException extends Exception {
	private static final long serialVersionUID = -1592189045756043062L;

	public RedmineException() {
    }

    public RedmineException(String message, Throwable cause) {
        super(message, cause);
    }

    public RedmineException(String message) {
        super(message);
    }

    public RedmineException(Throwable cause) {
        super(cause);
    }
}
