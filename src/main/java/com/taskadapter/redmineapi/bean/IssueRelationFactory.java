package com.taskadapter.redmineapi.bean;

public class IssueRelationFactory {
    public static IssueRelation create() {
        return new IssueRelation(null);
    }

    public static IssueRelation create(Integer id) {
        return new IssueRelation(id);
    }
}
