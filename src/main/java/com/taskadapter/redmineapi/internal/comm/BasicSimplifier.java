package com.taskadapter.redmineapi.internal.comm;

import org.apache.http.HttpRequest;
import com.taskadapter.redmineapi.RedmineException;

/**
 * Basic transport simplifier.
 * 
 * @author maxkar
 * 
 */
final class BasicSimplifier<K, T> implements SimpleCommunicator<K> {
	private final ContentHandler<T, K> contentHandler;
	private final Communicator<T> peer;

	public BasicSimplifier(ContentHandler<T, K> contentHandler,
			Communicator<T> peer) {
		this.contentHandler = contentHandler;
		this.peer = peer;
	}

	@Override
	public K sendRequest(HttpRequest request) throws RedmineException {
		return peer.sendRequest(request, contentHandler);
	}

}
