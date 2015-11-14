
package com.taskadapter.redmineapi.bean;

/**
 * Assignee is a named representation of an assignee.
 * 
 * <p>An assignee is can be a user or a group. For parsing a GenericAssignee
 * exists, that only holds a name and id (redmine only returns these infos
 * when querying an issue.</p>
 * 
 * <p>All subclasses must implements equals, so that assignees are only compared
 * based on ID.</p>
 */
public interface Assignee extends Identifiable {
    String getName();
}
