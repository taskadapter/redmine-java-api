package com.taskadapter.redmineapi.filter;

import com.taskadapter.redmineapi.enums.AccountStatuses;

public class StatusUserFilter extends GenericFilter<Integer> implements UserFilter {
	public StatusUserFilter(AccountStatuses value) {
		super("status", value.getId());
	}
}
