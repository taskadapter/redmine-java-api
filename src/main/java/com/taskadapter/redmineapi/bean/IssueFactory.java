package com.taskadapter.redmineapi.bean;

public class IssueFactory {
    public static Issue createWithSubject(String subject) {
        Issue issue = new Issue();
        issue.setSubject(subject);
        return issue;
    }

    public static Issue create(Integer databaseId) {
        return new Issue(databaseId);
    }
}
