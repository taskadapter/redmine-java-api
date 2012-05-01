package org.redmine.ta;

/**
 * User or password (or API access key) not recognized.
 */
public class RedmineAuthenticationException extends RedmineSecurityException {
    public RedmineAuthenticationException(String message) {
        super(message);
    }
}
