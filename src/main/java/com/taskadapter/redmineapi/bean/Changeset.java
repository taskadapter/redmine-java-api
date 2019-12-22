package com.taskadapter.redmineapi.bean;

import java.util.Date;

/**
 * Repository Change for a Redmine issue
 */
public class Changeset {


    private final PropertyStorage storage;

    public final static Property<String> REVISION = new Property<>(String.class, "revision");
    public final static Property<User> USER = new Property<>(User.class, "user");
    public final static Property<String> COMMENTS = new Property<>(String.class, "comments");
    public final static Property<Date> COMMITTED_ON = new Property<>(Date.class, "committedOn");

    public Changeset() {
        storage = new PropertyStorage();
    }

    public String getRevision() {
        return storage.get(REVISION);
    }

    public void setRevision(String revision) {
        storage.set(REVISION, revision);
    }

    public User getUser() {
        return storage.get(USER);
    }

    public void setUser(User user) {
        storage.set(USER, user);
    }

    public String getComments() {
        return storage.get(COMMENTS);
    }

    public void setComments(String comments) {
        storage.set(COMMENTS, comments);
    }

    public Date getCommittedOn() {
        return storage.get(COMMITTED_ON);
    }

    public void setCommittedOn(Date committedOn) {
        storage.set(COMMITTED_ON, committedOn);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Changeset changeset = (Changeset) o;

        if (getRevision() != null ? !getRevision().equals(changeset.getRevision()) : changeset.getRevision() != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return getRevision() != null ? getRevision().hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Changeset{" +
                "comments='" + getComments() + '\'' +
                ", revision='" + getRevision() + '\'' +
                ", user=" + getUser() +
                ", committedOn=" + getCommittedOn() +
                '}';
    }

}
