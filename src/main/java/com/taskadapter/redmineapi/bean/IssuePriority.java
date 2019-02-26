package com.taskadapter.redmineapi.bean;

public class IssuePriority {
    /**
     * database ID.
     */
    private Integer id;
    private String name;
    private boolean isDefault;

    /**
     * @param id database ID.
     */
    public IssuePriority setId(Integer id) {
        this.id = id;
        return this;
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public IssuePriority setName(String name) {
        this.name = name;
        return this;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public IssuePriority setDefault(boolean isDefault) {
        this.isDefault = isDefault;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        IssuePriority that = (IssuePriority) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Priority [id=" + id + ", name=" + name + ", isDefault="
                + isDefault + "]";
    }

}
