package com.taskadapter.redmineapi.bean;

public class TimeEntryActivityFactory {
    public static TimeEntryActivity create(Integer id) {
        return new TimeEntryActivity(id);
    }
}
