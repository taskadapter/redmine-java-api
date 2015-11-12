package com.taskadapter.redmineapi.internal.comm.betterssl;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.net.ssl.X509TrustManager;

/**
 * Trust manager which trusts a host when at least one peer trusts the target.
 */
final class CompositeTrustManager implements X509TrustManager {
	
	/** Peers to delegate to. */
	private final Collection<X509TrustManager> peers;
	
	/** All accepted issuers. */
	private final X509Certificate[] allCerts;

	/** 
	 * Creates a new composite manager.
	 * @param peers peers to delegate to.
	 */
	CompositeTrustManager(Collection<X509TrustManager> peers) {
		this.peers = peers;
		final List<X509Certificate> certs = new ArrayList<>();
		for (X509TrustManager peer: peers) {
			certs.addAll(Arrays.asList(peer.getAcceptedIssuers()));
		}
		this.allCerts = certs.toArray(new X509Certificate[certs.size()]);
	}

	@Override
	public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
		for (X509TrustManager peer : peers) {
			try {
				peer.checkClientTrusted(chain, authType);
				return;
			} catch (CertificateException e) {
				//Let other manager to check this.
			}
		}
		throw new CertificateException("Could not authenticate client, nobody trusts it.");
	}

	@Override
	public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
		for (X509TrustManager peer : peers) {
			try {
				peer.checkServerTrusted(chain, authType);
				return;
			} catch (CertificateException e) {
				//Let other manager to check this.
			}
		}
		throw new CertificateException("Could not authenticate server, nobody trusts it.");
	}

	@Override
	public X509Certificate[] getAcceptedIssuers() {
		return allCerts;
	}

}
