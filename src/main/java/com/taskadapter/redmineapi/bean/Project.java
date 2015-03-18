package com.taskadapter.redmineapi.bean;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;

/**
 * Redmine's Project.
 */
public class Project implements Identifiable, Serializable {
	private static final long serialVersionUID = 4529305420978716446L;

	/**
	 * database ID
	 */
    private final Integer id;

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
     * This is the *database ID*, not a String-based key.
     */
    private Integer parentId;
    private Boolean projectPublic;

    private final Collection<CustomField> customFields = new HashSet<CustomField>();

    /**
     * Trackers available for this project
     */
    private final Collection<Tracker> trackers = new HashSet<Tracker>();

    Project(Integer id) {
        this.id = id;
    }

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
     * @return Trackers allowed in this project (e.g.: Bug, Feature, Support, Task, ...)
     */
    public Collection<Tracker> getTrackers() {
        return Collections.unmodifiableCollection(trackers);
    }

    public void addTrackers(Collection<Tracker> trackers) {
        this.trackers.addAll(trackers);
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
        return "Project{" +
                "id=" + id +
                ", identifier='" + identifier + '\'' +
                ", name='" + name + '\'' +
                '}';
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
     * return <strong>NULL</strong> with that redmine. This bug was fixed in redmine 1.2.1.
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

    /**
     *
     * @return true if the project is public, false if the project is private. 
     * Returns <code>null</code> if the project visibility was not specified or if the project was just retrieved from server.
     *
     * @since Redmine 2.6.0. see http://www.redmine.org/issues/17628 . this property is for writing only before Redmine 2.6.0.
     * The value is not returned by older Redmine versions.
     */
    @Deprecated
    public Boolean getProjectPublic() {
        return projectPublic;
    }
    
    public void setProjectPublic(Boolean projectPublic) {
        this.projectPublic = projectPublic;
    }
    
    public Collection<CustomField> getCustomFields() {
        return customFields;
    }

    public void addCustomFields(Collection<CustomField> customFields) {
        this.customFields.addAll(customFields);
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
