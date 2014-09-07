package com.taskadapter.redmineapi.bean;

public class ProjectFactory {
    public static Project create(Integer id) {
        return new Project(id);
    }

    public static Project create() {
        return new Project(null);
    }

    public static Project create(String name, String identifier) {
        Project project = new Project(null);
        project.setName(name);
        project.setIdentifier(identifier);
        return project;
    }
}
