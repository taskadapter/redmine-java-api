package com.taskadapter.redmineapi.bean;

public class GroupFactory {
    /**
     * For new objects not added to Redmine yet. the ID field will be initialized to NULL.
     */
    public static Group create() {
        return new Group(new PropertyStorage());
    }

    public static Group create(Integer id) {
        return new Group(id);
    }

    public static Group create(String name) {
        final Group group = new Group(new PropertyStorage());
        group.setName(name);
        return group;
    }

}
