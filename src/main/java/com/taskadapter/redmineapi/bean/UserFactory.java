package com.taskadapter.redmineapi.bean;

// this is kinda useless now
public class UserFactory {
    public static User create(Integer id) {
        return new User(id);
    }

    public static User create() {
        return new User();
    }
}
