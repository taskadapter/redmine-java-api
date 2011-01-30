package org.alskor.redmine;

import java.util.List;

public class RedmineException extends Exception {
	private List<String> errors;

	public RedmineException(List<String> errors) {
		this.errors = errors;
	}

	private static final long serialVersionUID = 1L;

	public List<String> getErrors() {
		return errors;
	}

}
