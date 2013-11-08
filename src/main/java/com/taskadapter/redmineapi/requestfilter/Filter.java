package com.taskadapter.redmineapi.requestfilter;

import org.apache.http.NameValuePair;

/**
 * 
 * @author Paweł Dorofiejczyk
 *
 */
public interface Filter {
	public NameValuePair getNameValuePair();
}
