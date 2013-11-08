package com.taskadapter.redmineapi.requestfilter;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

/**
 * Generic filter implementation for all simple filters
 * 
 * @author Paweł Dorofiejczyk
 *
 * @param <T>
 */
public abstract class GenericFilter<T> implements Filter {
	private final T value;
	private final String name;
	
	public GenericFilter(String name, T value) {
		this.name = name;
		this.value = value;
	}
	
	@Override
	public NameValuePair getNameValuePair() {
		return new BasicNameValuePair(name, value.toString());
	}
}
