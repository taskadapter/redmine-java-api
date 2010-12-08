package org.alskor.redmine;

public class User {
	private Integer id;
	private String fullName;

	public Integer getId() {
		return id;
	}

	@Override
	public String toString() {
		return fullName;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

}
