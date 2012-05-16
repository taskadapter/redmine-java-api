package org.redmine.ta.internal.comm;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
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

	@SuppressWarnings("deprecation")
	public static DefaultHttpClient getNewHttpClient(int maxConnections) {
		try {
			KeyStore trustStore = KeyStore.getInstance(KeyStore
					.getDefaultType());
			trustStore.load(null, null);

			SSLSocketFactory sf = new FakeSSLSocketFactory(trustStore);
			sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

			HttpParams params = new BasicHttpParams();
			HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
			HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);

			SchemeRegistry registry = new SchemeRegistry();
			registry.register(new Scheme("http", PlainSocketFactory
					.getSocketFactory(), 80));
			registry.register(new Scheme("https", sf, 443));

			ThreadSafeClientConnManager ccm = new ThreadSafeClientConnManager(
					params, registry);
			ccm.setMaxTotal(maxConnections);
			ccm.setDefaultMaxPerRoute(maxConnections);

			final DefaultHttpClient result = new DefaultHttpClient(ccm, params);
			configureProxy(result);
			return result;
		} catch (Exception e) {
			return new DefaultHttpClient();
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
	private static String getEntityEncoding(HttpEntity entity) {
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
