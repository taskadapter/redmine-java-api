package org.redmine.ta.internal.comm;

import org.apache.http.HttpRequest;
import org.redmine.ta.RedmineException;

public interface Communicator<K> {

	/**
	 * Performs a request.
	 * 
	 * @return the response body.
	 */
	public abstract <R> R sendRequest(HttpRequest request,
			ContentHandler<K, R> contentHandler) throws RedmineException;

}