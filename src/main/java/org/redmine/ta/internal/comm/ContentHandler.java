package org.redmine.ta.internal.comm;

import org.redmine.ta.RedmineException;

/**
 * Redmine content handler.
 * 
 * @author maxkar
 * 
 */
public interface ContentHandler<K, R> {
	/**
	 * Consumes content of a specified type and returns a specified result.
	 * 
	 * @param content
	 *            content to process.
	 * @return processed content.
	 * @throws RedmineException
	 *             if something goes wrong.
	 */
	R processContent(K content) throws RedmineException;
}
