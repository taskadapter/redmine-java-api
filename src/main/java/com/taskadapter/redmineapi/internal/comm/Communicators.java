package com.taskadapter.redmineapi.internal.comm;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpResponse;

import com.taskadapter.redmineapi.RedmineException;
import com.taskadapter.redmineapi.RedmineTransportException;

/**
 * Communicator utilities.
 * 
 * @author maxkar
 * 
 */
public final class Communicators {
	private static final ContentHandler<Object, Object> IDENTITY_HANDLER = new ContentHandler<Object, Object>() {
		@Override
		public Object processContent(Object content) throws RedmineException {
			return content;
		}
	};

	private static final ContentHandler<HttpResponse, BasicHttpResponse> TRANSPORT_DECODER = new TransportDecoder();

	private static final ContentHandler<BasicHttpResponse, Reader> CHARACTER_DECODER = new ContentHandler<BasicHttpResponse, Reader>() {
		@Override
		public Reader processContent(BasicHttpResponse content)
				throws RedmineException {
			final String charset = content.getCharset();
			try {
				return new InputStreamReader(content.getStream(), charset);
			} catch (UnsupportedEncodingException e) {
				throw new RedmineTransportException(
						"Unsupported response charset " + charset, e);
			}
		}
	};
	
	private static final ContentHandler<HttpResponse, Integer> HTTP_RESPONSE_CODE = new ContentHandler<HttpResponse, Integer>() {
		@Override
		public Integer processContent(HttpResponse content) throws RedmineException {
			return content.getStatusLine().getStatusCode();
		}
	};

	private static final ContentHandler<Reader, String> READ_CHARS = new ContentHandler<Reader, String>() {
		@Override
		public String processContent(Reader content) throws RedmineException {
			return readAll(content);
		}
	};

	private static final ContentHandler<BasicHttpResponse, String> CHAR_CONTENT_READER = compose(
			READ_CHARS, CHARACTER_DECODER);

	static String readAll(Reader r) throws RedmineException {
		final StringWriter writer = new StringWriter();
		final char[] buffer = new char[4096];
		int readed;
		try {
			while ((readed = r.read(buffer)) > 0) {
				writer.write(buffer, 0, readed);
			}
			r.close();
			writer.close();
			return writer.toString();
		} catch (IOException e) {
			throw new RedmineTransportException(e);
		}
	}

	public static <K, V> SimpleCommunicator<V> simplify(
			Communicator<K> communicator, ContentHandler<K, V> handler) {
		return new BasicSimplifier<V, K>(handler, communicator);
	}

	@SuppressWarnings("unchecked")
	public static <K> ContentHandler<K, K> identityHandler() {
		return (ContentHandler<K, K>) IDENTITY_HANDLER;
	}

	public static ContentHandler<BasicHttpResponse, String> contentReader() {
		return CHAR_CONTENT_READER;
	}

	public static <K, I, R> ContentHandler<K, R> compose(
			ContentHandler<I, R> cont1, ContentHandler<K, I> cont2) {
		return new ComposingHandler<K, I, R>(cont1, cont2);
	}

	public static <K, R> Communicator<R> fmap(Communicator<K> comm,
			ContentHandler<K, R> handler) {
		return new FmapCommunicator<R, K>(handler, comm);
	}

	public static ContentHandler<HttpResponse, BasicHttpResponse> transportDecoder() {
		return TRANSPORT_DECODER;
	}

	public static ContentHandler<BasicHttpResponse, Reader> characterDecoder() {
		return CHARACTER_DECODER;
	}

	public static ContentHandler<Reader, String> readChars() {
		return READ_CHARS;
	}
	
	public static ContentHandler<HttpResponse, Integer> httpResponseCodeReader() {
		return HTTP_RESPONSE_CODE;
	}
}
