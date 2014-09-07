package com.taskadapter.redmineapi.bean;

public class UserFactory {
    public static User create(Integer id) {
        return new User(id);
    }

    public static User create() {
        return new User(null);
    }
}
