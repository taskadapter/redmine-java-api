package com.taskadapter.redmineapi.bean;

import java.util.Date;

/**
 * Repository Change for a Redmine issue
 */
public class Changeset {
    private String revision;
    private User user;
    private String comments;
    private Date commitedOn;

    public String getRevision() {
        return revision;
    }

    public void setRevision(String revision) {
        this.revision = revision;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public Date getCommitedOn() {
        return commitedOn;
    }

    public void setCommitedOn(Date commitedOn) {
        this.commitedOn = commitedOn;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Changeset changeset = (Changeset) o;

        if (revision != null ? !revision.equals(changeset.revision) : changeset.revision != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return revision != null ? revision.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Changeset{" +
                "comments='" + comments + '\'' +
                ", revision='" + revision + '\'' +
                ", user=" + user +
                ", commitedOn=" + commitedOn +
                '}';
    }

}
