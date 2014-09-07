package com.taskadapter.redmineapi.bean;

public class CustomFieldFactory {
    public static CustomField build() {
        CustomField field = new CustomField(null);
        return field;
    }

    public static CustomField build(Integer id) {
        CustomField field = new CustomField(id);
        return field;
    }

    public static CustomField build(Integer id, String name, String value) {
        CustomField field = new CustomField(id);
        field.setName(name);
        field.setValue(value);
        return field;
    }
}
