package com.taskadapter.redmineapi.bean;

import java.util.Date;

public class TimeEntrySearchCriteria {
	private String projectKey;
	private PeriodType periodType;
	private Date from;
	private Date to;

	public Date getFrom() {
		return from;
	}

	public PeriodType getPeriodType() {
		return periodType;
	}

	public String getProjectKey() {
		return projectKey;
	}

	public Date getTo() {
		return to;
	}

	public void setFrom(Date from) {
		this.from = from;
	}

	public void setPeriodType(PeriodType periodType) {
		this.periodType = periodType;
	}

	public void setProjectKey(String projectKey) {
		this.projectKey = projectKey;
	}

	public void setTo(Date to) {
		this.to = to;
	}

}
