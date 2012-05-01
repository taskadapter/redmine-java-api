package org.redmine.ta;

/**
 * covers two cases:
 * <ul>
 *    <li>user or password not recognized</li>
 *    <li>authenticated successfully, but the operation is not permitted for this user</li>
 * </ul>
 */
public class RedmineSecurityException extends RedmineException {
    public RedmineSecurityException(String message) {
        super(message);
    }
}
