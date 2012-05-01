package org.redmine.ta;

/**
 * Some Redmine configuration is not set properly. E.g. invalid port number is provided to RedmineManager class.
 */
public class RedmineConfigurationException extends RuntimeException {
    public RedmineConfigurationException(String message, NumberFormatException e) {
        super(message);
    }
}
