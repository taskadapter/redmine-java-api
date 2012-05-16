package org.redmine.ta.internal.comm;

import org.apache.http.HttpRequest;
import org.redmine.ta.RedmineException;

public interface Communicator {

	/**
	 * Performs a request.
	 * 
	 * @return the response body.
	 */
	public abstract String sendRequest(HttpRequest request)
			throws RedmineException;

}