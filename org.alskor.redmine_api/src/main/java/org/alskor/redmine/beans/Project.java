package org.alskor.redmine.beans;

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
	 * @param project name
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
	
	
}
