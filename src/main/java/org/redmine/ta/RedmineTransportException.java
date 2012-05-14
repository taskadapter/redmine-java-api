package org.redmine.ta;

public class RedmineTransportException extends RedmineCommunicationException {
	private static final long serialVersionUID = 3463778589975943695L;

	public RedmineTransportException(Throwable cause) {
        super(cause);
    }
}
