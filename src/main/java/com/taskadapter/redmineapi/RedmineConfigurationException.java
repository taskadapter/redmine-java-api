package com.taskadapter.redmineapi;

/**
 * Some Redmine configuration is not set properly. E.g. invalid port number is provided to RedmineManager class.
 */
public class RedmineConfigurationException extends RuntimeException {
	private static final long serialVersionUID = 5935193308676164988L;

	public RedmineConfigurationException(String message, NumberFormatException e) {
        super(message);
    }
}
