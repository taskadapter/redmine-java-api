package com.taskadapter.redmineapi.bean;

import java.io.Serializable;

/**
 * Redmine Issue Status ("new", "in progress" etc)
 */
public class IssueStatus implements Identifiable, Serializable {
	private static final long serialVersionUID = -2221390098554222099L;
	private Integer id;
    private String name;
    private boolean defaultStatus = false;
    private boolean closed = false;

    /**
     * No-params constructor is required for reflective construction.
     */
    public IssueStatus() {
    }

    public IssueStatus(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + (closed ? 1231 : 1237);
        result = prime * result + (defaultStatus ? 1231 : 1237);
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        IssueStatus other = (IssueStatus) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (closed != other.closed)
            return false;
        if (defaultStatus != other.defaultStatus)
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "Status [id=" + id + ", name=" + name + ", isDefault=" + defaultStatus + ", closed=" + closed + "]";
    }

}
