package com.taskadapter.redmineapi.bean;

public class CustomFieldDefinitionFactory {

    public static CustomFieldDefinition create(Integer id) {
        return new CustomFieldDefinition(id);
    }
}
