package org.redmine.ta.internal.comm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;

import org.apache.http.HttpEntity;
import org.redmine.ta.RedmineException;
import org.redmine.ta.RedmineTransportException;

/**
 * Entity to string processor.
 * 
 * @author maxkar
 * 
 */
// TODO: refactor to custom result (use separate node to unpack content)
final class EntityToStringHandler implements ContentHandler<HttpEntity, String> {

	@Override
	public String processContent(HttpEntity entity) throws RedmineException {
		final String charset = HttpUtil.getCharset(entity);

		try {

			final InputStream contentStream = HttpUtil.getEntityStream(entity);

			final char[] buffer = new char[4096];
			try {
				final StringWriter writer = new StringWriter();
				final Reader reader = new BufferedReader(new InputStreamReader(
						contentStream, charset));
				try {
					int readed;
					while ((readed = reader.read(buffer)) > 0)
						writer.write(buffer, 0, readed);
					return writer.toString();
				} finally {
					reader.close();
				}
			} finally {
				contentStream.close();
			}
		} catch (IOException e) {
			throw new RedmineTransportException(e);
		}
	}

}
