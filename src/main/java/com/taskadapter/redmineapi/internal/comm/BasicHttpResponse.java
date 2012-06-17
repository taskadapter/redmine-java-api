package com.taskadapter.redmineapi.internal.comm;

import java.io.InputStream;

/**
 * Basic http entity. Just an iternal implementation to use with proper
 * wrappers, etc...
 * 
 * @author maxkar
 * 
 */
public final class BasicHttpResponse {
	private final int responseCode;
	private final InputStream stream;
	private final String charset;

	public BasicHttpResponse(int responseCode, InputStream stream,
			String charset) {
		super();
		this.responseCode = responseCode;
		this.stream = stream;
		this.charset = charset;
	}

	public int getResponseCode() {
		return responseCode;
	}

	public InputStream getStream() {
		return stream;
	}

	public String getCharset() {
		return charset;
	}

}
