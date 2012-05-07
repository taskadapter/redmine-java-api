package org.redmine.ta;

public class RedmineException extends Exception {

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
