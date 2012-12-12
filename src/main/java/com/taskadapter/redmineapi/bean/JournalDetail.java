package com.taskadapter.redmineapi.bean;

import java.io.Serializable;

/**
 * Redmine journal entry field. Actual set of fields depends on a redmine
 * responce and probably is not documented anywhere.
 * 
 */
public class JournalDetail implements Serializable {
    private static final long serialVersionUID = -9170064829669555245L;

    private String newValue;
    private String name;
    private String property;
    private String oldValue;

    public String getNewValue() {
        return newValue;
    }

    public void setNewValue(String newValue) {
        this.newValue = newValue;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public String getOldValue() {
        return oldValue;
    }

    public void setOldValue(String oldValue) {
        this.oldValue = oldValue;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result
                + ((newValue == null) ? 0 : newValue.hashCode());
        result = prime * result
                + ((oldValue == null) ? 0 : oldValue.hashCode());
        result = prime * result
                + ((property == null) ? 0 : property.hashCode());
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
        JournalDetail other = (JournalDetail) obj;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (newValue == null) {
            if (other.newValue != null)
                return false;
        } else if (!newValue.equals(other.newValue))
            return false;
        if (oldValue == null) {
            if (other.oldValue != null)
                return false;
        } else if (!oldValue.equals(other.oldValue))
            return false;
        if (property == null) {
            if (other.property != null)
                return false;
        } else if (!property.equals(other.property))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "JournalDetail [newValue=" + newValue + ", name=" + name
                + ", property=" + property + ", oldValue=" + oldValue + "]";
    }
}
