package com.taskadapter.redmineapi.bean;

import java.util.Objects;

public class Group implements Identifiable, Assignee {
	
    private final Integer id;
    private String name;

    /**
     * Use GroupFactory to create instances of this class.
     *
     * @param id database ID.
     */
    Group(Integer id) {
        this.id = id;
    }

    @Override
    public Integer getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Group{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        
        if (o == null || (! (o instanceof Assignee)))  {
            return false;
        }

        Assignee assignee = (Assignee) o;
        
        return Objects.equals(getId(), assignee.getId());
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
