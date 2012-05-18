package org.redmine.ta.internal.comm;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpVersion;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.redmine.ta.RedmineConfigurationException;

class HttpUtil {
	private static final Map<String, ContentStreamAdapter> ENCODING_ADAPTERS = new HashMap<String, ContentStreamAdapter>();
	static {
		ENCODING_ADAPTERS.put("gzip", new ContentStreamAdapter() {
			@Override
			public InputStream adaptInput(InputStream stream)
					throws IOException {
				return new GZIPInputStream(stream);
			}
		});
		ENCODING_ADAPTERS.put("deflate", new ContentStreamAdapter() {
			@Override
			public InputStream adaptInput(InputStream stream)
					throws IOException {
				return new InflaterInputStream(stream);
			}
		});
	}

	public static DefaultHttpClient getNewHttpClient(
			ClientConnectionManager connectionManager) {
		try {

			HttpParams params = new BasicHttpParams();
			HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
			HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);

			final DefaultHttpClient result = new DefaultHttpClient(
					connectionManager, params);
			configureProxy(result);
			return result;
		} catch (Exception e) {
			return new DefaultHttpClient();
		}
	}

	@SuppressWarnings("deprecation")
	static ThreadSafeClientConnManager createConnectionManager(
			int maxConnections) throws KeyStoreException,
			NoSuchAlgorithmException, CertificateException, IOException,
			KeyManagementException, UnrecoverableKeyException {
		KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
		trustStore.load(null, null);
		SSLSocketFactory sf = new FakeSSLSocketFactory(trustStore);
		sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

		SchemeRegistry registry = new SchemeRegistry();
		registry.register(new Scheme("http", 80, PlainSocketFactory
				.getSocketFactory()));
		registry.register(new Scheme("https", 443, sf));

		ThreadSafeClientConnManager ccm = new ThreadSafeClientConnManager(
				registry);
		ccm.setMaxTotal(maxConnections);
		ccm.setDefaultMaxPerRoute(maxConnections);
		return ccm;
	}

	private static void configureProxy(DefaultHttpClient httpclient) {
		String proxyHost = System.getProperty("http.proxyHost");
		String proxyPort = System.getProperty("http.proxyPort");
		if (proxyHost != null && proxyPort != null) {
			int port;
			try {
				port = Integer.parseInt(proxyPort);
			} catch (NumberFormatException e) {
				throw new RedmineConfigurationException("Illegal proxy port "
						+ proxyPort, e);
			}
			HttpHost proxy = new HttpHost(proxyHost, port);
			httpclient.getParams().setParameter(
					org.apache.http.conn.params.ConnRoutePNames.DEFAULT_PROXY,
					proxy);
			String proxyUser = System.getProperty("http.proxyUser");
			if (proxyUser != null) {
				String proxyPassword = System.getProperty("http.proxyPassword");
				httpclient.getCredentialsProvider().setCredentials(
						new AuthScope(proxyHost, port),
						new UsernamePasswordCredentials(proxyUser,
								proxyPassword));
			}
		}
	}

	@Deprecated
	public static InputStream getEntityStream(HttpEntity entity)
			throws IOException {
		final String encoding = getEntityEncoding(entity);
		final ContentStreamAdapter adapter = ENCODING_ADAPTERS.get(encoding);
		if (adapter == null)
			return entity.getContent();
		return adapter.adaptInput(entity.getContent());
	}

	/**
	 * Returns entity encoding.
	 * 
	 * @param entity
	 *            entitity to get encoding.
	 * @return entity encoding string.
	 */
	public static String getEntityEncoding(HttpEntity entity) {
		final Header header = entity.getContentEncoding();
		if (header == null)
			return null;
		return header.getValue();
	}

	/**
	 * Returns entity charset to use.
	 * 
	 * @param entity
	 *            entity to check.
	 * @return entity charset to use in decoding.
	 */
	public static String getCharset(HttpEntity entity) {
		final String guess = EntityUtils.getContentCharSet(entity);
		return guess == null ? HTTP.DEFAULT_CONTENT_CHARSET : guess;
	}
}
