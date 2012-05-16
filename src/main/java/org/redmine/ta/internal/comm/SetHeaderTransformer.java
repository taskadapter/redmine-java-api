package org.redmine.ta.internal.comm;

import org.apache.http.HttpRequest;
import org.redmine.ta.RedmineException;

/**
 * "Set header" communication transformer.
 * 
 * @author maxkar
 * 
 */
final class SetHeaderTransformer implements Communicator {
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
	private final Communicator peer;

	public SetHeaderTransformer(String header, String value, Communicator peer) {
		this.header = header;
		this.value = value;
		this.peer = peer;
	}

	@Override
	public String sendRequest(HttpRequest request) throws RedmineException {
		request.addHeader(header, value);
		return peer.sendRequest(request);
	}

}
