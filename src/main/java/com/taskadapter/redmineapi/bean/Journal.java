package com.taskadapter.redmineapi.bean;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Redmine issue journal field
 */
public class Journal {

    private int id;
    private String notes;
    private User user;
    private Date createdOn;
    private List<JournalDetail> details = new ArrayList<JournalDetail>();

    public Journal() {
    }

    public Journal(int id, String notes, User user, Date createdOn) {
        this.id = id;
        this.notes = notes;
        this.user = user;
        this.createdOn = createdOn;
    }

    public Journal(int id, String notes, User user, Date createdOn, List<JournalDetail> details) {
        this.id = id;
        this.notes = notes;
        this.user = user;
        this.createdOn = createdOn;
        this.details = details;
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

    public void setId(int id) {
        this.id = id;
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
        return details;
    }

    public void setDetails(List<JournalDetail> details) {
        this.details = details;
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
