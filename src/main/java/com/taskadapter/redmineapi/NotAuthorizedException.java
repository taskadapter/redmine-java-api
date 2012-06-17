package com.taskadapter.redmineapi;

/**
 * The user was authenticated successfully, but it does not have required privileges for the operation.
 * E.g. trying to create an issue in a project, which the user has no access to.
 */
public class NotAuthorizedException extends RedmineSecurityException {
	private static final long serialVersionUID = -6810250160379994395L;

	public NotAuthorizedException(String message) {
        super(message);
    }
}
