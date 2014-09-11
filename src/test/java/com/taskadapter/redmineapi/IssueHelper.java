package com.taskadapter.redmineapi;

import com.taskadapter.redmineapi.bean.Issue;
import com.taskadapter.redmineapi.bean.IssueFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class IssueHelper {
    public static List<Issue> createIssues(IssueManager issueManager, String projectKey, int issuesNumber) throws RedmineException {
        List<Issue> issues = new ArrayList<Issue>(issuesNumber);
        for (int i = 0; i < issuesNumber; i++) {
            Issue issueToCreate = IssueFactory.createWithSubject("some issue " + i + " " + new Date());
            Issue issue = issueManager.createIssue(projectKey, issueToCreate);
            issues.add(issue);
        }
        return issues;
    }

    public static Issue generateRandomIssue() {
        Random r = new Random();
        Issue issue = IssueFactory.createWithSubject("some issue " + r.nextInt() + " " + new Date());
        return issue;
    }
}
