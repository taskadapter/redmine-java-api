package com.taskadapter.redmineapi.filter;

public class GroupUserFilter extends GenericFilter<Integer> implements UserFilter {
	public GroupUserFilter(Integer value) {
		super("group_id", value);
	}
}
