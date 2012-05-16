package org.redmine.ta.internal;

import java.security.KeyStore;

import org.apache.http.HttpHost;
import org.apache.http.HttpVersion;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.ContentEncodingHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.redmine.ta.RedmineConfigurationException;

class HttpUtil {
	@SuppressWarnings("deprecation")
	public static DefaultHttpClient getNewHttpClient(int maxConnections) {
        try {
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null, null);

            SSLSocketFactory sf = new FakeSSLSocketFactory(trustStore);
            sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

            HttpParams params = new BasicHttpParams();
            HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
            HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);

            SchemeRegistry registry = new SchemeRegistry();
            registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
            registry.register(new Scheme("https", sf, 443));

			ThreadSafeClientConnManager ccm = new ThreadSafeClientConnManager(
					params, registry);
			ccm.setMaxTotal(maxConnections);
			ccm.setDefaultMaxPerRoute(maxConnections);

            final ContentEncodingHttpClient result = new ContentEncodingHttpClient(ccm, params);
			configureProxy(result);
			return result;
        } catch (Exception e) {
            return new ContentEncodingHttpClient();
        }
    }

	private static void configureProxy(DefaultHttpClient httpclient) {
	    String proxyHost = System.getProperty("http.proxyHost");
	    String proxyPort = System.getProperty("http.proxyPort");
	    if (proxyHost != null && proxyPort != null) {
	        int port;
	        try {
	            port = Integer.parseInt(proxyPort);
	        } catch (NumberFormatException e) {
	            throw new RedmineConfigurationException("Illegal proxy port " + proxyPort, e);
	        }
	        HttpHost proxy = new HttpHost(proxyHost, port);
	        httpclient.getParams().setParameter(org.apache.http.conn.params.ConnRoutePNames.DEFAULT_PROXY, proxy);
	        String proxyUser = System.getProperty("http.proxyUser");
	        if (proxyUser != null) {
	            String proxyPassword = System.getProperty("http.proxyPassword");
	            httpclient.getCredentialsProvider().setCredentials(
	                    new AuthScope(proxyHost, port),
	                    new UsernamePasswordCredentials(proxyUser, proxyPassword));
	        }
	    }
	}
}
