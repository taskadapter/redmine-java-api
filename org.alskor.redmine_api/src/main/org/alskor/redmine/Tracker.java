package org.alskor.redmine;

/**
 * Represents Redmine's Tracker (bug/feature/task/...)
 */
public class Tracker {
	
	private Integer id;
	
	private String name;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
