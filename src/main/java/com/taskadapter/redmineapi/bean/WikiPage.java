package com.taskadapter.redmineapi.bean;

import java.util.Date;

public class WikiPage {
    protected final PropertyStorage storage;

    public final static Property<String> TITLE = new Property<>(String.class, "title");
    public final static Property<Integer> VERSION = new Property<>(Integer.class, "version");
    public final static Property<Date> CREATED_ON = new Property<>(Date.class, "createdOn");
    public final static Property<Date> UPDATED_ON = new Property<>(Date.class, "updatedOn");

    WikiPage() {
        storage = new PropertyStorage();
    }

    public String getTitle() {
        return storage.get(TITLE);
    }

    public void setTitle(String title) {
        storage.set(TITLE, title);
    }

    public Integer getVersion() {
        return storage.get(VERSION);
    }

    public void setVersion(Integer version) {
        storage.set(VERSION, version);
    }

    public Date getCreatedOn() {
        return storage.get(CREATED_ON);
    }

    public void setCreatedOn(Date createdOn) {
        storage.set(CREATED_ON, createdOn);
    }

    public Date getUpdatedOn() {
        return storage.get(UPDATED_ON);
    }

    public void setUpdatedOn(Date updatedOn) {
        storage.set(UPDATED_ON, updatedOn);
    }

    @Override
    public String toString() {
        return "WikiPage{" +
                "title='" + getTitle() + '\'' +
                ", version=" + getVersion() +
                ", createdOn=" + getCreatedOn() +
                ", updatedOn=" + getUpdatedOn() +
                '}';
    }
}