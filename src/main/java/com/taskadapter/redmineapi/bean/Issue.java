package com.taskadapter.redmineapi.bean;

import com.taskadapter.redmineapi.Include;
import com.taskadapter.redmineapi.NotFoundException;
import com.taskadapter.redmineapi.RedmineAuthenticationException;
import com.taskadapter.redmineapi.RedmineException;
import com.taskadapter.redmineapi.internal.RequestParam;
import com.taskadapter.redmineapi.internal.Transport;

import java.util.*;

/**
 * Redmine's Issue.
 * <p>
 * Note that methods returning lists of elements (like getRelations(), getWatchers(), etc return
 * unmodifiable collections.
 * You need to use methods like addRelations() if you want to add elements, e.g.:
 * <pre>
 *     issue.addRelations(Collections.singletonList(relation));
 * </pre>
 * 
 * @see <a href="http://www.redmine.org/projects/redmine/wiki/Rest_Issues">http://www.redmine.org/projects/redmine/wiki/Rest_Issues</a> 
 */
public class Issue implements Identifiable, FluentStyle {

    private final PropertyStorage storage = new PropertyStorage();

    public final static Property<Integer> DATABASE_ID = new Property<>(Integer.class, "id");
    public final static Property<String> SUBJECT = new Property<>(String.class, "subject");
    public final static Property<Date> START_DATE = new Property<>(Date.class, "startDate");
    public final static Property<Date> DUE_DATE = new Property<>(Date.class, "dueDate");
    public final static Property<Date> ACTUAL_START_DATE = new Property<>(Date.class, "actualStartDate");
    public final static Property<Date> ACTUAL_DUE_DATE = new Property<>(Date.class, "actualDueDate");
    public final static Property<Date> CREATED_ON = new Property<>(Date.class, "createdOn");
    public final static Property<Date> UPDATED_ON = new Property<>(Date.class, "updatedOn");
    public final static Property<Integer> DONE_RATIO = new Property<>(Integer.class, "doneRatio");
    public final static Property<Integer> PARENT_ID = new Property<>(Integer.class, "parentId");
    public final static Property<Integer> PRIORITY_ID = new Property<>(Integer.class, "priorityId");
    public final static Property<Float> ESTIMATED_HOURS = new Property<>(Float.class, "estimatedHours");
    public final static Property<Float> SPENT_HOURS = new Property<>(Float.class, "spentHours");
    public final static Property<Integer> ASSIGNEE_ID = new Property<>(Integer.class, "assigneeId");
    public final static Property<String> ASSIGNEE_NAME = new Property<>(String.class, "assigneeName");

    /**
     * Some comment describing an issue update.
     */
    public final static Property<String> NOTES = new Property<String>(String.class, "notes");
    public final static Property<Boolean> PRIVATE_NOTES = new Property<>(Boolean.class, "notes");
    public final static Property<String> PRIORITY_TEXT = new Property<>(String.class, "priorityText");
    public final static Property<Integer> PROJECT_ID = new Property<>(Integer.class, "projectId");
    public final static Property<String> PROJECT_NAME = new Property<>(String.class, "projectName");
    public final static Property<Integer> AUTHOR_ID = new Property<>(Integer.class, "authorId");
    public final static Property<String> AUTHOR_NAME = new Property<>(String.class, "authorName");
    public final static Property<Tracker> TRACKER = new Property<>(Tracker.class, "tracker");
    public final static Property<String> DESCRIPTION = new Property<>(String.class, "description");
    public final static Property<Date> CLOSED_ON = new Property<>(Date.class, "closedOn");
    public final static Property<Integer> STATUS_ID = new Property<>(Integer.class, "statusId");
    public final static Property<String> STATUS_NAME = new Property<>(String.class, "statusName");
    public final static Property<Version> TARGET_VERSION = new Property<>(Version.class, "targetVersion");
    public final static Property<IssueCategory> ISSUE_CATEGORY = new Property<>(IssueCategory.class, "issueCategory");
    public final static Property<Boolean> PRIVATE_ISSUE = new Property<>(Boolean.class, "privateIssue");

    /**
     * can't have two custom fields with the same ID in the collection, that's why it is declared
     * as a Set, not a List.
     */
    public final static Property<Set<CustomField>> CUSTOM_FIELDS = (Property<Set<CustomField>>) new Property(Set.class, "customFields");
    public final static Property<Set<Journal>> JOURNALS = (Property<Set<Journal>>) new Property(Set.class, "journals");
    public final static Property<Set<IssueRelation>> RELATIONS = (Property<Set<IssueRelation>>) new Property(Set.class, "relations");
    public final static Property<Set<Attachment>> ATTACHMENTS = (Property<Set<Attachment>>) new Property(Set.class, "attachments");
    public final static Property<Set<Changeset>> CHANGESETS = (Property<Set<Changeset>>) new Property(Set.class, "changesets");
    public final static Property<Set<Watcher>> WATCHERS = (Property<Set<Watcher>>) new Property(Set.class, "watchers");
    public final static Property<Set<Issue>> CHILDREN = (Property<Set<Issue>>) new Property(Set.class, "children");

    private Transport transport;

    public Issue() {
        initCollections(storage);
    }

    public Issue(Transport transport) {
        this();
        setTransport(transport);
    }

    /**
     * Each Issue object must have project Id set in order for Redmine 3.x to accept it via REST API.
     */
    public Issue(Transport transport, int projectId) {
        this();
        this.transport = transport;
        setProjectId(projectId);
    }

    /**
     * @param projectId Each Issue object must have project Id set in order for Redmine 3.x to accept it via REST API.
     */
    public Issue(Transport transport, int projectId, String subject) {
        this();
        this.transport = transport;
        setSubject(subject);
        setProjectId(projectId);
    }

    private void initCollections(PropertyStorage storage) {
        storage.set(CUSTOM_FIELDS, new HashSet<>());
        storage.set(CHILDREN, new HashSet<>());
        storage.set(WATCHERS, new HashSet<>());
        storage.set(CHANGESETS, new HashSet<>());
        storage.set(ATTACHMENTS, new HashSet<>());
        storage.set(RELATIONS, new HashSet<>());
        storage.set(JOURNALS, new HashSet<>());
    }

    public Integer getProjectId() {
        return storage.get(PROJECT_ID);
    }

    public Issue setProjectId(Integer projectId) {
        storage.set(PROJECT_ID, projectId);
        return this;
    }

    public String getProjectName() {
        return storage.get(PROJECT_NAME);
    }


    public Issue setProjectName(String name) {
        storage.set(PROJECT_NAME, name);
        return this;
    }

    /**
     * @param id database ID.
     */
    public Issue setId(Integer id) {
        storage.set(DATABASE_ID, id);
        return this;
    }

    public Integer getDoneRatio() {
        return storage.get(DONE_RATIO);
    }

    public Issue setDoneRatio(Integer doneRatio) {
        storage.set(DONE_RATIO, doneRatio);
        return this;
    }

    public String getPriorityText() {
        return storage.get(PRIORITY_TEXT);
    }

    /**
     * @deprecated This method has no effect when creating issues on Redmine Server, so we might as well just delete it
     * in the future releases.
     */
    public void setPriorityText(String priority) {
        storage.set(PRIORITY_TEXT, priority);
    }

    /**
     * Redmine can be configured to allow group assignments for issues:
     * Configuration option: Settings -> Issue Tracking -> Allow issue assignment to groups
     *
     * <p>An assignee can be a user or a group</p>
     */
    public Integer getAssigneeId() {
        return storage.get(ASSIGNEE_ID);
    }

    public Issue setAssigneeId(Integer assigneeId) {
        storage.set(ASSIGNEE_ID, assigneeId);
        return this;
    }

    public String getAssigneeName() {
        return storage.get(ASSIGNEE_NAME);
    }

    public Issue setAssigneeName(String assigneeName) {
        storage.set(ASSIGNEE_NAME, assigneeName);
        return this;
    }

    public Float getEstimatedHours() {
        return storage.get(ESTIMATED_HOURS);
    }

    public Issue setEstimatedHours(Float estimatedTime) {
        storage.set(ESTIMATED_HOURS, estimatedTime);
        return this;
    }

    public Float getSpentHours() {
        return storage.get(SPENT_HOURS);
    }

    public Issue setSpentHours(Float spentHours) {
        storage.set(SPENT_HOURS, spentHours);
        return this;
    }

  /**
     * Parent Issue ID, or NULL for issues without a parent.
     *
     * @return NULL, if there's no parent
     */
    public Integer getParentId() {
        return storage.get(PARENT_ID);
    }

    public Issue setParentId(Integer parentId) {
        storage.set(PARENT_ID, parentId);
        return this;
    }

    /**
     * @return database id for this object. can be NULL for Issues not added to Redmine yet
     */
    @Override
    public Integer getId() {
        return storage.get(DATABASE_ID);
    }

    public String getSubject() {
        return storage.get(SUBJECT);
    }

    public Issue setSubject(String subject) {
        storage.set(SUBJECT, subject);
        return this;
    }

    public Date getStartDate() {
        return storage.get(START_DATE);
    }

    public Issue setStartDate(Date startDate) {
        storage.set(START_DATE, startDate);
        return this;
    }

    public Date getDueDate() {
        return storage.get(DUE_DATE);
    }

    public Issue setDueDate(Date dueDate) {
        storage.set(DUE_DATE, dueDate);
        return this;
    }

    public Date getActualStartDate() {
        return storage.get(ACTUAL_START_DATE);
    }

    public Issue setActualStartDate(Date actualStartDate) {
        storage.set(ACTUAL_START_DATE, actualStartDate);
        return this;
    }

    public Date getActualDueDate() {
        return storage.get(ACTUAL_DUE_DATE);
    }

    public Issue setActualDueDate(Date actualStartDate) {
        storage.set(ACTUAL_DUE_DATE, actualStartDate);
        return this;
    }

    public Integer getAuthorId() {
        return storage.get(AUTHOR_ID);
    }

    /**
     * Marking as "deprecated": according to Redmine REST API docs
     * https://www.redmine.org/projects/redmine/wiki/Rest_Issues#Creating-an-issue , this parameter is not used
     * when creating issues (January 2020).
     */
    @Deprecated
    public Issue setAuthorId(Integer id) {
        storage.set(AUTHOR_ID, id);
        return this;
    }

    public String getAuthorName() {
        return storage.get(AUTHOR_NAME);
    }

    /**
     * Marking as "deprecated": according to Redmine REST API docs
     * https://www.redmine.org/projects/redmine/wiki/Rest_Issues#Creating-an-issue , this parameter is not used
     * when creating issues (January 2020).
     */
    @Deprecated
    public Issue setAuthorName(String name) {
        storage.set(AUTHOR_NAME, name);
        return this;
    }

    public Tracker getTracker() {
        return storage.get(TRACKER);
    }

    public Issue setTracker(Tracker tracker) {
        storage.set(TRACKER, tracker);
        return this;
    }

    public String getDescription() {
        return storage.get(DESCRIPTION);
    }

    public Issue setDescription(String description) {
        storage.set(DESCRIPTION, description);
        return this;
    }

    public Date getCreatedOn() {
        return storage.get(CREATED_ON);
    }

    public Issue setCreatedOn(Date createdOn) {
        storage.set(CREATED_ON, createdOn);
        return this;
    }

    public Date getUpdatedOn() {
        return storage.get(UPDATED_ON);
    }

    public Issue setUpdatedOn(Date updatedOn) {
        storage.set(UPDATED_ON, updatedOn);
        return this;
    }

    public Date getClosedOn() {
        return storage.get(CLOSED_ON);
    }

    public Issue setClosedOn(Date closedOn) {
        storage.set(CLOSED_ON, closedOn);
        return this;
    }

    public Integer getStatusId() {
        return storage.get(STATUS_ID);
    }

    public Issue setStatusId(Integer statusId) {
        storage.set(STATUS_ID, statusId);
        return this;
    }

    public String getStatusName() {
        return storage.get(STATUS_NAME);
    }

    public Issue setStatusName(String statusName) {
        storage.set(STATUS_NAME, statusName);
        return this;
    }

    /**
     * @return unmodifiable collection of Custom Field objects. the collection may be empty, but it is never NULL.
     */
    public Collection<CustomField> getCustomFields() {
        return Collections.unmodifiableCollection(storage.get(CUSTOM_FIELDS));
    }

    public Issue clearCustomFields() {
        storage.set(CUSTOM_FIELDS, new HashSet<>());
        return this;
    }

    /**
     * NOTE: The custom field(s) <strong>must have correct database ID set</strong> to be saved to Redmine.
     * This is Redmine REST API's requirement.
     */
    public Issue addCustomFields(Collection<CustomField> customFields) {
        storage.get(CUSTOM_FIELDS).addAll(customFields);
        return this;
    }

    /**
     * If there is a custom field with the same ID already present in the Issue,
     * the new field replaces the old one.
     *
     * @param customField the field to add to the issue.
     */
    public Issue addCustomField(CustomField customField) {
        storage.get(CUSTOM_FIELDS).add(customField);
        return this;
    }

    @Deprecated
    /**
     * This method should not be used by clients. "notes" only makes sense when creating/updating an issue - that is the
     * string value added along with the update.
     * <p>
     * use {@link #getJournals()} if you want to access previously saved notes. feel free to submit an enhancement
     * request to Redmine developers if you think this "notes - journals" separation looks weird...
     */
    public String getNotes() {
        return storage.get(NOTES);
    }

    /**
     * @param notes Some comment describing the issue update
     */
    public Issue setNotes(String notes) {
        storage.set(NOTES, notes);
        return this;
    }

    public boolean isPrivateNotes() {
        return storage.get(PRIVATE_NOTES);
    }

    /**
     * @param privateNotes mark note as private
     */
    public Issue setPrivateNotes(boolean privateNotes) {
        storage.set(PRIVATE_NOTES, privateNotes);
        return this;
    }

    /**
     * Don't forget to use Include.journals flag when loading issue from Redmine server:
     * <pre>
     *     Issue issue = issueManager.getIssueById(3205, Include.journals);
     * </pre>
     * @return unmodifiable collection of Journal entries or empty collection if no objects found. Never NULL.
     * @see com.taskadapter.redmineapi.Include#journals
     */
    public Collection<Journal> getJournals() {
        return Collections.unmodifiableCollection(storage.get(JOURNALS));
    }

    /**
     * Issue journals are created automatically when you update existing issues.
     * journal entries are essentially log records for changes you make.
     * you cannot just add log records without making actual changes.
     * this API method is misleading and it should only be used internally by Redmine Json parser
     * when parsing response from server. we should hide it from public.
     *
     * TODO hide this method. https://github.com/taskadapter/redmine-java-api/issues/199
     */
    public void addJournals(Collection<Journal> journals) {
        storage.get(JOURNALS).addAll(journals);
    }

    /**
     * Don't forget to use Include.changesets flag when loading issue from Redmine server:
     * <pre>
     *     Issue issue = issueManager.getIssueById(3205, Include.changesets);
     * </pre>
     * @return unmodifiable collection of entries or empty collection if no objects found.
     * @see com.taskadapter.redmineapi.Include#changesets
     */
    public Collection<Changeset> getChangesets() {
        return Collections.unmodifiableCollection(storage.get(CHANGESETS));
    }

    public Issue addChangesets(Collection<Changeset> changesets) {
        storage.get(CHANGESETS).addAll(changesets);
        return this;
    }

    /**
     * Don't forget to use Include.watchers flag when loading issue from Redmine server:
     * <pre>
     *     Issue issue = issueManager.getIssueById(3205, Include.watchers);
     * </pre>
     * @return unmodifiable collection of entries or empty collection if no objects found.
     * @see com.taskadapter.redmineapi.Include#watchers
     */
    public Collection<Watcher> getWatchers() {
        return Collections.unmodifiableCollection(storage.get(WATCHERS));
    }

    public Issue addWatchers(Collection<Watcher> watchers) {
        storage.get(WATCHERS).addAll(watchers);
        return this;
    }

    /**
      * Don't forget to use Include.children flag when loading issue from Redmine server:
      * <pre>
      *     Issue issue = issueManager.getIssueById(3205, Include.children);
      * </pre>
      * @return Collection of entries or empty collection if no objects found.
      * @see com.taskadapter.redmineapi.Include#children
      */
    public Collection<Issue> getChildren() {
        return Collections.unmodifiableCollection(storage.get(CHILDREN));
    }

    public Issue addChildren(Collection<Issue> children) {
        storage.get(CHILDREN).addAll(children);
        return this;
    }

    /**
     * Issues are considered equal if their IDs are equal. what about two issues with null ids?
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Issue issue = (Issue) o;

        if (getId() != null ? !getId().equals(issue.getId()) : issue.getId() != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return getId() != null ? getId().hashCode() : 0;
    }

    /**
     * @return the custom field with given Id or NULL if the field is not found
     */
    public CustomField getCustomFieldById(int customFieldId) {
        for (CustomField customField : storage.get(CUSTOM_FIELDS)) {
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
        for (CustomField customField : storage.get(CUSTOM_FIELDS)) {
            if (customFieldName.equals(customField.getName())) {
                return customField;
            }
        }
        return null;
    }


    @Override
    public String toString() {
        return "Issue [id=" + getId() + ", subject=" + getSubject() + "]";
    }

    /**
     * Relations are only loaded if you include Include.relations when loading the Issue.
     * <pre>
     *     Issue issue = issueManager.getIssueById(3205, Include.relations);
     * </pre>
     * <p>Since the returned collection is not modifiable, you need to use addRelations() method
     * if you want to add elements, e.g.:
     * <pre>
     *     issue.addRelations(Collections.singletonList(relation));
     * </pre>
     * @return unmodifiable collection of Relations or EMPTY collection if none found. Never returns NULL.
     * @see com.taskadapter.redmineapi.Include#relations
     */
    public Collection<IssueRelation> getRelations() {
        return Collections.unmodifiableCollection(storage.get(RELATIONS));
    }

    public Issue addRelations(Collection<IssueRelation> collection) {
        storage.get(RELATIONS).addAll(collection);
        return this;
    }

    public Integer getPriorityId() {
        return storage.get(PRIORITY_ID);
    }

    public Issue setPriorityId(Integer priorityId) {
        storage.set(PRIORITY_ID, priorityId);
        return this;
    }

    public Version getTargetVersion() {
        return storage.get(TARGET_VERSION);
    }

    /**
     * Don't forget to use <i>Include.attachments</i> flag when loading issue from Redmine server:
     * <pre>
     *     Issue issue = issueManager.getIssueById(3205, Include.attachments);
     * </pre>
     * @return unmodifiable collection of entries or empty collection if no objects found.
     * @see com.taskadapter.redmineapi.Include#attachments
     */
    public Collection<Attachment> getAttachments() {
        return Collections.unmodifiableCollection(storage.get(ATTACHMENTS));
    }

    public Issue addAttachments(Collection<Attachment> collection) {
        storage.get(ATTACHMENTS).addAll(collection);
        return this;
    }

    public Issue addAttachment(Attachment attachment) {
        storage.get(ATTACHMENTS).add(attachment);
        return this;
    }

    public Issue setTargetVersion(Version version) {
        storage.set(TARGET_VERSION, version);
        return this;
    }

    public IssueCategory getCategory() {
        return storage.get(ISSUE_CATEGORY);
    }

    public Issue setCategory(IssueCategory category) {
        storage.set(ISSUE_CATEGORY, category);
        return this;
    }

    /**
     * Default value is not determines. it's up to the server what it thinks the default value is if not set.
     */
    public boolean isPrivateIssue() {
        return storage.get(PRIVATE_ISSUE);
    }

    public Issue setPrivateIssue(boolean privateIssue) {
        storage.set(PRIVATE_ISSUE, privateIssue);
        return this;
    }

    public PropertyStorage getStorage() {
        return storage;
    }

    /**
     * @return the newly created Issue.
     *
     * @throws RedmineAuthenticationException invalid or no API access key is used with the server, which
     *                                 requires authorization. Check the constructor arguments.
     * @throws NotFoundException       the required project is not found
     * @throws RedmineException
     */
    public Issue create(RequestParam... params) throws RedmineException {
        RequestParam[] enrichParams = Arrays.copyOf(params, params.length + 1);
        enrichParams[params.length] = new RequestParam("include",
                Include.attachments.toString());
        return transport.addObject(this, enrichParams);
    }

    public void update(RequestParam... params) throws RedmineException {
        transport.updateObject(this, params);
    }

    public void delete() throws RedmineException {
        transport.deleteObject(Issue.class, Integer.toString(this.getId()));
    }

    public void addWatcher(int watcherId) throws RedmineException {
        transport.addWatcherToIssue(watcherId, getId());
    }

    public void deleteWatcher(int watcherId) throws RedmineException {
        transport.deleteChildId(Issue.class, Integer.toString(getId()), new Watcher(), watcherId);
    }

    @Override
    public void setTransport(Transport transport) {
        this.transport = transport;
        PropertyStorageUtil.updateCollections(storage, transport);
    }
}
