package com.taskadapter.redmineapi.bean;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

/**
 * User or group membership.
 */
public class Membership implements Identifiable {

    /**
     * database ID.
     */
    private final Integer id;

	private Project project;

	/**
	 * User. Not set for "group" membership.
	 */
	private User user;

    private Group group;

	private final Collection<Role> roles = new HashSet<Role>();

    /**
     * Use MembershipFactory to create instances of this class.
     *
     * @param id database ID.
     *
     * @see com.taskadapter.redmineapi.bean.MembershipFactory
     */
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

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    public Collection<Role> getRoles() {
		return Collections.unmodifiableCollection(roles);
	}

	public void addRoles(Collection<Role> roles) {
		this.roles.addAll(roles);
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
