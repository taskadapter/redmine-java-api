
package com.taskadapter.redmineapi.bean;

public class GenericAssigneeFactory {
    public static Assignee createGenericAssignee(int id, String name) {
        return new GenericAssignee(id, name);
    }
}
