package com.taskadapter.redmineapi.bean;

public class TimeEntryActivity {

    /**
     * @param id database Id
     */
    private final Integer id;

    private String name;
    private boolean isDefault;

    /**
     * @param id database Id
     */
    TimeEntryActivity(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TimeEntryActivity that = (TimeEntryActivity) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "TimeEntryActivity [id=" + id + ", name=" + name + ", isDefault="
                + isDefault + "]";
    }

}
