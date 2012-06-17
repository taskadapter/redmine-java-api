package com.taskadapter.redmineapi;

/**
 * Internal redmine error. Should never happen.
 *
 * @author maxkar
 *
 */
public class RedmineInternalError extends Error {

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
