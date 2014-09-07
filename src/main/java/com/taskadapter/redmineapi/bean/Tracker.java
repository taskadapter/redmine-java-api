package com.taskadapter.redmineapi.bean;

import java.io.Serializable;

/**
 * Redmine's Tracker (bug/feature/task/...)
 */
public class Tracker implements Identifiable, Serializable {

    /**
     * database ID.
     */
    private final Integer id;

    private String name;

    /**
     * Use TrackerFactory to create instances of this class.
     *
     * @param id database ID.
     *
     * @see com.taskadapter.redmineapi.bean.TrackerFactory
     */
    Tracker(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Tracker tracker = (Tracker) o;

        if (id != null ? !id.equals(tracker.id) : tracker.id != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
