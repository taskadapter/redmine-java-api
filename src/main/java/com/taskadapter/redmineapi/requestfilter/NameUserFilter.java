package com.taskadapter.redmineapi.filter;

public class NameUserFilter extends GenericFilter<String> implements UserFilter {
	public NameUserFilter(String value) {
		super("name", value);
	}
}
