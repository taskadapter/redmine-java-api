package org.redmine.ta.internal.comm;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpEntity;
import org.redmine.ta.RedmineException;
import org.redmine.ta.RedmineInternalError;

/**
 * Communicator utilities.
 * 
 * @author maxkar
 * 
 */
public final class Communicators {
	private static final ContentHandler<Object, Object> IDENTITY_HANDLER = new ContentHandler<Object, Object>() {
		@Override
		public Object processContent(Object content) throws RedmineException {
			return content;
		}
	};

	private static final ContentHandler<HttpEntity, String> CONTENT_READER = new EntityToStringHandler();

	/**
	 * Adds a basic authentication.
	 * 
	 * @param login
	 *            user login.
	 * @param password
	 *            user password.
	 * @param charset
	 *            communication charset.
	 * @param peer
	 *            peer communicator (used for request marshalling).
	 * @return communicator with basic authentication.
	 * @throws IOException
	 *             if something goes wrong.
	 */
	public static <K> Communicator<K> addBasicAuth(String login,
			String password, String charset, Communicator<K> peer) {
		final String credentials;
		try {
			credentials = "\""
					+ Base64.encodeBase64String(
							(login + ':' + password).getBytes(charset)).trim()
					+ "\"";
		} catch (UnsupportedEncodingException e) {
			throw new RedmineInternalError(e);
		}
		return new SetHeaderTransformer<K>("Authorization", "Basic: "
				+ credentials, peer);
	}

	public static <K, V> SimpleCommunicator<V> simplify(
			Communicator<K> communicator, ContentHandler<K, V> handler) {
		return new BasicSimplifier<V, K>(handler, communicator);
	}

	@SuppressWarnings("unchecked")
	public static <K> ContentHandler<K, K> identityHandler() {
		return (ContentHandler<K, K>) IDENTITY_HANDLER;
	}

	public static ContentHandler<HttpEntity, String> contentReader() {
		return CONTENT_READER;
	}

	public static <K, I, R> ContentHandler<K, R> compose(
			ContentHandler<I, R> cont1, ContentHandler<K, I> cont2) {
		return new ComposingHandler<K, I, R>(cont1, cont2);
	}

	public static <K, R> Communicator<R> fmap(Communicator<K> comm,
			ContentHandler<K, R> handler) {
		return new FmapCommunicator<R, K>(handler, comm);
	}
}
