package com.taskadapter.redmineapi.internal.json;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Json object parser.
 * 
 * @author maxkar
 * 
 * @param <T>
 *            parsing result type.
 */
public interface JsonObjectParser<T> {
	public T parse(JSONObject input) throws JSONException;
}
