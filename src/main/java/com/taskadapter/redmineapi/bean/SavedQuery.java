package com.taskadapter.redmineapi.bean;

public class SavedQuery {
    private Integer id;

    private String name;
    private boolean publicQuery;
    private Integer projectId;

    public SavedQuery setId(Integer id) {
        this.id = id;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SavedQuery that = (SavedQuery) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public SavedQuery setName(String name) {
        this.name = name;
        return this;
    }

    public boolean isPublicQuery() {
        return publicQuery;
    }

    public SavedQuery setPublicQuery(boolean isPublic) {
        this.publicQuery = isPublic;
        return this;
    }

    public Integer getProjectId() {
        return projectId;
    }

    public SavedQuery setProjectId(Integer value) {
        this.projectId = value;
        return this;
    }

    @Override
    public String toString() {
        return "SavedQuery [id=" + id + ", name=" + name + ", publicQuery="
                + publicQuery + ", projectId=" + projectId + "]";
    }

}
