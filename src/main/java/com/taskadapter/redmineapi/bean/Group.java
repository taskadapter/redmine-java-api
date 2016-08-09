package com.taskadapter.redmineapi.bean;

public class Group implements Identifiable {

    private final PropertyStorage storage;

    public final static Property<Integer> ID = new Property<Integer>(Integer.class, "id");
    public final static Property<String> NAME = new Property<String>(String.class, "name");

    /**
     * Use GroupFactory to create instances of this class.
     *
     * @param id database ID.
     */
    Group(Integer id) {
        storage = new PropertyStorage();
        storage.set(ID, id);
    }

    public Group(PropertyStorage storage) {
        this.storage = storage;
    }

    @Override
    public Integer getId() {
        return storage.get(ID);
    }

    public String getName() {
        return storage.get(NAME);
    }

    public void setName(String name) {
        storage.set(NAME, name);
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
}
