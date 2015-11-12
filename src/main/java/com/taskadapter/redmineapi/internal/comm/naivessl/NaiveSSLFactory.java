package com.taskadapter.redmineapi.internal.comm.naivessl;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.conn.ssl.SSLSocketFactory;


/**
 * Create naive SSLSocket factory which will authorize any TSL/SSL host.
 *
 * @author Bartosz Firyn (SarXos)
 */
public class NaiveSSLFactory {

    /**
     * @return Return naive SSL socket factory (authorize any SSL/TSL host)
     */
    public static SSLSocketFactory createNaiveSSLSocketFactory() {
        X509TrustManager manager = new NaiveX509TrustManager();
        SSLContext sslcontext = null;
        try {
            TrustManager[] managers = new TrustManager[] { manager };
            sslcontext = SSLContext.getInstance("SSL");
            sslcontext.init(null, managers, null);
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            e.printStackTrace();
        }
        return new SSLSocketFactory(sslcontext, SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
    }
}
