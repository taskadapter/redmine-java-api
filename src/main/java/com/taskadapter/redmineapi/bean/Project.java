package com.taskadapter.redmineapi.bean;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Redmine's Project.
 */
public class Project implements Identifiable, Serializable {
	private static final long serialVersionUID = 4529305420978716446L;

    private final PropertyStorage storage;

    /**
     * database ID
     */
    public final static Property<Integer> DATABASE_ID = new Property<>(Integer.class, "id");

    /**
     * String "identifier" (human-readable name without spaces and other extra stuff)
     */
    public final static Property<String> STRING_IDENTIFIER = new Property<>(String.class, "identifier");

    /**
     * Can contain any symbols
     */
    public final static Property<String> NAME = new Property<>(String.class, "name");
    public final static Property<String> DESCRIPTION = new Property<>(String.class, "description");
    public final static Property<String> HOMEPAGE = new Property<>(String.class, "homepage");
    public final static Property<Date> CREATED_ON = new Property<>(Date.class, "createdOn");
    public final static Property<Date> UPDATED_ON = new Property<>(Date.class, "updatedOn");

    /**
     * This is the *database ID*, not a String-based key.
     */
    public final static Property<Integer> PARENT_DATABASE_ID = new Property<>(Integer.class, "parentId");
    public final static Property<Boolean> PUBLIC = new Property<>(Boolean.class, "public");
    public final static Property<Boolean> INHERIT_MEMBERS = new Property<>(Boolean.class, "inheritMembers");
    public final static Property<Set<CustomField>> CUSTOM_FIELDS = (Property<Set<CustomField>>) new Property(Set.class, "customFields");

    /**
     * Trackers available for this project
     */
    public final static Property<Set<Tracker>> TRACKERS = (Property<Set<Tracker>>) new Property(Set.class, "trackers");

    Project(Integer id) {
        storage = new PropertyStorage();
        storage.set(DATABASE_ID, id);
        storage.set(CUSTOM_FIELDS, new HashSet<>());
        storage.set(TRACKERS, new HashSet<>());
    }

    public String getHomepage() {
        return storage.get(HOMEPAGE);
    }

    public void setHomepage(String homepage) {
        storage.set(HOMEPAGE, homepage);
    }

    /**
     * @return project's string "key" (not a numeric database id!). Example: "project_ABC"
     */
    public String getIdentifier() {
        return storage.get(STRING_IDENTIFIER);
    }

    public void setIdentifier(String identifier) {
        storage.set(STRING_IDENTIFIER, identifier);
    }

    /**
     * @return numeric database ID
     */
    @Override
    public Integer getId() {
        return storage.get(DATABASE_ID);
    }

    /**
     * @return project name
     */
    public String getName() {
        return storage.get(NAME);
    }

    /**
     * @param name the project name
     */
    public void setName(String name) {
        storage.set(NAME, name);
    }

    /**
     * @return Trackers allowed in this project (e.g.: Bug, Feature, Support, Task, ...)
     */
    public Collection<Tracker> getTrackers() {
        return Collections.unmodifiableCollection(storage.get(TRACKERS));
    }

    public void addTrackers(Collection<Tracker> trackers) {
        storage.get(TRACKERS).addAll(trackers);
    }

    public Tracker getTrackerByName(String trackerName) {
        for (Tracker t : getTrackers()) {
            if (t.getName().equals(trackerName)) return t;
        }
        return null;
    }

    @Override
    public String toString() {
        return "Project{" +
                "id=" + getId() +
                ", identifier='" + getIdentifier() + '\'' +
                ", name='" + getName() + '\'' +
                '}';
    }

    public String getDescription() {
        return storage.get(DESCRIPTION);
    }

    public void setDescription(String description) {
        storage.set(DESCRIPTION, description);
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
        return storage.get(PARENT_DATABASE_ID);
    }

    public void setParentId(Integer parentId) {
        storage.set(PARENT_DATABASE_ID, parentId);
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
        return storage.get(PUBLIC);
    }
    
    public void setInheritMembers(Boolean inheritMembers) {
        storage.set(INHERIT_MEMBERS, inheritMembers);
    }

    public Boolean getInheritMembers() {
        return storage.get(INHERIT_MEMBERS);
    }

    public void setProjectPublic(Boolean projectPublic) {
        storage.set(PUBLIC, projectPublic);
    }

    public Collection<CustomField> getCustomFields() {
        return storage.get(CUSTOM_FIELDS);
    }

    public void addCustomFields(Collection<CustomField> customFields) {
        storage.get(CUSTOM_FIELDS).addAll(customFields);
    }

    public CustomField getCustomFieldById(int customFieldId) {
        for (CustomField customField : getCustomFields()) {
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

        if (getId() != null ? !getId().equals(project.getId()) : project.getId() != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return getId() != null ? getId().hashCode() : 0;
    }

    public PropertyStorage getStorage() {
        return storage;
    }
}
