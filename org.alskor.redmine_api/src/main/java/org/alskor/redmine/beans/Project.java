package org.alskor.redmine.beans;

import java.util.Date;
import java.util.List;

/**
 * Redmine's Project.
 */
public class Project {
	
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
	
	private Date createdOn;
	
	private Date updatedOn;
	
	/**
	 * Trackers available for this project
	 */
	private List<Tracker> trackers;

	/**
	 * @return project's string "key" (not a numeric database id!). Example: "project_ABC"
	 */
	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

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
	public String toString(){
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((createdOn == null) ? 0 : createdOn.hashCode());
		result = prime * result
				+ ((description == null) ? 0 : description.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result
				+ ((identifier == null) ? 0 : identifier.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
