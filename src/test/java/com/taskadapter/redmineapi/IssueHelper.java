package com.taskadapter.redmineapi;

import com.taskadapter.redmineapi.bean.Issue;
import com.taskadapter.redmineapi.internal.Transport;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class IssueHelper {
    public static List<Issue> createIssues(Transport transport, int projectId, int issuesNumber) throws RedmineException {
        List<Issue> issues = new ArrayList<>(issuesNumber);
        for (int i = 0; i < issuesNumber; i++) {
            Issue issue = new Issue(transport, projectId).setSubject("some issue " + i + " " + new Date())
                    .create();
            issues.add(issue);
        }
        return issues;
    }

    public static Issue createIssue(Transport transport, int projectId) throws RedmineException {
        return generateRandomIssue(transport, projectId).create();
    }

    public static Issue generateRandomIssue(Transport transport, int projectId) {
        Random r = new Random();
        return new Issue(transport, projectId).setSubject("some issue " + r.nextInt() + " " + new Date());
    }

}
