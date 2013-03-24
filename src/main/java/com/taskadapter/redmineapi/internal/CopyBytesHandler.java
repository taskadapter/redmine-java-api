package com.taskadapter.redmineapi.internal;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.taskadapter.redmineapi.RedmineCommunicationException;
import com.taskadapter.redmineapi.RedmineException;
import com.taskadapter.redmineapi.internal.comm.BasicHttpResponse;
import com.taskadapter.redmineapi.internal.comm.ContentHandler;

/**
 * "Copy bytes" handler.
 * 
 */
public final class CopyBytesHandler implements ContentHandler<BasicHttpResponse, Void> {

	private final OutputStream outStream;

	public CopyBytesHandler(OutputStream outStream) {
		this.outStream = outStream;
	}

	@Override
	public Void processContent(BasicHttpResponse content)
			throws RedmineException {
		final byte[] buffer = new byte[4096 * 4];
		int readed;
		try {
			final InputStream input = content.getStream();
			try {
				while ((readed = input.read(buffer)) > 0)
					outStream.write(buffer, 0, readed);
			} finally {
				input.close();
			}
		} catch (IOException e) {
			throw new RedmineCommunicationException(e);
		}
		return null;
	}
}
