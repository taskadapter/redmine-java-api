package com.taskadapter.redmineapi.bean;

import java.util.Date;

/**
 * Repository Change for a Redmine issue
 * @author Martin Kurz
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
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((comments == null) ? 0 : comments.hashCode());
        result = prime * result + ((commitedOn == null) ? 0 : commitedOn.hashCode());
        result = prime * result + ((revision == null) ? 0 : revision.hashCode());
        result = prime * result + ((user == null) ? 0 : user.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Changeset other = (Changeset) obj;
        if (comments == null) {
            if (other.comments != null) {
                return false;
            }
        } else if (!comments.equals(other.comments)) {
            return false;
        }
        if (commitedOn == null) {
            if (other.commitedOn != null) {
                return false;
            }
        } else if (!commitedOn.equals(other.commitedOn)) {
            return false;
        }
        if (revision == null) {
            if (other.revision != null) {
                return false;
            }
        } else if (!revision.equals(other.revision)) {
            return false;
        }
        if (user == null) {
            if (other.user != null) {
                return false;
            }
        } else if (!user.equals(other.user)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Changeset [revision=");
        builder.append(revision);
        builder.append(", user=");
        builder.append(user);
        builder.append(", comments=");
        builder.append(comments);
        builder.append(", commitedOn=");
        builder.append(commitedOn);
        builder.append("]");
        return builder.toString();
    }

}
