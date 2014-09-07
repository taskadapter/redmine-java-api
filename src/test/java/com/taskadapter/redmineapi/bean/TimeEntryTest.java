package com.taskadapter.redmineapi.bean;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TimeEntryTest {

    @Test
    public void emptyObjectIsInvalid() {
        assertFalse(TimeEntryFactory.create().isValid());
    }

    @Test
    public void validWithNonEmptyIssueId() {
        TimeEntry timeEntry = TimeEntryFactory.create();
        timeEntry.setHours(5f);
        timeEntry.setIssueId(1);
        assertTrue(timeEntry.isValid());
    }

    @Test
    public void invalidWithoutTime() {
        TimeEntry timeEntry = TimeEntryFactory.create();
        timeEntry.setIssueId(1);
        assertFalse(timeEntry.isValid());
    }

    @Test
    public void validWithNonEmptyProjectId() {
        TimeEntry timeEntry = TimeEntryFactory.create();
        timeEntry.setHours(5f);
        timeEntry.setProjectId(123);
        assertTrue(timeEntry.isValid());
    }

    @Test
    public void validWithBothProjectIdAndIssueIdSet() {
        TimeEntry timeEntry = TimeEntryFactory.create();
        timeEntry.setHours(5f);
        timeEntry.setIssueId(11);
        timeEntry.setProjectId(22);
        assertTrue(timeEntry.isValid());
    }
}
