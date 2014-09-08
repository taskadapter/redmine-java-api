package com.taskadapter.redmineapi.bean;

import com.taskadapter.redmineapi.bean.Issue;

public class IssueFactory {
    public static Issue create(String subject) {
        Issue issue = new Issue();
        issue.setSubject(subject);
        return issue;
    }
}
