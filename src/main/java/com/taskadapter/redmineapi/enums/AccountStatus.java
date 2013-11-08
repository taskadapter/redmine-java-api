package com.taskadapter.redmineapi.enums;

/**
 * From http://www.redmine.org/projects/redmine/repository/entry/trunk/app/models/principal.rb#L22-25
 * 
 * @author Paweł Dorofiejczyk
 *
 */
public enum AccountStatus {
	ANONYMOUS(0),
	ACTIVE(1),
	REGISTERED(2),
	LOCKED(3);
	
	private Integer id;
	
	private AccountStatus(Integer id) {
		this.id = id;
	}
	
	public String toString() {
		return id.toString();
	}
}
