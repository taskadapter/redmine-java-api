package com.taskadapter.redmineapi.bean;

import java.io.Serializable;

/**
 * Redmine issue category.
 */
public class IssueCategory implements Identifiable, Serializable {
	private static final long serialVersionUID = -109010410391968475L;

    private final PropertyStorage storage;

    /**
     * database numeric ID.
     */
    public final static Property<Integer> DATABASE_ID = new Property<>(Integer.class, "id");
    public final static Property<String> NAME = new Property<>(String.class, "name");
    public final static Property<Integer> PROJECT_ID = new Property<>(Integer.class, "projectId");
    public final static Property<Integer> ASSIGNEE_ID = new Property<>(Integer.class, "assigneeId");
    public final static Property<String> ASSIGNEE_NAME = new Property<>(String.class, "assigneeName");

    /**
     * @param id database ID.
     */
    IssueCategory(Integer id) {
        storage = new PropertyStorage();
        storage.set(DATABASE_ID, id);
    }

    public Integer getId() {
        return storage.get(DATABASE_ID);
    }

    public String getName() {
        return storage.get(NAME);
    }

    public void setName(String name) {
        storage.set(NAME, name);
    }

    public Integer getProjectId() {
        return storage.get(PROJECT_ID);
    }

    public void setProjectId(Integer projectId) {
        storage.set(PROJECT_ID, projectId);
    }

    /**
     * Redmine can be configured to allow group assignments for issues:
     * Configuration option: Settings -> Issue Tracking -> Allow issue assignment to groups
     *
     * <p>An assignee can be a user or a group</p>
     */
    public Integer getAssigneeId() {
        return storage.get(ASSIGNEE_ID);
    }

    public void setAssigneeId(Integer assigneeId) {
        storage.set(ASSIGNEE_ID, assigneeId);
    }

    public String getAssigneeName() {
        return storage.get(ASSIGNEE_NAME);
    }

    public void setAssigneeName(String assigneeName) {
        storage.set(ASSIGNEE_NAME, assigneeName);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        IssueCategory that = (IssueCategory) o;

        if (getId() != null ? !getId().equals(that.getId()) : that.getId() != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return getId() != null ? getId().hashCode() : 0;
    }

    @Override
    public String toString() {
        return "IssueCategory{" +
                "id=" + getId() +
                ", name='" + getName() + '\'' +
                ", projectId=" + getProjectId() +
                ", assigneeId=" + getAssigneeId() +
                ", assigneeName='" + getAssigneeName() + '\'' +
                '}';
    }

    public PropertyStorage getStorage() {
        return storage;
    }
}
