package com.taskadapter.redmineapi.bean;

public class IssueStatusFactory {
    public static IssueStatus create(int id, String name) {
        IssueStatus issueStatus = new IssueStatus(id);
        issueStatus.setName(name);
        return issueStatus;
    }
}
