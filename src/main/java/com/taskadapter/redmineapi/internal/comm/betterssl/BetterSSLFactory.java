package com.taskadapter.redmineapi.internal.comm.betterssl;

import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.UnrecoverableKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.X509TrustManager;
import org.apache.http.conn.ssl.SSLSocketFactory;

/**
 * SSL Socket factory. Provides more authentication than the naive one and
 * allows stored (custom) certificates to be added into the trust chain. The
 * ability to also use Client Certificates is provided by the alternative constructor
 * <p>
 * This work is based on
 * http://codyaray.com/2013/04/java-ssl-with-multiple-keystores and common
 * sense.
 */
public class BetterSSLFactory {
	/**
	 * Creates a new SSL socket factory which supports both system-installed
	 * keys and all additional keys in the provided keystores.
	 *
	 * @param extraStores
	 *            extra keystores containing root certificate authorities.
	 * @return Socket factory supporting authorization for both system (default)
	 *         keystores and all the extraStores.
	 * @throws KeyStoreException if key store have problems.
	 * @throws KeyManagementException if new SSL context could not be initialized.
	 */
	public static SSLSocketFactory createSocketFactory(Collection<KeyStore> extraStores) throws KeyStoreException, KeyManagementException {
		final Collection<X509TrustManager> managers = new ArrayList<>();
		for (KeyStore ks : extraStores) {
			addX509Managers(managers, ks);
		}
		/* Add default manager. */
		addX509Managers(managers, null);
		final TrustManager tm = new CompositeTrustManager(managers);
		
		try {
			final SSLContext ctx = SSLContext.getInstance("SSL");
			ctx.init(null, new TrustManager[] {tm}, null);
			return new SSLSocketFactory(ctx);
		} catch (NoSuchAlgorithmException e) {
			throw new Error("No SSL protocols supported :(", e);
		}
	}

	/**
	 * Creates a new SSL socket factory which supports client certificate
	 * authentication, and validation against both system-installed
	 * keys and all additional keys in the provided keystores.
	 *
	 * @param keystore KeyStore object containing a Private and Public Key pair
	 *            for client authentication.
	 * @param keystorePassword Password used to unlock keystore.
	 * @param extraStores
	 *            extra keystores containing root certificate authorities.
	 * @return Socket factory supporting authorization for both system (default)
	 *         keystores and all the extraStores.
	 * @throws KeyStoreException if key store have problems.
	 * @throws KeyManagementException if new SSL context could not be initialized.
	 */
	public static SSLSocketFactory createSocketFactory(KeyStore keystore, String keystorePassword, Collection<KeyStore> extraStores) throws KeyStoreException, KeyManagementException {
		final Collection<X509TrustManager> managers = new ArrayList<>();

		for (KeyStore ks : extraStores) {
			addX509Managers(managers, ks);
		}
		/* Add default manager. */
		addX509Managers(managers, null);
		final TrustManager tm = new CompositeTrustManager(managers);

		try {
			final KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			kmf.init(keystore, keystorePassword.toCharArray());
			final SSLContext ctx = SSLContext.getInstance("TLS");
			ctx.init(kmf.getKeyManagers(), new TrustManager[] {tm}, new SecureRandom());
			return new SSLSocketFactory(ctx);
		} catch (NoSuchAlgorithmException e) {
					throw new Error("No SSL protocols supported :(", e);
		} catch (UnrecoverableKeyException e) {
					throw new Error("Could not load RSA Key!", e);
		}
	}


	/**
	 * Adds X509 keystore-backed trust manager into the list of managers.
	 * @param managers list of the managers to add to.
	 * @param ks key store with target keys.
	 * @throws KeyStoreException if key store could not be accessed.
	 */
	private static void addX509Managers(final Collection<X509TrustManager> managers, KeyStore ks)
			throws KeyStoreException, Error {
		try {
			final TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
			tmf.init(ks);
			for (TrustManager tm : tmf.getTrustManagers()) {
				if (tm instanceof X509TrustManager) {
					managers.add((X509TrustManager) tm);
				}
			}
		} catch (NoSuchAlgorithmException e) {
			throw new Error("Default trust manager algorithm is not supported!", e);
		}
	}
}
