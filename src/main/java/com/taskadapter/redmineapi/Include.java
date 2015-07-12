package com.taskadapter.redmineapi;

/**
 * Use these flags to indicate what needs to be loaded from the server. Example:
 * <pre>
 *     Issue issue = issueManager.getIssueById(id, Include.journals, Include.relations, Include.attachments);
 * </pre>
 */
public enum Include {
        // these values MUST BE exactly as they are written here,
        // can't use capital letters or rename.
        // they are provided in "?include=..." HTTP request
        journals, relations, attachments, changesets, watchers, children
}
