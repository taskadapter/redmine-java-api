package org.redmine.ta.internal.comm;

import org.apache.http.*;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.redmine.ta.*;
import org.redmine.ta.internal.RedmineJSONParser;
import org.redmine.ta.internal.logging.Logger;
import org.redmine.ta.internal.logging.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.net.URI;
import java.util.List;

public class BaseCommunicator implements Communicator {
	public static final String CHARSET = "UTF-8";

	private final Logger logger = LoggerFactory
			.getLogger(BaseCommunicator.class);

	/**
	 * Used redmine options.
	 */
	private final RedmineOptions options;

	/**
	 * Used HTTP client.
	 */
	private final HttpClient client;

	public BaseCommunicator(RedmineOptions options) {
		this.options = options;
		final DefaultHttpClient clientImpl = HttpUtil.getNewHttpClient(options
				.getMaxOpen());
		this.client = clientImpl;
	}

	// TODO lots of usages process 404 code themselves, but some don't.
	// check if we can process 404 code in this method instead of forcing
	// clients to deal with it.

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.redmine.ta.internal.comm.Communicator#sendRequest(org.apache.http
	 * .HttpRequest)
	 */
	@Override
	public String sendRequest(HttpRequest request) throws RedmineException {
		logger.debug(request.getRequestLine().toString());

		request.addHeader("Accept-Encoding", "gzip,deflate");
		final HttpClient httpclient = client;
		try {
			final HttpResponse httpResponse = httpclient
					.execute((HttpUriRequest) request);
			try {
				checkErrors(httpResponse);
				final HttpEntity responseEntity = httpResponse.getEntity();
				return getContent(responseEntity);
			} finally {
				EntityUtils.consume(httpResponse.getEntity());

			}
		} catch (ClientProtocolException e1) {
			throw new RedmineFormatException(e1);
		} catch (IOException e1) {
			throw new RedmineTransportException(e1);
		}
	}

	/**
	 * Checks http response for an error marks.
	 * 
	 * @param httpResponse
	 *            response to check.
	 * @throws IOException
	 */
	private void checkErrors(HttpResponse httpResponse)
			throws RedmineException, IOException {
		final int responseCode = httpResponse.getStatusLine().getStatusCode();
		if (responseCode == HttpStatus.SC_UNAUTHORIZED) {
			throw new RedmineAuthenticationException(
					"Authorization error. Please check if you provided a valid API access key or Login and Password and REST API service is enabled on the server.");
		}
		if (responseCode == HttpStatus.SC_FORBIDDEN) {
			throw new NotAuthorizedException(
					"Forbidden. Please check the user has proper permissions.");
		}
		if (responseCode == HttpStatus.SC_NOT_FOUND) {
			throw new NotFoundException(
					"Server returned '404 not found'. response body:"
							+ getContent(httpResponse.getEntity()));
		}

		if (responseCode == HttpStatus.SC_UNPROCESSABLE_ENTITY) {
			List<String> errors;
			try {
				errors = RedmineJSONParser.parseErrors(getContent(httpResponse
						.getEntity()));
			} catch (JSONException e) {
				throw new RedmineFormatException("Bad redmine error responce",
						e);
			}
			throw new RedmineProcessingException(errors);
		}
	}

	/**
	 * Returns a content entity.
	 * 
	 * @param entity
	 *            entity.
	 * @return entity content as string.
	 */
	private final String getContent(HttpEntity entity) throws IOException {
		final String charset = HttpUtil.getCharset(entity);
		final InputStream contentStream = HttpUtil.getEntityStream(entity);

		final char[] buffer = new char[4096];
		try {
			final StringWriter writer = new StringWriter();
			final Reader reader = new BufferedReader(new InputStreamReader(
					contentStream, charset));
			try {
				int readed;
				while ((readed = reader.read(buffer)) > 0)
					writer.write(buffer, 0, readed);
				return writer.toString();
			} finally {
				reader.close();
			}
		} finally {
			contentStream.close();
		}
	}

	public String sendGet(URI uri) throws RedmineException {
		HttpGet http = new HttpGet(uri);
		return sendRequest(http);
	}

	/**
	 * Shutdowns a communicator.
	 */
	public void shutdown() {
		client.getConnectionManager().shutdown();
	}
}
