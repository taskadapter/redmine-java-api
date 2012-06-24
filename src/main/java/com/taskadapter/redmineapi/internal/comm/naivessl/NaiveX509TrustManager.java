package com.taskadapter.redmineapi.internal.comm.naivessl;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.X509TrustManager;


/**
 * The goal of this trust manager is to do nothing - it will authorize
 * any TSL/SSL secure connection.
 *
 * @author Bartosz Firyn (SarXos)
 */
public class NaiveX509TrustManager implements X509TrustManager {

    @Override
    public void checkClientTrusted(X509Certificate[] certs, String str) throws CertificateException {
    }

    @Override
    public void checkServerTrusted(X509Certificate[] certs, String str) throws CertificateException {
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return null;
    }
}
