package com.taskadapter.redmineapi.internal.comm;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import com.taskadapter.redmineapi.RedmineException;
import com.taskadapter.redmineapi.RedmineTransportException;

/**
 * Transport encoding decoder.
 * 
 * @author maxkar
 * 
 */
final class TransportDecoder implements
		ContentHandler<HttpResponse, BasicHttpResponse> {

	@Override
	public BasicHttpResponse processContent(HttpResponse content)
			throws RedmineException {
		final HttpEntity entity = content.getEntity();
		final String charset = HttpUtil.getCharset(entity);
		final String encoding = HttpUtil.getEntityEncoding(entity);
		try {
			final InputStream initialStream = entity.getContent();
			return new BasicHttpResponse(content.getStatusLine()
					.getStatusCode(), decodeStream(encoding, initialStream),
					charset);
		} catch (IOException e) {
			throw new RedmineTransportException(e);
		}
	}

	/**
	 * Decodes a transport stream.
	 * 
	 * @param encoding
	 *            stream encoding.
	 * @param initialStream
	 *            initial stream.
	 * @return decoding stream.
	 * @throws IOException
	 */
	private InputStream decodeStream(String encoding, InputStream initialStream)
			throws IOException {
		if (encoding == null)
			return initialStream;
		if ("gzip".equals(encoding))
			return new GZIPInputStream(initialStream);
		if ("deflate".equals(encoding))
			return new InflaterInputStream(initialStream);
		throw new IOException("Unsupported transport encoding " + encoding);
	}
}
