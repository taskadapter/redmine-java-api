package com.taskadapter.redmineapi.bean;

public enum PeriodType {

	All("all"),
	Today("today"),
	Yesterday("yesterday"),
	CurrentWeek("current_week"),
	LastWeek("last_week"),
	SevenDays("7_days"),
	CurrentMonth("current_month"),
	LastMonth("last_month"),
	ThirtyDays("30_days"),
	CurrentYear("current_year"),
	Custom("custom");

	private String name;

	private PeriodType(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return name;
	}

}
