package com.taskadapter.redmineapi;

import com.taskadapter.redmineapi.bean.Issue;
import com.taskadapter.redmineapi.bean.IssueFactory;
import com.taskadapter.redmineapi.bean.ProjectFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class IssueHelper {
    public static List<Issue> createIssues(IssueManager issueManager, int projectId, int issuesNumber) throws RedmineException {
        List<Issue> issues = new ArrayList<Issue>(issuesNumber);
        for (int i = 0; i < issuesNumber; i++) {
            Issue issueToCreate = IssueFactory.create(projectId, "some issue " + i + " " + new Date());
            Issue issue = issueManager.createIssue(issueToCreate);
            issues.add(issue);
        }
        return issues;
    }

    public static Issue createIssue(IssueManager issueManager, int projectId) throws RedmineException {
        Issue issue = generateRandomIssue(projectId);
        issue.setProject(ProjectFactory.create(projectId));
        return issueManager.createIssue(issue);
    }

    public static Issue generateRandomIssue(int projectId) {
        Random r = new Random();
        return IssueFactory.create(projectId, "some issue " + r.nextInt() + " " + new Date());
    }

}
