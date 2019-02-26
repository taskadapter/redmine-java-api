package com.taskadapter.redmineapi.bean;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

/**
 * User's role.
 */
public class Role {

    /**
     * database ID.
     */
    private Integer id;
	private String name;
	private Boolean inherited;

	private final Collection<String> permissions = new HashSet<>();

    /**
     * @param id database ID.
     */
    public Role setId(Integer id) {
        this.id = id;
        return this;
    }

    public Integer getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public Role setName(String name) {
		this.name = name;
		return this;
	}

	public Boolean getInherited() {
		return inherited;
	}

	public void setInherited(Boolean inherited) {
		this.inherited = inherited;
	}
	
	public Collection<String> getPermissions() {
        return Collections.unmodifiableCollection(permissions);
    }

    public void addPermissions(Collection<String> permissions) {
        this.permissions.addAll(permissions);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Role role = (Role) o;

        if (id != null ? !id.equals(role.id) : role.id != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
	public String toString() {
		return "Role [id=" + id + ", name=" + name + ", inherited=" + inherited
				+ "]";
	}
}
