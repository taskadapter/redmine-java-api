package com.taskadapter.redmineapi;

public class RedmineInternalError extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public RedmineInternalError() {
    }

    public RedmineInternalError(String message, Throwable cause) {
        super(message, cause);
    }

    public RedmineInternalError(String message) {
        super(message);
    }

    public RedmineInternalError(Throwable cause) {
        super(cause);
    }

}
