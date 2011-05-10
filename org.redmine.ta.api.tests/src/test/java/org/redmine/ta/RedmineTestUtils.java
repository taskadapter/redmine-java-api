package org.redmine.ta;

import java.util.List;

import org.redmine.ta.beans.Issue;
import org.redmine.ta.beans.TimeEntry;

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
