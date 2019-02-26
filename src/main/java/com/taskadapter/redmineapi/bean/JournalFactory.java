package com.taskadapter.redmineapi.bean;

import java.util.Date;
import java.util.List;

public class JournalFactory {
    public static Journal create(Integer id, String notes, User user, Date createdOn) {
        Journal journal = new Journal().setId(id);
        journal.setNotes(notes);
        journal.setUser(user);
        journal.setCreatedOn(createdOn);
        return journal;
    }

    public static Journal create(Integer id, String notes, User user, Date createdOn, List<JournalDetail> details) {
        Journal journal = new Journal().setId(id);
        journal.setNotes(notes);
        journal.setUser(user);
        journal.setCreatedOn(createdOn);
        journal.addDetails(details);
        return journal;
    }
}
