package com.taskadapter.redmineapi.internal.comm;

import org.apache.http.HttpRequest;
import com.taskadapter.redmineapi.RedmineException;

public interface SimpleCommunicator<T> {
	/**
	 * Performs a request.
	 * 
	 * @return the response body.
	 */
	T sendRequest(HttpRequest request) throws RedmineException;

}
