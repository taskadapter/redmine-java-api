
package com.taskadapter.redmineapi.bean;

import java.util.Objects;

public class GenericAssignee implements Assignee {
    private int id;
    private String name;

    GenericAssignee() {
    }

    public GenericAssignee(int id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public Integer getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String getName() {
        if( name == null ) {
            return String.format("Assignee ID %d", id);
        } else {
            return name;
        }
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + this.id;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Assignee)) {
            return false;
        }
        final Assignee other = (Assignee) obj;
        
        return Objects.equals(getId(), other.getId());
    }
}
