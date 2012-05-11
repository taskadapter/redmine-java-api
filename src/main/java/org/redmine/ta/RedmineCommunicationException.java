package org.redmine.ta;

/**
 * Some I/O error
 */
public class RedmineCommunicationException extends RedmineException {
	private static final long serialVersionUID = 8270275922987093576L;

	public RedmineCommunicationException(Throwable cause) {
        super(cause);
    }
}
