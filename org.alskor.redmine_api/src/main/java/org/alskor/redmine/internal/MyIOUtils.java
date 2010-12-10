package org.alskor.redmine.internal;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;

public class MyIOUtils {

	// public static URL getResourceAsURL(String resource) {
	// ClassLoader cl = MyIOUtils.class.getClassLoader();
	// return cl.getResource(resource);
	// }

	public static InputStream getResourceAsStream(String resource)
			throws IOException {
		ClassLoader cl = MyIOUtils.class.getClassLoader();
		InputStream in = cl.getResourceAsStream(resource);

		if (in == null) {
			throw new IOException("resource \"" + resource + "\" not found");
		}

		return in;
	}

	public static String getResourceAsString(String resource)
			throws IOException {
		InputStream in = getResourceAsStream(resource);
		return convertStreamToString(in);
	}

	public static String convertStreamToString(InputStream is)
			throws IOException {
		/*
		 * To convert the InputStream to String we use the Reader.read(char[]
		 * buffer) method. We iterate until the Reader return -1 which means
		 * there's no more data to read. We use the StringWriter class to
		 * produce the string.
		 */
		if (is != null) {
			Writer writer = new StringWriter();

			char[] buffer = new char[1024];
			try {
				Reader reader = new BufferedReader(new InputStreamReader(is,
						"UTF-8"));
				int n;
				while ((n = reader.read(buffer)) != -1) {
					writer.write(buffer, 0, n);
				}
			} finally {
				is.close();
			}
			return writer.toString();
		} else {
			return "";
		}
	}

	public static void writeToFile(String fileName, String text)
			throws IOException {
		BufferedWriter out = null;
		try {
			out = new BufferedWriter(new OutputStreamWriter(
					// XXX is it UTF8 or UTF-8 ???
					new FileOutputStream(fileName), "UTF8"));

			out.write(text);
			out.close();
		} finally {
			if (out != null) {
				out.close();
			}
		}
	}
}