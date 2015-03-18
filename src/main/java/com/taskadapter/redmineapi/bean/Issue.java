package com.taskadapter.redmineapi.bean;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Redmine's Issue
 */
public class Issue implements Identifiable {

    /**
     * database ID.
     */
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
    private final Set<CustomField> customFields = new HashSet<CustomField>();
    private final Set<Journal> journals = new HashSet<Journal>();
    private final Set<IssueRelation> relations = new HashSet<IssueRelation>();
    private final Set<Attachment> attachments = new HashSet<Attachment>();
    private final Set<Changeset> changesets = new HashSet<Changeset>();
    private final Set<Watcher> watchers = new HashSet<Watcher>();

    /**
     * @param id database ID.
     */
    Issue(Integer id) {
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
     * @return Custom Field objects. the collection may be empty, but it is never NULL.
     */
    public Collection<CustomField> getCustomFields() {
        return Collections.unmodifiableCollection(customFields);
    }

    public void clearCustomFields() {
        customFields.clear();
    }

    /**
     * NOTE: The custom field(s) <strong>must have correct database ID set</strong> to be saved to Redmine. This is Redmine REST API's limitation.
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

    public String getNotes() {
        return notes;
    }

    /**
     * @param notes Some comment describing the issue update
     */
    public void setNotes(String notes) {
        this.notes = notes;
    }

    /**
     * Don't forget to use Include.journals flag when loading issue from Redmine server:
     * <pre>
     *     Issue issue = issueManager.getIssueById(3205, Include.journals);
     * </pre>
     * @return Collection of Journal entries or empty collection if no objects found.
     * @see com.taskadapter.redmineapi.Include#journals
     */
    public Collection<Journal> getJournals() {
        return Collections.unmodifiableCollection(journals);
    }

    public void addJournals(Collection<Journal> journals) {
        this.journals.addAll(journals);
    }

    /**
     * Don't forget to use Include.changesets flag when loading issue from Redmine server:
     * <pre>
     *     Issue issue = issueManager.getIssueById(3205, Include.changesets);
     * </pre>
     * @return Collection of entries or empty collection if no objects found.
     * @see com.taskadapter.redmineapi.Include#changesets
     */
    public Collection<Changeset> getChangesets() {
        return Collections.unmodifiableCollection(changesets);
    }

    public void addChangesets(Collection<Changeset> changesets) {
        this.changesets.addAll(changesets);
    }

    /**
     * Don't forget to use Include.watchers flag when loading issue from Redmine server:
     * <pre>
     *     Issue issue = issueManager.getIssueById(3205, Include.watchers);
     * </pre>
     * @return Collection of entries or empty collection if no objects found.
     * @see com.taskadapter.redmineapi.Include#watchers
     */
    public Collection<Watcher> getWatchers() {
        return Collections.unmodifiableCollection(watchers);
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
     * Deprecated. Please use the new getCustomFieldByName() method instead because the return value of this method
     * is not consistent with getCustomFieldById().
     *
     * @return the value or NULL if the field is not found
     *
     * @see #getCustomFieldByName(String customFieldName)
     */
    @Deprecated
    public String getCustomField(String fieldName) {
        for (CustomField f : customFields) {
            if (f.getName().equals(fieldName)) {
                return f.getValue();
            }
        }
        return null;
    }

    /**
     * @return the custom field with given Id or NULL if the field is not found
     */
    public CustomField getCustomFieldById(int customFieldId) {
        if(customFields == null) return null;
        for (CustomField customField : customFields) {
            if (customFieldId == customField.getId()) {
                return customField;
            }
        }
        return null;
    }

    /**
     * @return the custom field with given name or NULL if the field is not found
     */
    public CustomField getCustomFieldByName(String customFieldName) {
        if(customFields == null) return null;
        for (CustomField customField : customFields) {
            if (customFieldName.equals(customField.getName())) {
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
     * <pre>
     *     Issue issue = issueManager.getIssueById(3205, Include.relations);
     * </pre>
     * @return relations or EMPTY collection if no relations, never returns NULL
     * @see com.taskadapter.redmineapi.Include#relations
     */
    public Collection<IssueRelation> getRelations() {
        return Collections.unmodifiableCollection(relations);
    }

    public void addRelations(Collection<IssueRelation> collection) {
        relations.addAll(collection);
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
     * Don't forget to use <i>Include.attachments</i> flag when loading issue from Redmine server:
     * <pre>
     *     Issue issue = issueManager.getIssueById(3205, Include.attachments);
     * </pre>
     * @return Collection of entries or empty collection if no objects found.
     * @see com.taskadapter.redmineapi.Include#attachments
     */
    public Collection<Attachment> getAttachments() {
        return Collections.unmodifiableCollection(attachments);
    }

    public void addAttachments(Collection<Attachment> collection) {
        attachments.addAll(collection);
    }

    public void addAttachment(Attachment attachment) {
        attachments.add(attachment);
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
