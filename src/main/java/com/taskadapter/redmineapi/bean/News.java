package com.taskadapter.redmineapi.bean;

import java.io.Serializable;
import java.util.Date;

public class News implements Identifiable, Serializable {
    private static final long serialVersionUID = 1L;

    private Integer id;
    private Project project;
    private User user;
    private String title;
    private String description;
    private Date createdOn;
    private String link;

    public News setId(Integer id) {
        this.id = id;
        return this;
    }

    public Integer getId() {
        return id;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project aProject) {
        this.project = aProject;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User aUser) {
        this.user = aUser;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String aTitle) {
        this.title = aTitle;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String aDescription) {
        this.description = aDescription;
    }

    public Date getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Date aCreated) {
        this.createdOn = aCreated;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String aLink) {
        this.link = aLink;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        News news = (News) o;

        if (id != null ? !id.equals(news.id) : news.id != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "News [id=" + id + ", title=" + title + "]";
    }
}
