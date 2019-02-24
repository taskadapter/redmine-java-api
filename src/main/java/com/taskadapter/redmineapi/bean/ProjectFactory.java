package com.taskadapter.redmineapi.bean;

import com.taskadapter.redmineapi.internal.Transport;

public class ProjectFactory {
    public static Project create(Transport transport, String name, String identifier) {
        Project project = new Project(transport);
        project.setName(name);
        project.setIdentifier(identifier);
        return project;
    }
}
