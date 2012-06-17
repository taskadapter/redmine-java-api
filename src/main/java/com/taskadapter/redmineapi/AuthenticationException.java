package com.taskadapter.redmineapi;

/**
 * AuthenticationException is thrown when
 * <ul>
 * <li>a user was not authorized (due to invalid or no API access key used when the server requires authorization)
 * <li>OR the user was recognized, but the user permissions do not allow the operation
 * (e.g. trying to create an issue in a project, which the user has no access to).
 * </ul>
 *
 * @author Alexey Skorokhodov
 */
public class AuthenticationException extends Exception {

    private static final long serialVersionUID = 1L;

    public AuthenticationException(String msg) {
        super(msg);
    }
}
