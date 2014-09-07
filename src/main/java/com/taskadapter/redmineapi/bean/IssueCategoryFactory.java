package com.taskadapter.redmineapi.bean;

public class IssueCategoryFactory {

    /**
     * @param id database ID.
     */
    public static IssueCategory create(Integer id) {
        return new IssueCategory(id);
    }

    public static IssueCategory create(Project project, String name) {
        IssueCategory issueCategory = new IssueCategory(null);
        issueCategory.setProject(project);
        issueCategory.setName(name);
        return issueCategory;
    }
}
