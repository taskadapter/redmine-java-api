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
	
	/**
	 * Trackers available for this project
	 */
	private List<Tracker> trackers;

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
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
}
