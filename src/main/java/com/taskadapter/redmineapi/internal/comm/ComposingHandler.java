package com.taskadapter.redmineapi.internal.comm;

import com.taskadapter.redmineapi.RedmineException;

/**
 * Composing content handler.
 * 
 * @author maxkar
 * 
 */
final class ComposingHandler<K, I, R> implements ContentHandler<K, R> {

	private final ContentHandler<I, R> outer;
	private final ContentHandler<K, I> inner;

	public ComposingHandler(ContentHandler<I, R> outer,
			ContentHandler<K, I> inner) {
		super();
		this.outer = outer;
		this.inner = inner;
	}

	@Override
	public R processContent(K content) throws RedmineException {
		return outer.processContent(inner.processContent(content));
	}

}
