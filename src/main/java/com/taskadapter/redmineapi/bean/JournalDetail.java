package com.taskadapter.redmineapi.bean;

import java.io.Serializable;

/**
 * Redmine journal entry field. Actual set of fields depends on a redmine
 * response and probably is not documented anywhere.
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JournalDetail that = (JournalDetail) o;

        if (newValue != null ? !newValue.equals(that.newValue) : that.newValue != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return newValue != null ? newValue.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "JournalDetail [newValue=" + newValue + ", name=" + name
                + ", property=" + property + ", oldValue=" + oldValue + "]";
    }
}
