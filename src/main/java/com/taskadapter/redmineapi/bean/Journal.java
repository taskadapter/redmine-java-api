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

    private final PropertyStorage storage = new PropertyStorage();

    /**
     * database numeric ID.
     */
    public final static Property<Integer> DATABASE_ID = new Property<>(Integer.class, "id");
    public final static Property<String> NOTES = new Property<String>(String.class, "notes");
    public final static Property<User> USER = new Property<>(User.class, "user");
    public final static Property<Date> CREATED_ON = new Property<>(Date.class, "createdOn");
    public final static Property<List<JournalDetail>> DETAILS = (Property<List<JournalDetail>>) new Property(List.class, "details");

    public Journal() {
        storage.set(DETAILS, new ArrayList<>());
    }

    /**
     * @param id database ID.
     */
    public Journal setId(Integer id) {
        storage.set(DATABASE_ID, id);
        return this;
    }

    public Date getCreatedOn() {
        return storage.get(CREATED_ON);
    }

    public void setCreatedOn(Date createdOn) {
        storage.set(CREATED_ON, createdOn);
    }

    public Integer getId() {
        return storage.get(DATABASE_ID);
    }

    public String getNotes() {
        return storage.get(NOTES);
    }

    public void setNotes(String notes) {
        storage.set(NOTES, notes);
    }

    public User getUser() {
        return storage.get(USER);
    }

    public void setUser(User user) {
        storage.set(USER, user);
    }

    public List<JournalDetail> getDetails() {
        return Collections.unmodifiableList(storage.get(DETAILS));
    }

    public void addDetails(Collection<JournalDetail> details) {
        storage.get(DETAILS).addAll(details);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Journal journal = (Journal) o;

        return getId() != null ? getId().equals(journal.getId()) : journal.getId() == null;

    }

    @Override
    public int hashCode() {
        return getId() != null ? getId().hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Journal{" + "id=" + getId() + " notes=" + getNotes() + " user=" + getUser() + " createdOn=" + getCreatedOn() + " details=" + getDetails() + '}';
    }

}
