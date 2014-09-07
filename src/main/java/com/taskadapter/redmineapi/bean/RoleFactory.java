package com.taskadapter.redmineapi.bean;

public class RoleFactory {
    public static Role create(Integer id) {
        return new Role(id);
    }
}
