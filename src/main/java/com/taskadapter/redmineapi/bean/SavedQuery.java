package com.taskadapter.redmineapi.bean;

public class SavedQuery {
    private Integer id;
    private String name;
    private boolean publicQuery;
    private Integer projectId;

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + (publicQuery ? 1231 : 1237);
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result
                + ((projectId == null) ? 0 : projectId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        SavedQuery other = (SavedQuery) obj;
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        if (publicQuery != other.publicQuery) {
            return false;
        }
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (projectId == null) {
            if (other.projectId != null) {
                return false;
            }
        } else if (!projectId.equals(other.projectId)) {
            return false;
        }
        return true;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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
