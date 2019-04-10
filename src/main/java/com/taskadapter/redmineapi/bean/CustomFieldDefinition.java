package com.taskadapter.redmineapi.bean;

import java.util.ArrayList;
import java.util.List;

public class CustomFieldDefinition {

    private Integer id;
    private String name;
    private String customizedType;
    private String fieldFormat;
    private String regexp;
    private Integer minLength;
    private Integer maxLength;
    private boolean required;
    private boolean filter;
    private boolean searchable;
    private boolean multiple;
    private String defaultValue;
    private boolean visible;
    private final List<String> possibleValues = new ArrayList<>();
    private final List<Tracker> trackers = new ArrayList<>();
    private final List<Role> roles = new ArrayList<>();

    public Integer getId() {
        return id;
    }

    /**
     * @param id database ID.
     */
    public CustomFieldDefinition setId(Integer id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCustomizedType() {
        return customizedType;
    }

    public void setCustomizedType(String customizedType) {
        this.customizedType = customizedType;
    }

    public String getFieldFormat() {
        return fieldFormat;
    }

    public void setFieldFormat(String fieldFormat) {
        this.fieldFormat = fieldFormat;
    }

    public String getRegexp() {
        return regexp;
    }

    public void setRegexp(String regexp) {
        this.regexp = regexp;
    }

    public Integer getMinLength() {
        return minLength;
    }

    public void setMinLength(Integer minLength) {
        this.minLength = minLength;
    }

    public Integer getMaxLength() {
        return maxLength;
    }

    public void setMaxLength(Integer maxLength) {
        this.maxLength = maxLength;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public boolean isFilter() {
        return filter;
    }

    public void setFilter(Boolean filter) {
        this.filter = filter;
    }

    public boolean isSearchable() {
        return searchable;
    }

    public void setSearchable(Boolean searchable) {
        this.searchable = searchable;
    }

    public boolean isMultiple() {
        return multiple;
    }

    public void setMultiple(Boolean multiple) {
        this.multiple = multiple;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(Boolean visible) {
        this.visible = visible;
    }

    public List<String> getPossibleValues() {
        return possibleValues;
    }

    public List<Tracker> getTrackers() {
        return trackers;
    }

    public List<Role> getRoles() {
        return roles;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CustomFieldDefinition that = (CustomFieldDefinition) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "CustomFieldDefinition{" + "id=" + id + ", name=" + name +
                ", customizedType=" + customizedType + ", fieldFormat=" +
                fieldFormat + ", regexp=" + regexp + ", minLength=" + minLength +
                ", maxLength=" + maxLength + ", required=" + required +
                ", filter=" + filter + ", searchable=" + searchable +
                ", multiple=" + multiple + ", defaultValue=" + defaultValue +
                ", visible=" + visible + ", possibleValues=" + possibleValues +
                ", trackers=" + trackers + ", roles=" + roles + '}';
    }
}
