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

    public News() {
        super();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer aId) {
        this.id = aId;
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
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        News other = (News) obj;
        if (createdOn == null) {
            if (other.createdOn != null)
                return false;
        } else if (!createdOn.equals(other.createdOn))
            return false;
        if (description == null) {
            if (other.description != null)
                return false;
        } else if (!description.equals(other.description))
            return false;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (title == null) {
            if (other.title != null)
                return false;
        } else if (!title.equals(other.title))
            return false;
        if (user == null) {
            if (other.user != null)
                return false;
        } else if (!user.equals(other.user))
            return false;
        if (project == null) {
            if (other.project != null)
                return false;
        } else if (!project.equals(other.project))
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((createdOn == null) ? 0 : createdOn.hashCode());
        result = prime * result + ((description == null) ? 0 : description.hashCode());
        result = prime * result + ((user == null) ? 0 : user.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((title == null) ? 0 : title.hashCode());
        result = prime * result + ((project == null) ? 0 : project.hashCode());

        return result;
    }

    @Override
    public String toString() {
        return "News [id=" + id + ", title=" + title + "]";
    }
}
