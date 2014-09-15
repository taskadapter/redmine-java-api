package com.taskadapter.redmineapi.bean;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Redmine issue journal field
 */
public class Journal {

    /**
     * database ID.
     */
    private final Integer id;

    private String notes;
    private User user;
    private Date createdOn;

    private final List<JournalDetail> details = new ArrayList<JournalDetail>();

    /**
     * Use JournalFactory to create instances of this class.
     *
     * @param id database ID.
     */
    Journal(Integer id) {
        this.id = id;
    }

    public Date getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Date createdOn) {
        this.createdOn = createdOn;
    }

    public int getId() {
        return id;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<JournalDetail> getDetails() {
        return Collections.unmodifiableList(details);
    }

    public void addDetails(Collection<JournalDetail> details) {
        this.details.addAll(details);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Journal journal = (Journal) o;

        if (id != journal.id) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public String toString() {
        return "Journal{" + "id=" + id + " notes=" + notes + " user=" + user + " createdOn=" + createdOn + " details=" + details + '}';
    }

}
