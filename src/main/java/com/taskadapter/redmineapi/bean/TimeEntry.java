package com.taskadapter.redmineapi.bean;

import com.taskadapter.redmineapi.RedmineException;
import com.taskadapter.redmineapi.internal.Transport;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class TimeEntry implements Identifiable, FluentStyle {

    private final PropertyStorage storage = new PropertyStorage();

    /**
     * database numeric Id
     */
    public final static Property<Integer> DATABASE_ID = new Property<>(Integer.class, "id");

    /**
     * database Id of the Issue
     */
    public final static Property<Integer> ISSUE_ID = new Property<>(Integer.class, "issueId");

    /**
     * database Id of the project
     */
    public final static Property<Integer> PROJECT_ID = new Property<>(Integer.class, "projectId");

    public final static Property<String> PROJECT_NAME = new Property<>(String.class, "projectName");
    public final static Property<String> USER_NAME = new Property<>(String.class, "userName");
    public final static Property<Integer> USER_ID = new Property<>(Integer.class, "userId");
    public final static Property<String> ACTIVITY_NAME = new Property<>(String.class, "activityName");
    public final static Property<Integer> ACTIVITY_ID = new Property<>(Integer.class, "activityId");
    public final static Property<Float> HOURS = new Property<>(Float.class, "hours");
    public final static Property<String> COMMENT = new Property<>(String.class, "comment");
    public final static Property<Date> SPENT_ON = new Property<>(Date.class, "spentOn");
    public final static Property<Date> CREATED_ON = new Property<>(Date.class, "createdOn");
    public final static Property<Date> UPDATED_ON = new Property<>(Date.class, "updatedOn");
    public static final Property<Set<CustomField>> CUSTOM_FIELDS = new Property(Set.class, "custom_fields");
    private Transport transport;

    public TimeEntry(Transport transport) {
        setTransport(transport);
        storage.set(CUSTOM_FIELDS, new HashSet<>());
    }

    /**
     * @param id database Id
     */
    public TimeEntry setId(Integer id) {
        storage.set(DATABASE_ID, id);
        return this;
    }

    public Integer getId() {
        return storage.get(DATABASE_ID);
    }

    public Integer getUserId() {
        return storage.get(USER_ID);
    }

    public TimeEntry setUserId(Integer userId) {
        storage.set(USER_ID, userId);
        return this;
    }

    public Integer getIssueId() {
        return storage.get(ISSUE_ID);
    }

    public TimeEntry setIssueId(Integer issueId) {
        storage.set(ISSUE_ID, issueId);
        return this;
    }

    public Integer getProjectId() {
        return storage.get(PROJECT_ID);
    }

    public TimeEntry setProjectId(Integer projectId) {
        storage.set(PROJECT_ID, projectId);
        return this;
    }

    public String getProjectName() {
        return storage.get(PROJECT_NAME);
    }

    public void setProjectName(String projectName) {
        storage.set(PROJECT_NAME, projectName);
    }

    public String getActivityName() {
        return storage.get(ACTIVITY_NAME);
    }

    public void setActivityName(String activityName) {
        storage.set(ACTIVITY_NAME, activityName);
    }

    public Integer getActivityId() {
        return storage.get(ACTIVITY_ID);
    }

    public TimeEntry setActivityId(Integer activityId) {
        storage.set(ACTIVITY_ID, activityId);
        return this;
    }

    public Float getHours() {
        return storage.get(HOURS);
    }

    public TimeEntry setHours(Float hours) {
        storage.set(HOURS, hours);
        return this;
    }

    public String getComment() {
        return storage.get(COMMENT);
    }

    public TimeEntry setComment(String comment) {
        storage.set(COMMENT, comment);
        return this;
    }

    public Date getSpentOn() {
        return storage.get(SPENT_ON);
    }

    public TimeEntry setSpentOn(Date spentOn) {
        storage.set(SPENT_ON, spentOn);
        return this;
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

    public String getUserName() {
        return storage.get(USER_NAME);
    }

    public void setUserName(String userName) {
        storage.set(USER_NAME, userName);
    }

    /**
     * @return the custom field with given name or NULL if the field is not found
     */
    public CustomField getCustomField(String name) {
        return storage.get(CUSTOM_FIELDS).stream().filter(a -> a.getName().equals(name)).findFirst().orElse(null);
    }

    @Override
    public String toString() {
        return "User \"" + getUserName() + "\" spent " + getHours()
                + " hours on task " + getIssueId() + " (project \"" + getProjectName()
                + "\") doing " + getActivityName();
    }

    public Set<CustomField> getCustomFields() {
        return storage.get(CUSTOM_FIELDS);
    }

    public void clearCustomFields() {
        storage.set(CUSTOM_FIELDS, new HashSet<>());
    }

    public void addCustomFields(Collection<CustomField> customFields) {
        storage.get(CUSTOM_FIELDS).addAll(customFields);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TimeEntry timeEntry = (TimeEntry) o;

        if (getId() != null ? !getId().equals(timeEntry.getId()) : timeEntry.getId() != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return getId() != null ? getId().hashCode() : 0;
    }

    public boolean isValid() {
        return (getHours() != null) && (getProjectId() != null || getIssueId() != null);
    }

    public PropertyStorage getStorage() {
        return storage;
    }

    @Override
    public void setTransport(Transport transport) {
        this.transport = transport;
        PropertyStorageUtil.updateCollections(storage, transport);
    }

    public TimeEntry create() throws RedmineException {
        validate(this);
        return transport.addObject(this);
    }


    public void update() throws RedmineException {
        validate(this);
        transport.updateObject(this);
    }


    public void delete() throws RedmineException {
        transport.deleteObject(TimeEntry.class, Integer.toString(getId()));
    }

    private static void validate(TimeEntry obj) {
        if (!obj.isValid()) {
            throw new IllegalArgumentException("You have to either define a Project or Issue ID for a Time Entry. "
                    + "The given Time Entry object has neither defined.");
        }
    }
}
