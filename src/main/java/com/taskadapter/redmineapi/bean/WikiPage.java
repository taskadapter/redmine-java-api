package com.taskadapter.redmineapi.bean;

import java.util.Date;

public class WikiPage {

    private String title;
    private Integer version;
    private Date createdOn;
    private Date updatedOn;

    WikiPage() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public Date getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Date createdOn) {
        this.createdOn = createdOn;
    }

    public Date getUpdatedOn() {
        return updatedOn;
    }

    public void setUpdatedOn(Date updatedOn) {
        this.updatedOn = updatedOn;
    }

    @Override
    public String toString() {
        return "WikiPage{" +
                "title='" + title + '\'' +
                ", version=" + version +
                ", createdOn=" + createdOn +
                ", updatedOn=" + updatedOn +
                '}';
    }
}