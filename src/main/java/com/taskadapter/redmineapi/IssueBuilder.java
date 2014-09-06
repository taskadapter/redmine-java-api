package com.taskadapter.redmineapi;

import com.taskadapter.redmineapi.bean.Issue;

public class IssueBuilder {
    public static Issue build(String subject) {
        Issue issue = new Issue();
        issue.setSubject(subject);
        return issue;
    }
}
