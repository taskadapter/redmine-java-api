/*
   Copyright 2010-2011 Alexey Skorokhodov.

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
package com.taskadapter.redmineapi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.taskadapter.redmineapi.bean.Issue;

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
