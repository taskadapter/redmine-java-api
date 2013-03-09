package com.taskadapter.redmineapi.bean;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Redmine's Issue
 *
 * @author Alexey Skorokhodov
 */
public class Issue implements Identifiable {

    private Integer id;
    private String subject;
    private Integer parentId;
    private Float estimatedHours;
    private Float spentHours;
    private User assignee;
    private String priorityText;
    private Integer priorityId;
    private Integer doneRatio;
    private Project project;
    private User author;
    private Date startDate;
    private Date dueDate;
    private Tracker tracker;
    private String description;
    private Date createdOn;
    private Date updatedOn;
    private Integer statusId;
    private String statusName;
    private Version targetVersion;
    private IssueCategory category;

    /**
     * Some comment describing the issue update
     */
    private String notes;
    private List<CustomField> customFields = new ArrayList<CustomField>();
    private List<Journal> journals = new ArrayList<Journal>();
    private List<IssueRelation> relations = new ArrayList<IssueRelation>();
    private List<Attachment> attachments = new ArrayList<Attachment>();
    private List<Changeset> changesets = new ArrayList<Changeset>();
    private List<Watcher> watchers = new ArrayList<Watcher>();

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

    /**
     * @deprecated This method has no effect when creating issues on Redmine Server, so we might as well just delete it
     * in the future releases.
     */
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

    public Float getSpentHours() {
        return spentHours;
    }

    public void setSpentHours(Float spentHours) {
         this.spentHours = spentHours;
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

    @Override
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

    /**
     * Description is empty by default, not NULL.
     */
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

    public Integer getStatusId() {
        return statusId;
    }

    public void setStatusId(Integer statusId) {
        this.statusId = statusId;
    }

    public String getStatusName() {
        return statusName;
    }

    public void setStatusName(String statusName) {
        this.statusName = statusName;
    }

    /**
     * list of Custom Field objects, NEVER NULL.
     */
    public List<CustomField> getCustomFields() {
        return customFields;
    }

    /**
     * NOTE: The custom field(s) <b>must have correct database ID set</b> to be saved to Redmine. This is Redmine REST API's limitation.
     */
    public void setCustomFields(List<CustomField> customFields) {
        this.customFields = customFields;
    }

    public String getNotes() {
        return notes;
    }

    /**
     * @param notes Some comment describing the issue update
     */
    public void setNotes(String notes) {
        this.notes = notes;
    }

    public List<Journal> getJournals() {
        return journals;
    }

    public void setJournals(List<Journal> journals) {
        this.journals = journals;
    }

    public List<Changeset> getChangesets() {
        return changesets;
    }

    public void setChangesets(List<Changeset> changesets) {
        this.changesets = changesets;
    }

    public List<Watcher> getWatchers() {
        return watchers;
    }

    public void setWatchers(List<Watcher> watchers) {
        this.watchers = watchers;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((assignee == null) ? 0 : assignee.hashCode());
        result = prime * result + ((author == null) ? 0 : author.hashCode());
        result = prime * result
                + ((createdOn == null) ? 0 : createdOn.hashCode());
        result = prime * result
                + ((customFields == null) ? 0 : customFields.hashCode());
        result = prime * result
                + ((description == null) ? 0 : description.hashCode());
        result = prime * result
                + ((doneRatio == null) ? 0 : doneRatio.hashCode());
        result = prime * result + ((dueDate == null) ? 0 : dueDate.hashCode());
        result = prime * result
                + ((estimatedHours == null) ? 0 : estimatedHours.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result
                + ((journals == null) ? 0 : journals.hashCode());
        result = prime * result + ((notes == null) ? 0 : notes.hashCode());
        result = prime * result
                + ((parentId == null) ? 0 : parentId.hashCode());
        result = prime * result
                + ((priorityId == null) ? 0 : priorityId.hashCode());
        result = prime * result
                + ((priorityText == null) ? 0 : priorityText.hashCode());
        result = prime * result + ((project == null) ? 0 : project.hashCode());
        result = prime * result
                + ((relations == null) ? 0 : relations.hashCode());
        result = prime * result
                + ((spentHours == null) ? 0 : spentHours.hashCode());
        result = prime * result
                + ((startDate == null) ? 0 : startDate.hashCode());
        result = prime * result
                + ((statusId == null) ? 0 : statusId.hashCode());
        result = prime * result
                + ((statusName == null) ? 0 : statusName.hashCode());
        result = prime * result + ((subject == null) ? 0 : subject.hashCode());
        result = prime * result + ((tracker == null) ? 0 : tracker.hashCode());
        result = prime * result
                + ((updatedOn == null) ? 0 : updatedOn.hashCode());
        result = prime * result
            + ((changesets == null) ? 0 : changesets.hashCode());
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
        Issue other = (Issue) obj;
        if (assignee == null) {
            if (other.assignee != null) {
                return false;
            }
        } else if (!assignee.equals(other.assignee)) {
            return false;
        }
        if (author == null) {
            if (other.author != null) {
                return false;
            }
        } else if (!author.equals(other.author)) {
            return false;
        }
        if (createdOn == null) {
            if (other.createdOn != null) {
                return false;
            }
        } else if (!createdOn.equals(other.createdOn)) {
            return false;
        }
        if (customFields == null) {
            if (other.customFields != null) {
                return false;
            }
        } else if (!customFields.equals(other.customFields)) {
            return false;
        }
        if (description == null) {
            if (other.description != null) {
                return false;
            }
        } else if (!description.equals(other.description)) {
            return false;
        }
        if (doneRatio == null) {
            if (other.doneRatio != null) {
                return false;
            }
        } else if (!doneRatio.equals(other.doneRatio)) {
            return false;
        }
        if (dueDate == null) {
            if (other.dueDate != null) {
                return false;
            }
        } else if (!dueDate.equals(other.dueDate)) {
            return false;
        }
        if (estimatedHours == null) {
            if (other.estimatedHours != null) {
                return false;
            }
        } else if (!estimatedHours.equals(other.estimatedHours)) {
            return false;
        }
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        if (journals == null) {
            if (other.journals != null) {
                return false;
            }
        } else if (!journals.equals(other.journals)) {
            return false;
        }
        if (notes == null) {
            if (other.notes != null) {
                return false;
            }
        } else if (!notes.equals(other.notes)) {
            return false;
        }
        if (parentId == null) {
            if (other.parentId != null) {
                return false;
            }
        } else if (!parentId.equals(other.parentId)) {
            return false;
        }
        if (priorityId == null) {
            if (other.priorityId != null) {
                return false;
            }
        } else if (!priorityId.equals(other.priorityId)) {
            return false;
        }
        if (priorityText == null) {
            if (other.priorityText != null) {
                return false;
            }
        } else if (!priorityText.equals(other.priorityText)) {
            return false;
        }
        if (project == null) {
            if (other.project != null) {
                return false;
            }
        } else if (!project.equals(other.project)) {
            return false;
        }
        if (relations == null) {
            if (other.relations != null) {
                return false;
            }
        } else if (!relations.equals(other.relations)) {
            return false;
        }
        if (spentHours == null) {
            if (other.spentHours != null) {
                return false;
            }
        } else if (!spentHours.equals(other.spentHours)) {
            return false;
        }
        if (startDate == null) {
            if (other.startDate != null) {
                return false;
            }
        } else if (!startDate.equals(other.startDate)) {
            return false;
        }
        if (statusId == null) {
            if (other.statusId != null) {
                return false;
            }
        } else if (!statusId.equals(other.statusId)) {
            return false;
        }
        if (statusName == null) {
            if (other.statusName != null) {
                return false;
            }
        } else if (!statusName.equals(other.statusName)) {
            return false;
        }
        if (subject == null) {
            if (other.subject != null) {
                return false;
            }
        } else if (!subject.equals(other.subject)) {
            return false;
        }
        if (tracker == null) {
            if (other.tracker != null) {
                return false;
            }
        } else if (!tracker.equals(other.tracker)) {
            return false;
        }
        if (updatedOn == null) {
            if (other.updatedOn != null) {
                return false;
            }
        } else if (!updatedOn.equals(other.updatedOn)) {
            return false;
        }
        if (attachments == null) {
            if (other.attachments != null) {
                return false;
            }
        } else if (!attachments.equals(other.attachments)) {
            return false;
        }
        if (changesets == null) {
            if (other.changesets != null) {
                return false;
            }
        } else if (!changesets.equals(other.changesets)) {
            return false;
        }
        if (watchers == null) {
            if (other.watchers != null) {
                return false;
            }
        } else if (!watchers.equals(other.watchers)) {
            return false;
        }
        return true;
    }

    /**
     * @return the value or NULL if the field is not found
     */
    public String getCustomField(String fieldName) {
        for (CustomField f : customFields) {
            if (f.getName().equals(fieldName)) {
                return f.getValue();
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return "Issue [id=" + id + ", subject=" + subject + "]";
    }

    /**
     * Relations are only loaded if you include INCLUDE.relations when loading the Issue.
     *
     * @return list of relations or EMPTY list if no relations, never returns NULL
     *
     * @see com.taskadapter.redmineapi.RedmineManager#getIssueById(Integer id, INCLUDE... include)
     */
    public List<IssueRelation> getRelations() {
        return relations;
    }

    public Integer getPriorityId() {
        return priorityId;
    }

    public void setPriorityId(Integer priorityId) {
        this.priorityId = priorityId;
    }

    public Version getTargetVersion() {
        return targetVersion;
    }

    public List<Attachment> getAttachments() {
        return attachments;
    }

    public void setTargetVersion(Version version) {
        this.targetVersion = version;

    }

    public IssueCategory getCategory() {
        return category;
    }

    public void setCategory(IssueCategory category) {
        this.category = category;
    }
}
