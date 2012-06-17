package com.taskadapter.redmineapi;

/**
 * User or password (or API access key) not recognized.
 */
public class RedmineAuthenticationException extends RedmineSecurityException {
	private static final long serialVersionUID = -2494397318821827279L;

	public RedmineAuthenticationException(String message) {
        super(message);
    }
}
