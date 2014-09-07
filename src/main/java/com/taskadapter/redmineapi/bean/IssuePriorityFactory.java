package com.taskadapter.redmineapi.bean;

public class IssuePriorityFactory {
    public static IssuePriority create(Integer id) {
        return new IssuePriority(id);
    }
}
