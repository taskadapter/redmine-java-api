package com.taskadapter.redmineapi;

import com.taskadapter.redmineapi.bean.CustomFieldDefinition;

import java.util.List;

final class CustomFieldResolver {
    static CustomFieldDefinition getCustomFieldByName(List<CustomFieldDefinition> customFieldDefinitions, String fieldName) {
        for (CustomFieldDefinition customFieldDefinition : customFieldDefinitions) {
            if (customFieldDefinition.getName().equals(fieldName)) {
                return customFieldDefinition;
            }
        }
        throw new RuntimeException("Custom Field definition '" + fieldName + "' is not found on server.");
    }

}
