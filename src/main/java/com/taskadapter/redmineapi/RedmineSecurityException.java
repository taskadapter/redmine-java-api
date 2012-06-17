package com.taskadapter.redmineapi;

/**
 * covers two cases:
 * <ul>
 *    <li>user or password not recognized</li>
 *    <li>authenticated successfully, but the operation is not permitted for this user</li>
 * </ul>
 */
public class RedmineSecurityException extends RedmineException {
	private static final long serialVersionUID = -7112215624257956273L;

	public RedmineSecurityException(String message) {
        super(message);
    }
}
