package com.taskadapter.redmineapi.bean;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Redmine's Issue
 */
public class Issue implements Identifiable {

    private final Integer id;

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

    /**
     * can't have two custom fields with the same ID in the collection, that's why it is declared
     * as a Set, not a List.
     */
    private Set<CustomField> customFields = new HashSet<CustomField>();
    private Set<Journal> journals = new HashSet<Journal>();
    private Set<IssueRelation> relations = new HashSet<IssueRelation>();
    private Set<Attachment> attachments = new HashSet<Attachment>();
    private Set<Changeset> changesets = new HashSet<Changeset>();
    private Set<Watcher> watchers = new HashSet<Watcher>();

    public Issue(Integer id) {
        this.id = id;
    }

    public Issue() {
        this.id = null;
    }

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
     * @return Custom Field objects. the iterator may be empty, but it is never NULL.
     */
    public Iterator<CustomField> getCustomFields() {
        return customFields.iterator();
    }

    public void clearCustomFields() {
        customFields.clear();
    }

    /**
     * NOTE: The custom field(s) <b>must have correct database ID set</b> to be saved to Redmine. This is Redmine REST API's limitation.
     */
    public void addCustomFields(Collection<CustomField> customFields) {
        this.customFields.addAll(customFields);
    }

    /**
     * If there is a custom field with the same ID already present in the Issue,
     * the new field replaces the old one.
     *
     * @param customField the field to add to the issue.
     */
    public void addCustomField(CustomField customField) {
        customFields.add(customField);
    }

    public int getNumberOfCustomFields() {
        return customFields.size();
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

    public Iterator<Journal> getJournals() {
        return journals.iterator();
    }

    public int getNumberOfJournals() {
        return journals.size();
    }

    public void addJournals(Collection<Journal> journals) {
        this.journals.addAll(journals);
    }

    public Iterator<Changeset> getChangesets() {
        return changesets.iterator();
    }

    public void addChangesets(Collection<Changeset> changesets) {
        this.changesets.addAll(changesets);
    }

    public int getNumberOfChangesets() {
        return changesets.size();
    }

    public Iterator<Watcher> getWatchers() {
        return watchers.iterator();
    }

    public void addWatchers(Collection<Watcher> watchers) {
        this.watchers.addAll(watchers);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Issue issue = (Issue) o;

        if (id != null ? !id.equals(issue.id) : issue.id != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
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

    public CustomField getCustomFieldById(int customFieldId) {
        if(customFields == null) return null;
        for (CustomField customField : customFields) {
            if (customFieldId == customField.getId()) {
                return customField;
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
     * @return relations or EMPTY iterator if no relations, never returns NULL
     *
     * @see com.taskadapter.redmineapi.RedmineManager#getIssueById(Integer id, INCLUDE... include)
     */
    public Iterator<IssueRelation> getRelations() {
        return relations.iterator();
    }

    public void addRelations(Collection<IssueRelation> collection) {
        relations.addAll(collection);
    }

    public int getNumberOfRelations() {
        return relations.size();
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

    /**
     * @return attachments. the iterator can be empty, but never null.
     */
    public Iterator<Attachment> getAttachments() {
        return attachments.iterator();
    }

    public void addAttachments(Collection<Attachment> collection) {
        attachments.addAll(collection);
    }

    public void addAttachment(Attachment attachment) {
        attachments.add(attachment);
    }

    public int getNumberOfAttachments() {
        return attachments.size();
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
