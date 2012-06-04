package org.redmine.ta.internal.comm;

import org.apache.http.HttpRequest;
import org.redmine.ta.RedmineException;

/**
 * FMap communicator.
 * 
 * @author maxkar
 * 
 */
final class FmapCommunicator<K, I> implements Communicator<K> {

	private final ContentHandler<I, K> handler;
	private final Communicator<I> peer;

	public FmapCommunicator(ContentHandler<I, K> handler, Communicator<I> peer) {
		super();
		this.handler = handler;
		this.peer = peer;
	}

	@Override
	public <R> R sendRequest(HttpRequest request,
			ContentHandler<K, R> contentHandler) throws RedmineException {
		return peer.sendRequest(request,
				Communicators.compose(contentHandler, handler));
	}

}
