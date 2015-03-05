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
    public static final String PROP_SUBJECT = "subject";
    public static final String PROP_PARENT_ID = "parentId";
    public static final String PROP_ESTIMATED_HOURS = "estimatedHours";
    public static final String PROP_SPENT_HOURS = "spentHours";
    public static final String PROP_ASSIGNEE = "assignee";
    public static final String PROP_PRIORITY_ID = "priorityId";
    public static final String PROP_DONE_RATIO = "doneRatio";
    public static final String PROP_PROJECT = "project";
    public static final String PROP_AUTHOR = "author";
    public static final String PROP_START_DATE = "startDate";
    public static final String PROP_DUE_DATE = "dueDate";
    public static final String PROP_TRACKER = "tracker";
    public static final String PROP_DESCRIPTION = "description";
    public static final String PROP_CREATED_ON = "createdOn";
    public static final String PROP_UPDATED_ON = "updatedOn";
    public static final String PROP_STATUS_ID = "statusId";
    public static final String PROP_TARGET_VERSION = "targetVersion";
    public static final String PROP_CATEGORY = "category";
    
    /**
     * @param id database ID.
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

    private boolean updateTracking = false;
    // Package visible to be accessible by unittests
    final Set<String> updated = new HashSet<String>();
    
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
        updated.add(PROP_PROJECT);
        this.project = project;
    }

    public Integer getDoneRatio() {
        return doneRatio;
    }

    public void setDoneRatio(Integer doneRatio) {
        updated.add(PROP_DONE_RATIO);
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
        updated.add(PROP_ASSIGNEE);
        this.assignee = assignee;
    }

    public Float getEstimatedHours() {
        return estimatedHours;
    }

    public void setEstimatedHours(Float estimatedTime) {
        updated.add(PROP_ESTIMATED_HOURS);
        this.estimatedHours = estimatedTime;
    }

    public Float getSpentHours() {
        return spentHours;
    }

    public void setSpentHours(Float spentHours) {
         updated.add(PROP_SPENT_HOURS);
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
        updated.add(PROP_PARENT_ID);
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
        updated.add(PROP_SUBJECT);
        this.subject = subject;
    }

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        updated.add(PROP_AUTHOR);
        this.author = author;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        updated.add(PROP_START_DATE);
        this.startDate = startDate;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public void setDueDate(Date dueDate) {
        updated.add(PROP_DUE_DATE);
        this.dueDate = dueDate;
    }

    public Tracker getTracker() {
        return tracker;
    }

    public void setTracker(Tracker tracker) {
        updated.add(PROP_TRACKER);
        this.tracker = tracker;
    }

    /**
     * Description is empty by default, not NULL.
     */
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        updated.add(PROP_DESCRIPTION);
        this.description = description;
    }

    public Date getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Date createdOn) {
        updated.add(PROP_CREATED_ON);
        this.createdOn = createdOn;
    }

    public Date getUpdatedOn() {
        return updatedOn;
    }

    public void setUpdatedOn(Date updatedOn) {
        updated.add(PROP_UPDATED_ON);
        this.updatedOn = updatedOn;
    }

    public Integer getStatusId() {
        return statusId;
    }

    public void setStatusId(Integer statusId) {
        updated.add(PROP_STATUS_ID);
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

    public String getNotes() {
        return notes;
    }

    /**
     * @param notes Some comment describing the issue update
     */
    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Collection<Journal> getJournals() {
        return Collections.unmodifiableCollection(journals);
    }

    public void addJournals(Collection<Journal> journals) {
        this.journals.addAll(journals);
    }

    public Collection<Changeset> getChangesets() {
        return Collections.unmodifiableCollection(changesets);
    }

    public void addChangesets(Collection<Changeset> changesets) {
        this.changesets.addAll(changesets);
    }

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
     *
     * @return relations or EMPTY collection if no relations, never returns NULL
     *
     * @see com.taskadapter.redmineapi.IssueManager#getIssueById(Integer id, Include... include)
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
        updated.add(PROP_PRIORITY_ID);
        this.priorityId = priorityId;
    }

    public Version getTargetVersion() {
        return targetVersion;
    }

    /**
     * @return attachments. the collection can be empty, but never null.
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
        updated.add(PROP_TARGET_VERSION);
        this.targetVersion = version;
    }

    public IssueCategory getCategory() {
        return category;
    }

    public void setCategory(IssueCategory category) {
        updated.add(PROP_CATEGORY);
        this.category = category;
    }
    
    /**
     * Check update state of an issue property.
     * 
     * @param property name of property to check, see the static PROP_* fields of this class
     * @return true if property was changed since last invocation of setUpdateTracking
     */
    public boolean wasUpdated(String property) {
        return updated.contains(property);
    }

    public boolean isUpdateTracking() {
        return updateTracking;
    }

    /**
     * Clear/set update tracking.
     * 
     * <p>updateTracking is only a state - calling this method does not change
     * the behaviour of the issue object, but is an indicator for other
     * methods working with issues.</p>
     * 
     * <p>It is the intention to provide a backwards compatible way of tracking
     * changes in the object state. Before the introduction of this updateTracking
     * null was used as an indicator of an unchanged value. This breaks when
     * trying to set the value to null (parent issue, duedate, ...).</p>
     * 
     * <p>Calling this method resets the wasUpdated state of all fields. So for this
     * sequence:</p>
     * 
     * <pre>
     * {@code
     * // retrieve the current state of the issue
     * Issue currentState = issueManager.getIssueById(12);
     * // enable update tracking and reset list of updated properties
     * currentState.setUpdateTracking(true);
     * // Change state of issue
     * currentState.setSubject("New subject");
     * // now the change can be detected (used when updating issue)
     * assert currentState.wasUpdated(Issue.PROP_SUBJECT);
     * // and it is visible, that updateTracking as used
     * assert currentState.isUpdateTracking();
     * }
     * </pre>
     * 
     * <p>It is expected, that this method is only called once the issue was
     * retrieved by the backend.</p>
     * 
     * @param updateTracking new state of update tracking
     */
    public void setUpdateTracking(boolean updateTracking) {
        this.updateTracking = updateTracking;
        updated.clear();
    }
}
