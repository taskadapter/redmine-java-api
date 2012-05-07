package org.redmine.ta;

/**
 * The user was authenticated successfully, but it does not have required privileges for the operation.
 * E.g. trying to create an issue in a project, which the user has no access to.
 */
public class NotAuthorizedException extends RedmineSecurityException {
    public NotAuthorizedException(String message) {
        super(message);
    }
}
