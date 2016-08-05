package com.taskadapter.redmineapi.bean;

public class IssueCategoryFactory {

    /**
     * @param id database ID.
     */
    public static IssueCategory create(Integer id) {
        return new IssueCategory(id);
    }

    public static IssueCategory create(Integer projectId, String name) {
        IssueCategory issueCategory = new IssueCategory(null);
        issueCategory.setProjectId(projectId);
        issueCategory.setName(name);
        return issueCategory;
    }
}
