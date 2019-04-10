package com.taskadapter.redmineapi.bean;

public class Property<T> {

    private final Class<T> type;
    private final String name;

    Property(Class<T> type, String name) {
        this.type = type;
        this.name = name;
    }

    public final String getName() {
        return name;
    }

    public Class<T> getType() {
        return type;
    }

    @Override
    public String toString() {
        return "Property{" +
                "type=" + type +
                ", name='" + name + '\'' +
                '}';
    }
}