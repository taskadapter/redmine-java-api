package com.taskadapter.redmineapi.enums;

public enum AccountStatuses {
	ANONYMOUS(0),
	ACTIVE(1),
	REGISTERED(2),
	LOCKED(3);
	
	private Integer id;
	
	private AccountStatuses(Integer id) {
		this.id = id;
	}
	
	public Integer getId() {
		return id;
	}
}
