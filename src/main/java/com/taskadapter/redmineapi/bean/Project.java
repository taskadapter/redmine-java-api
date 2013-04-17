package com.taskadapter.redmineapi.bean;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Redmine's Project.
 */
public class Project implements Identifiable, Serializable {
	private static final long serialVersionUID = 4529305420978716446L;

	/**
	 * database ID
	 */
    private Integer id;

    /**
     * String "identifier" (human-readable name without spaces and other extra stuff)
     */
    private String identifier;

    /**
     * Can contain any symbols
     */
    private String name;

    private String description;

    private String homepage;

    private Date createdOn;

    private Date updatedOn;

    /**
     * Trackers available for this project
     */
    private List<Tracker> trackers;

    /**
     * This is the *database ID*, not a String-based key.
     */
    private Integer parentId;

    public String getHomepage() {
        return homepage;
    }

    public void setHomepage(String homepage) {
        this.homepage = homepage;
    }

    /**
     * @return project's string "key" (not a numeric database id!). Example: "project_ABC"
     */
    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    @Override
    /**
     * @return numeric database ID
     */
    public Integer getId() {
        return id;
    }

    /**
     * @param id numeric database ID
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * @return project name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the project name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return list of Trackers allowed in this project (e.g.: Bug, Feature, Support, Task, ...)
     */
    public List<Tracker> getTrackers() {
        return trackers;
    }

    public void setTrackers(List<Tracker> trackers) {
        this.trackers = trackers;
    }

    public Tracker getTrackerByName(String trackerName) {
        if (this.trackers == null) return null;
        for (Tracker t : this.trackers) {
            if (t.getName().equals(trackerName)) return t;
        }
        return null;
    }

    @Override
    public String toString() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Date createdOn) {
        this.createdOn = createdOn;
    }

    public Date getUpdatedOn() {
        return updatedOn;
    }

    public void setUpdatedOn(Date updatedOn) {
        this.updatedOn = updatedOn;
    }

    /**
     * Redmine's REST API "get project" operation does NOT return the 
     * parent project ID in redmine 1.1.2 (and maybe earlier). Which means 
     * calling getParentId() of the project loaded from Redmine server will
     * return <b>NULL</b> with that redmine. This bug was fixed in redmine 1.2.1.
     * See bug http://www.redmine.org/issues/8229
     * 
     *
     * @return the parent project Id if it was set programmatically or NULL (!!!) if the project was loaded from the server.
     */
    public Integer getParentId() {
        return parentId;
    }

    public void setParentId(Integer parentId) {
        this.parentId = parentId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((createdOn == null) ? 0 : createdOn.hashCode());
        result = prime * result
                + ((description == null) ? 0 : description.hashCode());
        result = prime * result
                + ((homepage == null) ? 0 : homepage.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result
                + ((identifier == null) ? 0 : identifier.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result
                + ((parentId == null) ? 0 : parentId.hashCode());
        result = prime * result
                + ((trackers == null) ? 0 : trackers.hashCode());
        result = prime * result
                + ((updatedOn == null) ? 0 : updatedOn.hashCode());
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
        Project other = (Project) obj;
        if (createdOn == null) {
            if (other.createdOn != null) {
                return false;
            }
        } else if (!createdOn.equals(other.createdOn)) {
            return false;
        }
        if (description == null) {
            if (other.description != null) {
                return false;
            }
        } else if (!description.equals(other.description)) {
            return false;
        }
        if (homepage == null) {
            if (other.homepage != null) {
                return false;
            }
        } else if (!homepage.equals(other.homepage)) {
            return false;
        }
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        if (identifier == null) {
            if (other.identifier != null) {
                return false;
            }
        } else if (!identifier.equals(other.identifier)) {
            return false;
        }
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (parentId == null) {
            if (other.parentId != null) {
                return false;
            }
        } else if (!parentId.equals(other.parentId)) {
            return false;
        }
        if (trackers == null) {
            if (other.trackers != null) {
                return false;
            }
        } else if (!trackers.equals(other.trackers)) {
            return false;
        }
        if (updatedOn == null) {
            if (other.updatedOn != null) {
                return false;
            }
        } else if (!updatedOn.equals(other.updatedOn)) {
            return false;
        }
        return true;
    }


}
