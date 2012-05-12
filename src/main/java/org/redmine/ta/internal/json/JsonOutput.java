package org.redmine.ta.internal.json;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;

import com.google.gson.stream.JsonWriter;

public class JsonOutput {

	/**
	 * Adds a value to a writer if value is not <code>null</code>.
	 * 
	 * @param writer
	 *            writer to add object to.
	 * @param field
	 *            field name to set.
	 * @param value
	 *            field value.
	 * @throws IOException
	 *             if io error occurs.
	 */
	public static void addIfNotNull(JsonWriter writer, String field,
			String value) throws IOException {
		if (value == null)
			return;
		writer.name(field).value(value);
	}

	/**
	 * Adds a value to a writer if value is not <code>null</code>.
	 * 
	 * @param writer
	 *            writer to add object to.
	 * @param field
	 *            field name to set.
	 * @param value
	 *            field value.
	 * @throws IOException
	 *             if io error occurs.
	 */
	public static void addIfNotNull(JsonWriter writer, String field,
			Integer value) throws IOException {
		if (value == null)
			return;
		writer.name(field).value(value);
	}

	/**
	 * Adds a value to a writer if value is not <code>null</code>.
	 * 
	 * @param writer
	 *            writer to add object to.
	 * @param field
	 *            field name to set.
	 * @param value
	 *            field value.
	 * @throws IOException
	 *             if io error occurs.
	 */
	public static void addIfNotNull(JsonWriter writer, String field, Float value)
			throws IOException {
		if (value == null)
			return;
		writer.name(field).value(value);
	}

	/**
	 * Adds a value to a writer if value is not <code>null</code>.
	 * 
	 * @param writer
	 *            writer to add object to.
	 * @param field
	 *            field name to set.
	 * @param value
	 *            field value.
	 * @param format
	 *            date foramt to use.
	 * @throws IOException
	 *             if io error occurs.
	 */
	public static void addIfNotNull(JsonWriter writer, String field,
			Date value, final SimpleDateFormat format) throws IOException {
		if (value == null)
			return;
		writer.name(field).value(format.format(value));
	}

	/**
	 * Adds an object if object is not null.
	 * 
	 * @param writer
	 *            object writer.
	 * @param field
	 *            field writer.
	 * @param value
	 *            value writer.
	 * @param objWriter
	 *            object value writer.
	 * @throws IOException
	 *             if io error occurs.
	 */
	public static <T> void addIfNotNull(JsonWriter writer, String field,
			T value, JsonObjectWriter<T> objWriter) throws IOException {
		if (value == null)
			return;
		writer.name(field).beginObject();
		objWriter.write(writer, value);
		writer.endObject();
	}

	/**
	 * Adds a list.
	 * 
	 * @param writer
	 *            used writer.
	 * @param field
	 *            field to write.
	 * @param items
	 *            used items.
	 * @param objWriter
	 *            single object writer.
	 */
	public static <T> void addArrayIfNotNull(JsonWriter writer, String field,
			Collection<T> items, JsonObjectWriter<T> objWriter)
			throws IOException {
		if (items == null)
			return;
		writer.name(field).beginArray();
		for (T item : items) {
			writer.beginObject();
			objWriter.write(writer, item);
			writer.endObject();
		}
		writer.endArray();
	}

}
