package com.taskadapter.redmineapi.bean;

import java.util.ArrayList;
import java.util.List;

/**
 * User or group membership.
 */
public class Membership implements Identifiable {
	private final Integer id;

	private Project project;

	/**
	 * User. Not set for "group" membership.
	 */
	private User user;
	private List<Role> roles = new ArrayList<Role>();

    Membership(Integer id) {
        this.id = id;
    }

    @Override
	public Integer getId() {
		return id;
	}

	public Project getProject() {
		return project;
	}

	public void setProject(Project project) {
		this.project = project;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public List<Role> getRoles() {
		return roles;
	}

	public void setRoles(List<Role> roles) {
		this.roles = roles;
	}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Membership that = (Membership) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
	public String toString() {
		return "Membership [id=" + id + ", project=" + project + ", user="
				+ user + ", roles=" + roles + "]";
	}
}
