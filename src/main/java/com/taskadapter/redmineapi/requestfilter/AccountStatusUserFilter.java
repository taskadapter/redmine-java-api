package com.taskadapter.redmineapi.requestfilter;

import com.taskadapter.redmineapi.enums.AccountStatus;

public class AccountStatusUserFilter extends GenericFilter<AccountStatus> implements UserFilter {
	public AccountStatusUserFilter(AccountStatus value) {
		super("status", value);
	}
}
