package com.taskadapter.redmineapi.internal.json;

import org.json.JSONException;
import org.json.JSONWriter;

/**
 * Json object writer.
 * 
 * @author maxkar
 * 
 */
public interface JsonObjectWriter<T> {
	public void write(JSONWriter writer, T object) throws JSONException;
}
