package org.alskor.redmine.internal;

public class License {
	private String customerName;
	private String email;
	private String createdOn;

	public String getCustomerName() {
		return customerName;
	}

	public String getEmail() {
		return email;
	}

	public String getCreatedOn() {
		return createdOn;
	}

	public License(String customerName, String email, String createdOn) {
		super();
		this.customerName = customerName;
		this.email = email;
		this.createdOn = createdOn;
	}

}
