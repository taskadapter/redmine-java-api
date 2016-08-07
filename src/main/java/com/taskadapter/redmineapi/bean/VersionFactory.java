package com.taskadapter.redmineapi.bean;

public class VersionFactory {
    public static Version create() {
        return new Version(null);
    }

    public static Version create(Integer id) {
        return new Version(id);
    }

    public static Version create(Integer projectId, String versionName) {
        Version version = new Version(null);
        version.setProjectId(projectId);
        version.setName(versionName);
        return version;
    }
}
