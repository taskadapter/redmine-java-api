package org.alskor.redmine.beans;

import java.util.Date;

/**
 * Redmine's Issue
 * 
 * @author Alexey Skorokhodov
 */
public class Issue {

	private Integer id;
	private String subject;
	private Integer parentId;
	private Float estimatedHours;
	private User assignee;
	private String priorityText;
	private Integer doneRatio;
	private Project project;
	private User author;
	private Date startDate;
	private Date dueDate;
	private Tracker tracker;
	
	public Project getProject() {
		return project;
	}

	public void setProject(Project project) {
		this.project = project;
	}

	public Integer getDoneRatio() {
		return doneRatio;
	}

	public void setDoneRatio(Integer doneRatio) {
		this.doneRatio = doneRatio;
	}

	public String getPriorityText() {
		return priorityText;
	}

	public void setPriorityText(String priority) {
		this.priorityText = priority;
	}

	public User getAssignee() {
		return assignee;
	}

	public void setAssignee(User assignee) {
		this.assignee = assignee;
	}

	public Float getEstimatedHours() {
		return estimatedHours;
	}

	public void setEstimatedHours(Float estimatedTime) {
		this.estimatedHours = estimatedTime;
	}

	/**
	 * Parent Issue ID, or NULL for issues without a parent.
	 * 
	 * @return NULL, if there's no parent
	 */
	public Integer getParentId() {
		return parentId;
	}

	public void setParentId(Integer parentId) {
		this.parentId = parentId;
	}

	/**
	 * @return id. can be NULL for Issues not added to Redmine yet
	 */
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	@Override
	public String toString() {
//		return id + "  (parent=" + parentId + ")";
		
		return "issue: id=" +id + " assignee:" + ((assignee == null) ? "" : assignee.toString()) + " subject:" + subject;
	}

	public User getAuthor() {
		return author;
	}

	public void setAuthor(User author) {
		this.author = author;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getDueDate() {
		return dueDate;
	}

	public void setDueDate(Date dueDate) {
		this.dueDate = dueDate;
	}

	public Tracker getTracker() {
		return tracker;
	}

	public void setTracker(Tracker tracker) {
		this.tracker = tracker;
	}
	
}
