package com.taskadapter.redmineapi.bean;

public class GroupFactory {
    /**
     * For new objects not added to Redmine yet. the ID field will be initialized to NULL.
     */
    public static Group create() {
        return new Group(null);
    }

    public static Group create(Integer id) {
        return new Group(id);
    }
}
