package org.redmine.ta.internal;

import java.io.IOException;

import com.google.gson.stream.JsonWriter;

/**
 * Json object writer.
 * 
 * @author maxkar
 * 
 */
public interface JsonObjectWriter<T> {
	public void write(JsonWriter writer, T object) throws IOException;
}
