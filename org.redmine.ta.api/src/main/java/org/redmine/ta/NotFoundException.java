package org.redmine.ta;

public class NotFoundException extends Exception {

	private static final long serialVersionUID = 1L;

	public NotFoundException(String msg) {
		super(msg);
	}
}