package com.taskadapter.redmineapi.bean;

import java.util.Date;
import java.util.List;

public class JournalFactory {
    public static Journal create(Integer id, String notes, User user, Date createdOn) {
        Journal journal = new Journal(id);
        journal.setNotes(notes);
        journal.setUser(user);
        journal.setCreatedOn(createdOn);
        return journal;
    }

    public static Journal create(Integer id, String notes, User user, Date createdOn, List<JournalDetail> details) {
        Journal journal = new Journal(id);
        journal.setNotes(notes);
        journal.setUser(user);
        journal.setCreatedOn(createdOn);
        journal.setDetails(details);
        return journal;
    }

    public static Journal create(int id) {
        return new Journal(id);
    }
}
