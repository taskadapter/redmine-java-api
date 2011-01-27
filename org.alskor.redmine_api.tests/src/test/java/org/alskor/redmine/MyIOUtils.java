package org.alskor.redmine;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;

public class MyIOUtils {

	public static InputStream getResourceAsStream(String resource)
			throws IOException {
		ClassLoader cl = MyIOUtils.class.getClassLoader();
		InputStream in = cl.getResourceAsStream(resource);

		if (in == null) {
			throw new IOException("resource \"" + resource + "\" not found");
		}

		return in;
	}

	/**
	 * Loads the resource from classpath
	 */
	public static String getResourceAsString(String resource)
			throws IOException {
		InputStream in = getResourceAsStream(resource);
		return convertStreamToString(in);
	}

	private static String convertStreamToString(InputStream is)
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
			// XXX is it UTF8 or UTF-8 ???
			out = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(fileName), "UTF8"));

			out.write(text);
			out.close();
		} finally {
			if (out != null) {
				out.close();
			}
		}
	}

	public static String loadFile(String fileName) throws IOException {
		StringBuffer buffer = new StringBuffer();
		FileInputStream fis = new FileInputStream(fileName);
		InputStreamReader isr = new InputStreamReader(fis, "UTF8");
		Reader in = new BufferedReader(isr);
		int ch;
		while ((ch = in.read()) > -1) {
			buffer.append((char) ch);
		}
		in.close();
		return buffer.toString();
	}
}
