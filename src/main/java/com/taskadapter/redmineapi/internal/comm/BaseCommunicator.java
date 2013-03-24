package com.taskadapter.redmineapi.internal.comm;

import com.taskadapter.redmineapi.RedmineException;
import com.taskadapter.redmineapi.RedmineFormatException;
import com.taskadapter.redmineapi.RedmineOptions;
import com.taskadapter.redmineapi.RedmineTransportException;
import org.apache.http.*;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class BaseCommunicator implements Communicator<HttpResponse> {
	private final Logger logger = LoggerFactory.getLogger(BaseCommunicator.class);

	private final HttpClient client;

	private final ConnectionEvictor evictor;

	public BaseCommunicator(RedmineOptions options) {
		DefaultHttpClient clientImpl;
		ClientConnectionManager connManager;
		try {
			connManager = HttpUtil.createConnectionManager(options
					.getMaxOpenConnections());
			clientImpl = HttpUtil.getNewHttpClient(connManager);
		} catch (Exception e) {
			connManager = null;
			clientImpl = new DefaultHttpClient();
		}

		if (connManager != null) {
			evictor = new ConnectionEvictor(connManager,
					options.getEvictionCheckInterval(),
					options.getIdleTimeout());
			runEvictor(evictor);
		} else {
			evictor = null;
		}

		this.client = clientImpl;
	}

	/**
	 * Runs an evictor thread.
	 */
	private void runEvictor(ConnectionEvictor evictor2) {
		final Thread evictorThread = new Thread(evictor2);
		evictorThread.setDaemon(true);
		evictorThread
				.setName("Redmine communicator connection eviction thread");
		evictorThread.start();
	}

	// TODO lots of usages process 404 code themselves, but some don't.
	// check if we can process 404 code in this method instead of forcing
	// clients to deal with it.

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.taskadapter.redmineapi.internal.comm.Communicator#sendRequest(org.apache.http
	 * .HttpRequest)
	 */
	@Override
	public <R> R sendRequest(HttpRequest request,
			ContentHandler<HttpResponse, R> handler) throws RedmineException {
		logger.debug(request.getRequestLine().toString());

		request.addHeader("Accept-Encoding", "gzip");
		final HttpClient httpclient = client;
		try {
			final HttpResponse httpResponse = httpclient
					.execute((HttpUriRequest) request);
			try {
				return handler.processContent(httpResponse);
			} finally {
				EntityUtils.consume(httpResponse.getEntity());

			}
		} catch (ClientProtocolException e1) {
			throw new RedmineFormatException(e1);
		} catch (IOException e1) {
			throw new RedmineTransportException("Cannot fetch data from "
					+ getMessageURI(request) + " : "
							+ e1.toString(), e1);
		}
	}

	private String getMessageURI(HttpRequest request) {
		final String uri = request.getRequestLine().getUri();
		final int paramsIndex = uri.indexOf('?');
		if (paramsIndex >= 0)
			return uri.substring(0, paramsIndex);
		return uri;
	}

	/**
	 * Shutdowns a communicator.
	 */
	public void shutdown() {
		client.getConnectionManager().shutdown();
		if (evictor != null)
			evictor.shutdown();
	}

	@Override
	protected void finalize() throws Throwable {
		/*
		 * We MUST terminate evictor on finalization. Threads (even daemon
		 * threads) will not be garbage-collected automatically. Thus we should
		 * release such threads even if client forget to call
		 * "manager.shutdown".
		 */
		try {
			if (evictor != null)
				evictor.shutdown();
		} catch (Exception e) {
			// ignore;
		}
		super.finalize();
	}
}
