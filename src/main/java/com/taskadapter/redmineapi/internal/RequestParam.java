package com.taskadapter.redmineapi.internal;

import java.util.Objects;

public class RequestParam {

    private final String name;
    private final String value;

    public RequestParam(final String name, final String value) {
        this.name = Objects.requireNonNull(name, "Name may not be null");
        this.value = Objects.requireNonNull(value, "Value may not be null");
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RequestParam that = (RequestParam) o;
        return name.equals(that.name) &&
                value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, value);
    }

    @Override
    public String toString() {
        return "RequestParam{" +
                "name='" + name + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}
