package org.redmine.ta.internal;

import org.redmine.ta.RedmineFormatException;

import com.google.gson.JsonElement;

/**
 * Json object parser.
 * 
 * @author maxkar
 * 
 * @param <T>
 *            parsing result type.
 */
public interface JsonObjectParser<T> {
	public T parse(JsonElement input) throws RedmineFormatException;
}
