package com.taskadapter.redmineapi.bean;

public class CustomFieldFactory {

    public static CustomField create(Integer id) {
        return new CustomField(id);
    }

    public static CustomField create(Integer id, String name, String value) {
        CustomField field = new CustomField(id);
        field.setName(name);
        field.setValue(value);
        return field;
    }
}
