package org.alskor.redmine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/** 
 * Utility class to help convert flat list of Redmine issues to a tree-like structure.
 *  
 * @author Alexey Skorokhodov
 */
public class RedmineIssuesMap {

	/**
	 * map: issueID -> Issue
	 */
	private HashMap<Integer, Issue> issuesMap = new HashMap<Integer, Issue>();

	public RedmineIssuesMap(List<Issue> list) {
		Iterator<Issue> it = list.iterator();
		while (it.hasNext()) {
			Issue issue = it.next();
			issuesMap.put(issue.getId(), issue);
		}
	}

	protected HashMap<Integer, Issue> getIssuesMap() {
		return issuesMap;
	}

	public List<Issue> getRootLevelTasks() {
		return getChildren(null);
	}

	// XXX rewrite this to use issue.getChildren method().
	// TODO add junit test for this new method
	public List<Issue> getChildren(Issue redmineParentIssue) {
		List<Issue> rootIssues = new ArrayList<Issue>();
		Iterator<Issue> it = issuesMap.values().iterator();
		Integer parentID = null; // look for root level by default
		boolean lookingForRootTasks = true;
		if (redmineParentIssue != null) {
			parentID = redmineParentIssue.getId();
			lookingForRootTasks = false;
		}
		while (it.hasNext()) {
			Issue issue = it.next();
			if (lookingForRootTasks
					&& (issue.getParentId() == null)
					|| (!lookingForRootTasks && (issue.getParentId() != null) && (issue
							.getParentId().equals(parentID)))) {
				rootIssues.add(issue);
			}
		}
		return rootIssues;
	}

}
