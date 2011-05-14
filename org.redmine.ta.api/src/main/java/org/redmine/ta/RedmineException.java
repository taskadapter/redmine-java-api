package org.redmine.ta;

import java.util.List;

public class RedmineException extends Exception {
	private List<String> errors;
	private String text = "";

	public RedmineException(List<String> errors) {
		this.errors = errors;
		for(String s : errors) {
			text += s + "\n";
		}
	}

	private static final long serialVersionUID = 1L;

	public List<String> getErrors() {
		return errors;
	}
	
	@Override
	public String getMessage(){
		return text;
	}
}
