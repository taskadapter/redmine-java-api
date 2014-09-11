package com.taskadapter.redmineapi.bean;

public class SavedQueryFactory {
    public static SavedQuery create(Integer id) {
        return new SavedQuery(id);
    }
}
