package com.taskadapter.redmineapi.bean;

public class RoleFactory {
    /**
     * @param id database ID.
     */
    public static Role create(Integer id) {
        return new Role(id);
    }
}
