package com.taskadapter.redmineapi.requestfilter;

public class NameUserFilter extends GenericFilter<String> implements UserFilter {
	public NameUserFilter(String value) {
		super("name", value);
	}
}
