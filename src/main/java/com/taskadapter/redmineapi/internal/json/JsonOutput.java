package com.taskadapter.redmineapi.internal.json;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONWriter;

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
	 * @throws JSONException
	 *             if io error occurs.
	 */
	public static void addIfNotNull(JSONWriter writer, String field,
			String value) throws JSONException {
		if (value == null)
			return;
		writer.key(field);
		writer.value(value);
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
	 * @throws JSONException
	 *             if io error occurs.
	 */
	public static void addIfNotNull(JSONWriter writer, String field,
			Integer value) throws JSONException {
		if (value == null)
			return;
		writer.key(field);
		writer.value(value);
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
	 * @throws JSONException
	 *             if io error occurs.
	 */
	public static void addIfNotNull(JSONWriter writer, String field, Float value)
			throws JSONException {
		if (value == null)
			return;
		writer.key(field);
		writer.value(value);
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
	 * @throws JSONException
	 *             if io error occurs.
	 */
	public static void addIfNotNull(JSONWriter writer, String field,
			Date value, final SimpleDateFormat format) throws JSONException {
		if (value == null)
			return;
		writer.key(field);
		writer.value(format.format(value));
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
	 * @throws JSONException
	 *             if io error occurs.
	 */
	public static void addIfNotNull(JSONWriter writer, String field,
			Boolean value) throws JSONException {
		if (value == null)
			return;
		writer.key(field);
		writer.value(value);
	}
        
	/**
	 * Adds a value to a writer.
	 * 
	 * @param writer
	 *            writer to add object to.
	 * @param field
	 *            field name to set.
	 * @param value
	 *            field value.
	 * @param format
	 *            date foramt to use.
	 * @throws JSONException
	 *             if io error occurs.
	 */
	public static void add(JSONWriter writer, String field,
 Date value,
			final SimpleDateFormat format) throws JSONException {
		writer.key(field);
		if (value == null)
			writer.value(null);
		else
			writer.value(format.format(value));
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
	 * @throws JSONException
	 *             if io error occurs.
	 */
	public static <T> void addIfNotNull(JSONWriter writer, String field,
			T value, JsonObjectWriter<T> objWriter) throws JSONException {
		if (value == null)
			return;
		writer.key(field);
		writer.object();
		objWriter.write(writer, value);
		writer.endObject();
	}

	/**
	 * Adds a list of scalar values.
	 * 
	 * @param writer
	 *            used writer.
	 * @param field
	 *            field to write.
	 * @param items
	 *            used items.
	 * @param objWriter
	 *            single value writer.
	 */
	public static <T> void addScalarArray(JSONWriter writer, String field,
			Collection<T> items, JsonObjectWriter<T> objWriter)
			throws JSONException {
		writer.key(field);
		writer.array();
		for (T item : items) {
			objWriter.write(writer, item);
		}
		writer.endArray();
	}

	/**
	 * Adds a list of objects.
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
	public static <T> void addArrayIfNotEmpty(JSONWriter writer, String field,
			Collection<T> items, JsonObjectWriter<T> objWriter)
			throws JSONException {
		if (items == null || items.size() == 0)
			return;
		addCollection(writer, field, items, objWriter);
	}

	private static <T> void addCollection(JSONWriter writer, String field,
			Collection<T> items, JsonObjectWriter<T> objWriter)
			throws JSONException {
		writer.key(field);
		writer.array();
		for (T item : items) {
			writer.object();
			objWriter.write(writer, item);
			writer.endObject();
		}
		writer.endArray();
	}

}
