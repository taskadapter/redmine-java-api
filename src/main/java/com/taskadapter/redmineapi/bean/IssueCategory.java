package com.taskadapter.redmineapi.bean;

import com.taskadapter.redmineapi.NotFoundException;
import com.taskadapter.redmineapi.RedmineAuthenticationException;
import com.taskadapter.redmineapi.RedmineException;
import com.taskadapter.redmineapi.internal.Transport;

import java.io.Serializable;

/**
 * Redmine issue category.
 */
public class IssueCategory implements Identifiable, Serializable, FluentStyle {
	private static final long serialVersionUID = -109010410391968475L;

    private final PropertyStorage storage = new PropertyStorage();

    /**
     * database numeric ID.
     */
    public final static Property<Integer> DATABASE_ID = new Property<>(Integer.class, "id");
    public final static Property<String> NAME = new Property<>(String.class, "name");
    public final static Property<Integer> PROJECT_ID = new Property<>(Integer.class, "projectId");
    public final static Property<Integer> ASSIGNEE_ID = new Property<>(Integer.class, "assigneeId");
    public final static Property<String> ASSIGNEE_NAME = new Property<>(String.class, "assigneeName");
    private Transport transport;

    public IssueCategory(Transport transport) {
        setTransport(transport);
    }

    public IssueCategory(Transport transport, Integer projectId, String name) {
        setTransport(transport);
        setProjectId(projectId);
        setName(name);
    }

    public Integer getId() {
        return storage.get(DATABASE_ID);
    }

    public String getName() {
        return storage.get(NAME);
    }

    public IssueCategory setName(String name) {
        storage.set(NAME, name);
        return this;
    }

    /**
     * @param id database Id of the issue category.
     */
    public IssueCategory setId(Integer id) {
        storage.set(DATABASE_ID, id);
        return this;
    }

    public Integer getProjectId() {
        return storage.get(PROJECT_ID);
    }

    public IssueCategory setProjectId(Integer projectId) {
        storage.set(PROJECT_ID, projectId);
        return this;
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

    public IssueCategory setAssigneeId(Integer assigneeId) {
        storage.set(ASSIGNEE_ID, assigneeId);
        return this;
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

    @Override
    public void setTransport(Transport transport) {
        this.transport = transport;
    }

    /**
     * creates a new {@link IssueCategory} for the {@link Project} contained. <br>
     * Pre-condition: the attribute {@link Project} for the {@link IssueCategory} must
     * not be null!
     *
     * @return the new {@link IssueCategory} created by Redmine
     * @throws IllegalArgumentException       thrown in case the category does not contain a project.
     * @throws RedmineAuthenticationException thrown in case something went wrong while trying to login
     * @throws RedmineException               thrown in case something went wrong in Redmine
     * @throws NotFoundException              thrown in case an object can not be found
     */
    public IssueCategory create() throws RedmineException {
        if (getProjectId() == null) {
            throw new IllegalArgumentException(
                    "IssueCategory must contain projectId");
        }

        return transport.addChildEntry(Project.class, getProjectId().toString(), this);
    }

    public void delete() throws RedmineException {
        transport.deleteObject(IssueCategory.class, Integer.toString(this.getId()));
    }
}
