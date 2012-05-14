package org.redmine.ta.internal.json;

/**
 * Json format exception.
 * 
 * @author maxkar
 * 
 */
public class JsonFormatException extends Exception {
	private static final long serialVersionUID = 1L;

	public JsonFormatException() {
		super();
	}

	public JsonFormatException(String message) {
		super(message);
	}

	public JsonFormatException(Throwable cause) {
		super(cause);
	}

	public JsonFormatException(String message, Throwable cause) {
		super(message, cause);
	}
}
