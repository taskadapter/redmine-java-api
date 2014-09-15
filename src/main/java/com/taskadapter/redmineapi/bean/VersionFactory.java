package com.taskadapter.redmineapi.bean;

public class VersionFactory {
    public static Version create() {
        return new Version(null);
    }

    public static Version create(Integer id) {
        return new Version(id);
    }

    public static Version create(Project project, String versionName) {
        Version version = new Version(null);
        version.setProject(project);
        version.setName(versionName);
        return version;
    }
}
