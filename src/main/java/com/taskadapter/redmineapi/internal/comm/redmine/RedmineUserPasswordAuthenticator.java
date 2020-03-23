package com.taskadapter.redmineapi.internal.comm.redmine;

import com.taskadapter.redmineapi.RedmineException;
import com.taskadapter.redmineapi.RedmineInternalError;
import com.taskadapter.redmineapi.internal.comm.Communicator;
import com.taskadapter.redmineapi.internal.comm.ContentHandler;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpRequest;

import java.io.UnsupportedEncodingException;

public class RedmineUserPasswordAuthenticator<K> implements Communicator<K> {
	/**
	 * Header value.
	 */
	private String authKey;

	/**
	 * Used charset.
	 */
	private final String charset;

	/**
	 * Peer communicator.
	 */
	private final Communicator<K> peer;

	public RedmineUserPasswordAuthenticator(Communicator<K> peer, String charset, String login, String password) {
		this.peer = peer;
		this.charset = charset;
		setCredentials(login, password);
	}

	public void setCredentials(String login, String password) {
		if (login == null) {
			authKey = null;
			return;
		}
		try {
			authKey = "Basic "
					+ Base64.encodeBase64String(
							(login + ':' + password).getBytes(charset)).trim();
		} catch (UnsupportedEncodingException e) {
			throw new RedmineInternalError(e);
		}
	}

	@Override
	public <R> R sendRequest(HttpRequest request, ContentHandler<K, R> handler)
			throws RedmineException {
		if (authKey != null)
			request.addHeader("Authorization", authKey);
		return peer.sendRequest(request, handler);
	}

}
