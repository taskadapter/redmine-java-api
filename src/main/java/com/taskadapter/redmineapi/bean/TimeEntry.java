package com.taskadapter.redmineapi.bean;

import java.util.Date;

public class TimeEntry implements Identifiable {
    /**
     * database Id
     */
    private Integer id;

    /**
     * database Id of the Issue
     */
    private Integer issueId;

    /**
     * database Id of the project
     */
    private Integer projectId;
    private String projectName;
    private String userName;
    private Integer userId;
    private String activityName;
    private Integer activityId;
    private Float hours;
    private String comment;
    private Date spentOn;
    private Date createdOn;
    private Date updatedOn;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getIssueId() {
        return issueId;
    }

    public void setIssueId(Integer issueId) {
        this.issueId = issueId;
    }

    public Integer getProjectId() {
        return projectId;
    }

    public void setProjectId(Integer projectId) {
        this.projectId = projectId;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getActivityName() {
        return activityName;
    }

    public void setActivityName(String activityName) {
        this.activityName = activityName;
    }

    public Integer getActivityId() {
        return activityId;
    }

    public void setActivityId(Integer activityId) {
        this.activityId = activityId;
    }

    public Float getHours() {
        return hours;
    }

    public void setHours(Float hours) {
        this.hours = hours;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Date getSpentOn() {
        return spentOn;
    }

    public void setSpentOn(Date spentOn) {
        this.spentOn = spentOn;
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

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Override
    public String toString() {
        return "User \"" + userName + "\" spent " + hours
                + " hours on task " + issueId + " (project \"" + projectName
                + "\") doing " + activityName;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((activityId == null) ? 0 : activityId.hashCode());
        result = prime * result
                + ((activityName == null) ? 0 : activityName.hashCode());
        result = prime * result + ((comment == null) ? 0 : comment.hashCode());
        result = prime * result
                + ((createdOn == null) ? 0 : createdOn.hashCode());
        result = prime * result + ((hours == null) ? 0 : hours.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((issueId == null) ? 0 : issueId.hashCode());
        result = prime * result
                + ((projectId == null) ? 0 : projectId.hashCode());
        result = prime * result
                + ((projectName == null) ? 0 : projectName.hashCode());
        result = prime * result + ((spentOn == null) ? 0 : spentOn.hashCode());
        result = prime * result
                + ((updatedOn == null) ? 0 : updatedOn.hashCode());
        result = prime * result + ((userId == null) ? 0 : userId.hashCode());
        result = prime * result
                + ((userName == null) ? 0 : userName.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        TimeEntry other = (TimeEntry) obj;
        if (activityId == null) {
            if (other.activityId != null)
                return false;
        } else if (!activityId.equals(other.activityId))
            return false;
        if (activityName == null) {
            if (other.activityName != null)
                return false;
        } else if (!activityName.equals(other.activityName))
            return false;
        if (comment == null) {
            if (other.comment != null)
                return false;
        } else if (!comment.equals(other.comment))
            return false;
        if (createdOn == null) {
            if (other.createdOn != null)
                return false;
        } else if (!createdOn.equals(other.createdOn))
            return false;
        if (hours == null) {
            if (other.hours != null)
                return false;
        } else if (!hours.equals(other.hours))
            return false;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (issueId == null) {
            if (other.issueId != null)
                return false;
        } else if (!issueId.equals(other.issueId))
            return false;
        if (projectId == null) {
            if (other.projectId != null)
                return false;
        } else if (!projectId.equals(other.projectId))
            return false;
        if (projectName == null) {
            if (other.projectName != null)
                return false;
        } else if (!projectName.equals(other.projectName))
            return false;
        if (spentOn == null) {
            if (other.spentOn != null)
                return false;
        } else if (!spentOn.equals(other.spentOn))
            return false;
        if (updatedOn == null) {
            if (other.updatedOn != null)
                return false;
        } else if (!updatedOn.equals(other.updatedOn))
            return false;
        if (userId == null) {
            if (other.userId != null)
                return false;
        } else if (!userId.equals(other.userId))
            return false;
        if (userName == null) {
            if (other.userName != null)
                return false;
        } else if (!userName.equals(other.userName))
            return false;
        return true;
    }

    public boolean isValid() {
        return (hours != null) && (projectId != null || issueId != null);
    }
}
