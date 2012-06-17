package com.taskadapter.redmineapi;

import java.util.List;

import com.taskadapter.redmineapi.bean.Issue;
import com.taskadapter.redmineapi.bean.TimeEntry;

public class RedmineTestUtils {

    public static Issue findIssueInList(List<Issue> list, Integer id) {
        Issue result = null;
        for (Issue issue : list) {
            if (issue.getId().equals(id)) {
                result = issue;
            }
        }
        return result;
    }

    public static Issue findIssueInList(List<Issue> list, String subject) {
        Issue result = null;
        for (Issue issue : list) {
            if (issue.getSubject().equals(subject)) {
                result = issue;
            }
        }
        return result;
    }

    public static TimeEntry findTimeEntry(List<TimeEntry> list, Integer id) {
        TimeEntry result = null;
        for (TimeEntry obj : list) {
            if (obj.getId().equals(id)) {
                result = obj;
            }
        }
        return result;
    }

}
