package com.taskadapter.redmineapi.bean;

import com.taskadapter.redmineapi.internal.Transport;

public class IssueCategoryFactory {

    /**
     * @param id database ID.
     */
    public static IssueCategory create(Integer id) {
        return new IssueCategory(id);
    }

}
