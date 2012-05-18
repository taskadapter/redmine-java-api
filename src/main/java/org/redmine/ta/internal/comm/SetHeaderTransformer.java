package org.redmine.ta.internal.comm;

import org.apache.http.HttpRequest;
import org.redmine.ta.RedmineException;

/**
 * "Set header" communication transformer.
 * 
 * @author maxkar
 * 
 */
final class SetHeaderTransformer<K> implements Communicator<K> {
	/**
	 * Header name.
	 */
	private final String header;

	/**
	 * Header value.
	 */
	private final String value;

	/**
	 * Peer communicator.
	 */
	private final Communicator<K> peer;

	public SetHeaderTransformer(String header, String value,
			Communicator<K> peer) {
		this.header = header;
		this.value = value;
		this.peer = peer;
	}

	@Override
	public <R> R sendRequest(HttpRequest request, ContentHandler<K, R> handler)
			throws RedmineException {
		request.addHeader(header, value);
		return peer.sendRequest(request, handler);
	}

}
