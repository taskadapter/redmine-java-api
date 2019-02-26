package com.taskadapter.redmineapi.bean;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TimeEntryTest {

    @Test
    public void emptyObjectIsInvalid() {
        assertFalse(new TimeEntry(null).isValid());
    }

    @Test
    public void validWithNonEmptyIssueId() {
        TimeEntry timeEntry = new TimeEntry(null)
                .setHours(5f)
                .setIssueId(1);

        assertTrue(timeEntry.isValid());
    }

    @Test
    public void invalidWithoutTime() {
        TimeEntry timeEntry = new TimeEntry(null)
                .setIssueId(1);
        assertFalse(timeEntry.isValid());
    }

    @Test
    public void validWithNonEmptyProjectId() {
        TimeEntry timeEntry = new TimeEntry(null)
                .setHours(5f)
                .setProjectId(123);
        assertTrue(timeEntry.isValid());
    }

    @Test
    public void validWithBothProjectIdAndIssueIdSet() {
        TimeEntry timeEntry = new TimeEntry(null)
                .setHours(5f)
                .setIssueId(11)
                .setProjectId(22);
        assertTrue(timeEntry.isValid());
    }
}
