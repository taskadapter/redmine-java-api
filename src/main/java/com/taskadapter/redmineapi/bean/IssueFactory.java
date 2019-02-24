package com.taskadapter.redmineapi.bean;

public class IssueFactory {

    public static Issue create(Integer databaseId) {
        return new Issue(databaseId);
    }
}
