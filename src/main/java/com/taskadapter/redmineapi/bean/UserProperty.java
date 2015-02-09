package com.taskadapter.redmineapi.bean;

public class UserProperty extends Property<User> {
    UserProperty(String name) {
        super(User.class, name);
    }

    @Override
    User cloneDeep(Object t) {
        return ((User) t).cloneDeep();
    }
}
