package com.taskadapter.redmineapi.bean;

import java.io.Serializable;

/**
 * Redmine issue category.
 *
 * @author Matthias Paul Scholz
 */
public class IssueCategory implements Identifiable, Serializable {
	private static final long serialVersionUID = -109010410391968475L;

	private Integer id;
    private String name;
    private Project project;
    private User assignee;

    /**
     * No-params constructor is required for reflective construction.
     */
    public IssueCategory() {
    }

    /**
     * Constructor with some mandatory fields.
     *
     * @param project the {@link Project}
     * @param name    the name
     */
    public IssueCategory(Project project, String name) {
        this.name = name;
        this.project = project;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public User getAssignee() {
        return assignee;
    }

    public void setAssignee(User assignee) {
        this.assignee = assignee;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        IssueCategory that = (IssueCategory) o;

        if (assignee != null ? !assignee.equals(that.assignee) : that.assignee != null) return false;
        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (project != null ? !project.equals(that.project) : that.project != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (project != null ? project.hashCode() : 0);
        result = 31 * result + (assignee != null ? assignee.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "IssueCategory{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", project=" + project +
                ", assignee=" + assignee +
                '}';
    }
}
