package com.taskadapter.redmineapi.internal.io;

import java.io.IOException;

/**
 * IOException, marked by a tag.
 * 
 * @author maxkar
 * 
 */
public class MarkedIOException extends IOException {
	private static final long serialVersionUID = 1L;
	private final String tag;

	public MarkedIOException(String tag, IOException cause) {
		super(cause);
		this.tag = tag;
	}

	public String getTag() {
		return tag;
	}

	public IOException getIOException() {
		return (IOException) getCause();
	}

}
