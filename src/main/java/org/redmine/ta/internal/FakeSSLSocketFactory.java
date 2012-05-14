package org.redmine.ta.internal;

import java.io.IOException;
import java.net.Socket;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.conn.ssl.SSLSocketFactory;

class FakeSSLSocketFactory extends SSLSocketFactory {
    private final SSLContext sslContext = SSLContext.getInstance("TLS");

    public FakeSSLSocketFactory(KeyStore truststore)
            throws NoSuchAlgorithmException, KeyManagementException,
            KeyStoreException, UnrecoverableKeyException {
        super(truststore);

        TrustManager tm = new X509TrustManager() {
            public void checkClientTrusted(X509Certificate[] chain,
                                           String authType) throws CertificateException {
				// Check always succeed
            }

            public void checkServerTrusted(X509Certificate[] chain,
                                           String authType) throws CertificateException {
				// Check always succeed
            }

            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }
        };

        sslContext.init(null, new TrustManager[]{tm}, null);
    }

    @Override
    public Socket createSocket(Socket socket, String host, int port,
                               boolean autoClose) throws IOException {
        return sslContext.getSocketFactory().createSocket(socket, host, port,
                autoClose);
    }

    @Override
    public Socket createSocket() throws IOException {
        return sslContext.getSocketFactory().createSocket();
    }

}
