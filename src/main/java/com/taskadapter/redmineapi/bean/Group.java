package com.taskadapter.redmineapi.bean;

import com.taskadapter.redmineapi.RedmineException;
import com.taskadapter.redmineapi.internal.Transport;

public class Group implements Identifiable, FluentStyle {

    private final PropertyStorage storage = new PropertyStorage();

    public final static Property<Integer> ID = new Property<Integer>(Integer.class, "id");
    public final static Property<String> NAME = new Property<String>(String.class, "name");
    private Transport transport;

    public Group(Transport transport) {
        setTransport(transport);
    }

    /**
     * @param id database ID of the group
     */
    public Group setId(Integer id) {
        storage.set(ID, id);
        return this;
    }

    @Override
    public Integer getId() {
        return storage.get(ID);
    }

    public String getName() {
        return storage.get(NAME);
    }

    public Group setName(String name) {
        storage.set(NAME, name);
        return this;
    }

    @Override
    public String toString() {
        return "Group{" +
                "id=" + getId() +
                ", name='" + getName() + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Group group = (Group) o;

        if (getId() != null ? !getId().equals(group.getId()) : group.getId() != null) return false;
        return true;
    }

    @Override
    public int hashCode() {
        return getId() != null ? getId().hashCode() : 0;
    }

    public PropertyStorage getStorage() {
        return storage;
    }

    /**
     * Creates a new group.
     * <p><strong>This operation requires "Redmine Administrator" permission.</strong>
     *
     * @return created group.
     */
    public Group create() throws RedmineException {
        return transport.addObject(this);
    }

    public void update() throws RedmineException {
        transport.updateObject(this);
    }

    /**
     * Delete this group. The object must have Id set.
     * <p><strong>This operation requires "Redmine Administrator" permission.</strong>
     */
    public void delete() throws RedmineException {
        transport.deleteObject(Group.class, getId().toString());
    }

    @Override
    public void setTransport(Transport transport) {
        this.transport = transport;
    }
}
