package com.taskadapter.redmineapi.bean;

import java.io.Serializable;

/**
 * Redmine's Tracker (bug/feature/task/...)
 */
public class Tracker implements Identifiable, Serializable {

    private Integer id;
    private String name;

    public Tracker() {
    }

    public Tracker(Integer id, String name) {
        super();
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
