package com.taskadapter.redmineapi.bean;

public class SavedQuery {
    private final Integer id;

    private String name;
    private boolean publicQuery;
    private Integer projectId;

    /**
     * @param id database Id
     */
    SavedQuery(Integer id) {
        this.id = id;
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

    public void setName(String name) {
        this.name = name;
    }

    public boolean isPublicQuery() {
        return publicQuery;
    }

    public void setPublicQuery(boolean isPublic) {
        this.publicQuery = isPublic;
    }

    public Integer getProjectId() {
        return projectId;
    }

    public void setProjectId(Integer value) {
        this.projectId = value;
    }

    @Override
    public String toString() {
        return "SavedQuery [id=" + id + ", name=" + name + ", publicQuery="
                + publicQuery + ", projectId=" + projectId + "]";
    }

}
