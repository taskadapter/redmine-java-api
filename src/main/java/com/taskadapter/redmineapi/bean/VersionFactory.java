package com.taskadapter.redmineapi.bean;

public class VersionFactory {
    public static Version create() {
        Version version = new Version(null);
        return version;
    }

    public static Version create(Integer id) {
        Version version = new Version(id);
        return version;
    }

    public static Version create(Project project, String versionName) {
        Version version = new Version(null);
        version.setProject(project);
        version.setName(versionName);
        return version;
    }
}
