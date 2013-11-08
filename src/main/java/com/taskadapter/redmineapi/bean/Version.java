package com.taskadapter.redmineapi.bean;

import java.util.Date;

/**
 * Redmine's project version
 * <p>
 * REMARK: currently this is only used with Issues, so only id and name are filled
 */
public class Version implements Identifiable {
    /*
      * <id>1</id> <project name="Redmine" id="1"/> <name>0.7</name>
      * <description/> <status>closed</status> <due_date>2008-04-28</due_date>
      * <created_on>2008-03-09T12:52:06+01:00</created_on>
      * <updated_on>2009-11-15T12:22:12+01:00</updated_on>
      */
    private Integer id;
    private Project project;
    private String name;
    private String description;
    private String status;
    private Date dueDate;
    private Date createdOn;
    private Date updatedOn;

    /**
     * Required for reflective construction.
     */
    public Version() {
    }

    /**
     * @param project the {@link Project} of the {@link Version}
     * @param name    the name of the the {@link Version}
     */
    public Version(Project project, String name) {
        this.project = project;
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Version version = (Version) o;

        if (id != null ? !id.equals(version.id) : version.id != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    public Date getCreatedOn() {
        return createdOn;
    }

    public String getDescription() {
        return description;
    }

    public Date getDueDate() {
        return dueDate;
    }

    @Override
    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Project getProject() {
        return project;
    }

    public String getStatus() {
        return status;
    }

    public Date getUpdatedOn() {
        return updatedOn;
    }

    public void setCreatedOn(Date createdOn) {
        this.createdOn = createdOn;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setUpdatedOn(Date updatedOn) {
        this.updatedOn = updatedOn;
    }

    @Override
    public String toString() {
        return "Version [id=" + id + ", name=" + name + "]";
    }

}
