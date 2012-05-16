package org.redmine.ta.internal.comm;

import java.io.IOException;
import java.io.InputStream;

/**
 * Content stream adapter.
 * 
 * @author maxkar
 * 
 */
interface ContentStreamAdapter {
	/**
	 * Adapts a stream input.
	 * 
	 * @param stream
	 *            stream to use.
	 * @return adapted input stream.
	 * @throws IOException
	 *             if something goes wrong.
	 */
	public InputStream adaptInput(InputStream stream) throws IOException;
}
