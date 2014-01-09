package com.taskadapter.redmineapi.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Redmine's Project.
 */
public class Project implements Identifiable, Serializable {
	private static final long serialVersionUID = 4529305420978716446L;

	/**
	 * database ID
	 */
    private Integer id;

    /**
     * String "identifier" (human-readable name without spaces and other extra stuff)
     */
    private String identifier;

    /**
     * Can contain any symbols
     */
    private String name;

    private String description;

    private String homepage;

    private Date createdOn;

    private Date updatedOn;

    /**
     * Trackers available for this project
     */
    private List<Tracker> trackers;

    /**
     * This is the *database ID*, not a String-based key.
     */
    private Integer parentId;

    private List<CustomField> customFields = new ArrayList<CustomField>();

    public String getHomepage() {
        return homepage;
    }

    public void setHomepage(String homepage) {
        this.homepage = homepage;
    }

    /**
     * @return project's string "key" (not a numeric database id!). Example: "project_ABC"
     */
    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    @Override
    /**
     * @return numeric database ID
     */
    public Integer getId() {
        return id;
    }

    /**
     * @param id numeric database ID
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * @return project name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the project name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return list of Trackers allowed in this project (e.g.: Bug, Feature, Support, Task, ...)
     */
    public List<Tracker> getTrackers() {
        return trackers;
    }

    public void setTrackers(List<Tracker> trackers) {
        this.trackers = trackers;
    }

    public Tracker getTrackerByName(String trackerName) {
        if (this.trackers == null) return null;
        for (Tracker t : this.trackers) {
            if (t.getName().equals(trackerName)) return t;
        }
        return null;
    }

    @Override
    public String toString() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    /**
     * Redmine's REST API "get project" operation does NOT return the 
     * parent project ID in redmine 1.1.2 (and maybe earlier). Which means 
     * calling getParentId() of the project loaded from Redmine server will
     * return <b>NULL</b> with that redmine. This bug was fixed in redmine 1.2.1.
     * See bug http://www.redmine.org/issues/8229
     * 
     *
     * @return the parent project Id if it was set programmatically or NULL (!!!) if the project was loaded from the server.
     */
    public Integer getParentId() {
        return parentId;
    }

    public void setParentId(Integer parentId) {
        this.parentId = parentId;
    }

    public List<CustomField> getCustomFields() {
        return customFields;
    }

    public void setCustomFields(List<CustomField> customFields) {
        this.customFields = customFields;
    }

    public CustomField getCustomFieldById(int customFieldId) {
        for (CustomField customField : customFields) {
            if (customFieldId == customField.getId()) {
                return customField;
            }
        }
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Project project = (Project) o;

        if (id != null ? !id.equals(project.id) : project.id != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
