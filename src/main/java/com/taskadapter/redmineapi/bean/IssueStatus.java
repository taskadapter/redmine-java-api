package com.taskadapter.redmineapi.bean;

import java.io.Serializable;

/**
 * Redmine Issue Status ("new", "in progress", etc)
 */
public class IssueStatus implements Identifiable, Serializable {
	private static final long serialVersionUID = -2221390098554222099L;

	private Integer id;
    private String name;
    private boolean defaultStatus = false;
    private boolean closed = false;

    public IssueStatus setId(Integer id) {
        this.id = id;
        return this;
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public IssueStatus setName(String name) {
        this.name = name;
        return this;
    }

    public boolean isDefaultStatus() {
        return defaultStatus;
    }

    public void setDefaultStatus(boolean defaultStatus) {
        this.defaultStatus = defaultStatus;
    }

    public boolean isClosed() {
        return closed;
    }

    public void setClosed(boolean closed) {
        this.closed = closed;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        IssueStatus that = (IssueStatus) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Status [id=" + id + ", name=" + name + ", isDefault=" + defaultStatus + ", closed=" + closed + "]";
    }

}
