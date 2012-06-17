package com.taskadapter.redmineapi.internal.io;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public final class MarkedInputStream extends FilterInputStream {

	private final String tag;

	public MarkedInputStream(InputStream in, String tag) {
		super(in);
		this.tag = tag;
	}

	@Override
	public int available() throws IOException {
		try {
			return super.available();
		} catch (IOException e) {
			throw new MarkedIOException(tag, e);
		}
	}

	@Override
	public void close() throws IOException {
		try {
			super.close();
		} catch (IOException e) {
			throw new MarkedIOException(tag, e);
		}
	}

	@Override
	public int read() throws IOException {
		try {
			return super.read();
		} catch (IOException e) {
			throw new MarkedIOException(tag, e);
		}
	}

	@Override
	public int read(byte[] b) throws IOException {
		try {
			return super.read(b);
		} catch (IOException e) {
			throw new MarkedIOException(tag, e);
		}
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		try {
			return super.read(b, off, len);
		} catch (IOException e) {
			throw new MarkedIOException(tag, e);
		}
	}

	@Override
	public synchronized void reset() throws IOException {
		try {
			super.reset();
		} catch (IOException e) {
			throw new MarkedIOException(tag, e);
		}
	}

	@Override
	public long skip(long n) throws IOException {
		try {
			return super.skip(n);
		} catch (IOException e) {
			throw new MarkedIOException(tag, e);
		}
	}
}
