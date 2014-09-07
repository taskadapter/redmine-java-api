package com.taskadapter.redmineapi.bean;

public class TimeEntryFactory {
    public static TimeEntry create(Integer id) {
        return new TimeEntry(id);
    }

    public static TimeEntry create() {
        return new TimeEntry(null);
    }
}
