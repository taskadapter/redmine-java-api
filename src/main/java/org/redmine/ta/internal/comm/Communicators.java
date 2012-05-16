package org.redmine.ta.internal.comm;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.commons.codec.binary.Base64;
import org.redmine.ta.RedmineInternalError;

/**
 * Communicator utilities.
 * 
 * @author maxkar
 * 
 */
public final class Communicators {
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
	public static Communicator addBasicAuth(String login, String password,
			String charset, Communicator peer) {
		final String credentials;
		try {
			credentials = "\""
					+ Base64.encodeBase64String(
							(login + ':' + password).getBytes(charset)).trim()
					+ "\"";
		} catch (UnsupportedEncodingException e) {
			throw new RedmineInternalError(e);
		}
		return new SetHeaderTransformer("Authorization", "Basic: "
				+ credentials, peer);

	}
}
