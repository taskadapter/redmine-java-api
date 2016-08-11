package com.taskadapter.redmineapi.bean;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * User or group membership.
 */
public class Membership implements Identifiable {

	private final PropertyStorage storage;

	/**
	 * database numeric ID.
	 */
	public final static Property<Integer> DATABASE_ID = new Property<>(Integer.class, "id");
	public final static Property<Project> PROJECT = new Property<>(Project.class, "project");
	/**
	 * User. Not set for "group" membership.
	 */
	public final static Property<Integer> USER_ID = new Property<>(Integer.class, "userId");
        public final static Property<String> USER_NAME = new Property<>(String.class, "userName");
        /**
         * Group. Not set for "user" membership.
         */
	public final static Property<Integer> GROUP_ID = new Property<>(Integer.class, "groupId");
        public final static Property<String> GROUP_NAME = new Property<>(String.class, "groupName");
	public final static Property<Set<Role>> ROLES = (Property<Set<Role>>) new Property(Set.class, "roles");

    /**
     * Use MembershipFactory to create instances of this class.
     *
     * @param id database ID.
     *
     * @see com.taskadapter.redmineapi.bean.MembershipFactory
     */
    Membership(Integer id) {
    	storage = new PropertyStorage();
		storage.set(ROLES, new HashSet<>());
        storage.set(DATABASE_ID, id);
    }

    @Override
	public Integer getId() {
		return storage.get(DATABASE_ID);
	}

	public Project getProject() {
		return storage.get(PROJECT);
	}

	public void setProject(Project project) {
		storage.set(PROJECT, project);
	}

	public Integer getUserId() {
		return storage.get(USER_ID);
	}

	public void setUserId(Integer id) {
		storage.set(USER_ID, id);
	}

    public Integer getGroupId() {
        return storage.get(GROUP_ID);
    }

    public void setGroupId(Integer id) {
		storage.set(GROUP_ID, id);
    }
    
	public String getUserName() {
		return storage.get(USER_NAME);
	}

	public void setUserName(String id) {
		storage.set(USER_NAME, id);
	}

    public String getGroupName() {
        return storage.get(GROUP_NAME);
    }

    public void setGroupName(String id) {
		storage.set(GROUP_NAME, id);
    }

    public Collection<Role> getRoles() {
		return Collections.unmodifiableCollection(storage.get(ROLES));
	}

	public void addRoles(Collection<Role> roles) {
		storage.get(ROLES).addAll(roles);
	}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Membership that = (Membership) o;

        if (getId() != null ? !getId().equals(that.getId()) : that.getId() != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return getId() != null ? getId().hashCode() : 0;
    }

    @Override
	public String toString() {
		return "Membership [id=" + getId() + ", project=" + getProject() + ", userId="
				+ getUserId() + ", groupId=" + getGroupId() + ", roles=" + getRoles() + "]";
	}

	public PropertyStorage getStorage() {
		return storage;
	}
}
