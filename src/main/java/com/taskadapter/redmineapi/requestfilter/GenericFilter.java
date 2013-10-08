package com.taskadapter.redmineapi.filter;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

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
