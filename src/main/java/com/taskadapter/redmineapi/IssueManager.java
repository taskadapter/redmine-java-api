package com.taskadapter.redmineapi;

import com.taskadapter.redmineapi.bean.Issue;
import com.taskadapter.redmineapi.bean.IssueCategory;
import com.taskadapter.redmineapi.bean.IssuePriority;
import com.taskadapter.redmineapi.bean.IssueRelation;
import com.taskadapter.redmineapi.bean.IssueRelationFactory;
import com.taskadapter.redmineapi.bean.IssueStatus;
import com.taskadapter.redmineapi.bean.Project;
import com.taskadapter.redmineapi.bean.ProjectFactory;
import com.taskadapter.redmineapi.bean.SavedQuery;
import com.taskadapter.redmineapi.bean.TimeEntry;
import com.taskadapter.redmineapi.bean.TimeEntryActivity;
import com.taskadapter.redmineapi.bean.Tracker;
import com.taskadapter.redmineapi.bean.Watcher;
import com.taskadapter.redmineapi.internal.Joiner;
import com.taskadapter.redmineapi.internal.Transport;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Works with Issues, Time Entries, Issue Statuses, Issue Relations.
 * <p>Obtain it via RedmineManager:
 * <pre>
 RedmineManager redmineManager = RedmineManagerFactory.createWithUserAuth(redmineURI, login, password);
 IssueManager issueManager = redmineManager.getIssueManager();
 * </pre>
 *
 * <p>Sample usage:
 * <pre>
 Issue issue = issueManager.getIssueById(3205, Include.journals, Include.relations, Include.attachments);
 System.out.println(issue.getJournals());
 * </pre>
 *
 * @see RedmineManager#getIssueManager()
 */
public class IssueManager {
    private final Transport transport;

    IssueManager(Transport transport) {
        this.transport = transport;
    }

    /**
     * There could be several issues with the same summary, so the method returns List.
     *
     * @return empty list if not issues with this summary field exist, never NULL
     * @throws RedmineAuthenticationException invalid or no API access key is used with the server, which
     *                                 requires authorization. Check the constructor arguments.
     * @throws NotFoundException
     * @throws RedmineException
     */
    public List<Issue> getIssuesBySummary(String projectKey, String summaryField) throws RedmineException {
        if ((projectKey != null) && (projectKey.length() > 0)) {
            return transport.getObjectsList(Issue.class,
                    new BasicNameValuePair("subject", summaryField),
                    new BasicNameValuePair("project_id", projectKey));
        } else {
            return transport.getObjectsList(Issue.class,
                    new BasicNameValuePair("subject", summaryField));
        }
    }

    /**
     * Direct method to search for issues using any Redmine REST API parameters you want.
     * <p>Unlike other getXXXObjects() methods in this library, this one does NOT handle paging for you so
     * you have to provide "offset" and "limit" parameters if you want to control paging.
     *
     * @param pParameters the http parameters key/value pairs to append to the rest api request
     * @return empty list if no issues found matching given parameters
     * @throws RedmineAuthenticationException invalid or no API access key is used with the server, which
     *                                 requires authorization. Check the constructor arguments.
     * @throws RedmineException
     */
    public List<Issue> getIssues(Map<String, String> pParameters) throws RedmineException {
        Set<NameValuePair> params = new HashSet<NameValuePair>();

        for (final Map.Entry<String, String> param : pParameters.entrySet()) {
            params.add(new BasicNameValuePair(param.getKey(), param.getValue()));
        }

        final Transport.ResultsWrapper<Issue> wrapper = transport.getObjectsListNoPaging(Issue.class, params);
        return wrapper.getResults();
    }

    /**
     * @param id      Redmine issue Id
     * @param include list of "includes". e.g. "relations", "journals", ...
     * @return Issue object. never Null: an exception is thrown if the issue is not found (see Throws section).
     * @throws RedmineAuthenticationException invalid or no API access key is used with the server, which
     *                                 requires authorization. Check the constructor arguments.
     * @throws NotFoundException       the issue with the given id is not found on the server
     * @throws RedmineException
     */
    public Issue getIssueById(Integer id, Include... include) throws RedmineException {
        String value = Joiner.join(",", include);
        return transport.getObject(Issue.class, id, new BasicNameValuePair("include", value));
    }

    public void addWatcherToIssue(Watcher watcher, Issue issue) throws RedmineException {
        transport.addWatcherToIssue(watcher.getId(), issue.getId());
    }

    public void deleteWatcherFromIssue(Watcher watcher, Issue issue) throws RedmineException {
        transport.deleteChildId(Issue.class, Integer.toString(issue.getId()), watcher, watcher.getId());
    }

    /**
     * DEPRECATED. use createIssue(Issue issue) instead.
     * "projectKey" parameter required by this method duplicates what is already available in "issue" parameter.
     * Also, some Redmine APIs require project database ID instead of the string key (e.g. when updating Issue).
     * To keep things consistent, we propose using project database IDs for other operations as well, like
     * for creating new issues. Which means you need to create a Project instance with the project numeric ID
     * and set it to the new Issue object - see createIssue(Issue issue) method.
     * <p>
     * Sample usage:
     * <pre>
     * {@code
     *   Issue issueToCreate = IssueFactory.create();
     *   issueToCreate.setSubject("This is the summary line 123");
     *   Issue newIssue = mgr.createIssue(PROJECT_KEY, issueToCreate);
     * }
     * </pre>
     *
     * @param projectKey The project "identifier". This is a string key like "project-ABC", NOT a database numeric ID.
     * @param issue      the Issue object to create on the server.
     * @return the newly created Issue.
     * @throws RedmineAuthenticationException invalid or no API access key is used with the server, which
     *                                 requires authorization. Check the constructor arguments.
     * @throws NotFoundException       the project with the given projectKey is not found
     * @throws RedmineException
     * @see #createIssue(com.taskadapter.redmineapi.bean.Issue)
     */
    @Deprecated
    public Issue createIssue(String projectKey, Issue issue) throws RedmineException {
        final Project oldProject = issue.getProject();
        final Project newProject = ProjectFactory.create();
        newProject.setIdentifier(projectKey);
        issue.setProject(newProject);
        try {
            return transport.addObject(issue, new BasicNameValuePair("include",
                    Include.attachments.toString()));
        } finally {
            issue.setProject(oldProject);
        }
    }

    /**
     * Sample usage:
     * <pre>
     * {@code
     *   Issue issueToCreate = IssueFactory.create(projectDatabaseId, subject);
     *   Issue newIssue = mgr.createIssue(issueToCreate);
     * }
     * </pre>
     *
     * @param issue      the Issue object to create on the server.
     * @return the newly created Issue.
     * @throws RedmineAuthenticationException invalid or no API access key is used with the server, which
     *                                 requires authorization. Check the constructor arguments.
     * @throws NotFoundException       the project is not found
     * @throws RedmineException
     */
    public Issue createIssue(Issue issue) throws RedmineException {
        return transport.addObject(issue, new BasicNameValuePair("include",
               Include.attachments.toString()));
    }

    public void deleteIssue(Integer id) throws RedmineException {
        transport.deleteObject(Issue.class, Integer.toString(id));
    }

    /**
     * @param projectKey ignored if NULL
     * @param queryId    id of the saved query in Redmine. the query must be accessible to the user
     *                   represented by the API access key (if the Redmine project requires authorization).
     *                   This parameter is <strong>optional</strong>, NULL can be provided to get all available issues.
     * @return list of Issue objects
     * @throws RedmineAuthenticationException invalid or no API access key is used with the server, which
     *                                 requires authorization. Check the constructor arguments.
     * @throws RedmineException
     * @see Issue
     */
    public List<Issue> getIssues(String projectKey, Integer queryId, Include... include) throws RedmineException {
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        if (queryId != null) {
            params.add(new BasicNameValuePair("query_id", String.valueOf(queryId)));
        }

        if ((projectKey != null) && (projectKey.length() > 0)) {
            params.add(new BasicNameValuePair("project_id", projectKey));
        }
        String includeStr = Joiner.join(",", include);
        params.add(new BasicNameValuePair("include", includeStr));

        return transport.getObjectsList(Issue.class, params);
    }

    /**
     * @param issueId id of the source issue
     * @param issueToId if of the target issue
     * @param type type of the relation. e.g. "precedes". see IssueRelation.TYPE for possible types.
     * @return newly created IssueRelation instance.
     *
     * @throws RedmineException
     * @see IssueRelation.TYPE
     */
    public IssueRelation createRelation(Integer issueId, Integer issueToId, String type) throws RedmineException {
        IssueRelation toCreate = IssueRelationFactory.create();
        toCreate.setIssueId(issueId);
        toCreate.setIssueToId(issueToId);
        toCreate.setType(type);
        return transport.addChildEntry(Issue.class, issueId.toString(),
                toCreate);
    }

    /**
     * Delete Issue Relation with the given Id.
     */
    public void deleteRelation(Integer id) throws RedmineException {
        transport.deleteObject(IssueRelation.class, Integer.toString(id));
    }

    /**
     * Delete all issue's relations
     */
    public void deleteIssueRelations(Issue redmineIssue) throws RedmineException {
        for (IssueRelation relation : redmineIssue.getRelations()) {
            deleteRelation(relation.getId());
        }
    }

    /**
     * Delete relations for the given issue ID.
     *
     * @param issueId issue ID
     */
    public void deleteIssueRelationsByIssueId(Integer issueId) throws RedmineException {
        Issue issue = getIssueById(issueId, Include.relations);
        deleteIssueRelations(issue);
    }

    public List<IssuePriority> getIssuePriorities() throws RedmineException {
        return transport.getObjectsList(IssuePriority.class);
    }

    public List<TimeEntry> getTimeEntries() throws RedmineException {
        return transport.getObjectsList(TimeEntry.class);
    }

    /**
     * @param id the database Id of the TimeEntry record
     */
    public TimeEntry getTimeEntry(Integer id) throws RedmineException {
        return transport.getObject(TimeEntry.class, id);
    }

    public List<TimeEntry> getTimeEntriesForIssue(Integer issueId) throws RedmineException {
        return transport.getObjectsList(TimeEntry.class,
                new BasicNameValuePair("issue_id", Integer.toString(issueId)));
    }

    public TimeEntry createTimeEntry(TimeEntry obj) throws RedmineException {
        validate(obj);
        return transport.addObject(obj);
    }

    private void validate(TimeEntry obj) {
        if (!obj.isValid()) {
            throw new IllegalArgumentException("You have to either define a Project or Issue ID for a Time Entry. "
                        + "The given Time Entry object has neither defined.");
        }
    }

    public void deleteTimeEntry(Integer id) throws RedmineException {
        transport.deleteObject(TimeEntry.class, Integer.toString(id));
    }

    public List<TimeEntryActivity> getTimeEntryActivities() throws RedmineException {
        return transport.getObjectsList(TimeEntryActivity.class);
    }

    public void update(TimeEntry obj) throws RedmineException {
        validate(obj);
        transport.updateObject(obj);
    }

    public void update(Issue obj) throws RedmineException {
        transport.updateObject(obj);
    }

    /**
     * delivers a list of {@link com.taskadapter.redmineapi.bean.IssueCategory}s of a {@link Project}
     *
     * @param projectID the ID of the {@link Project}
     * @return the list of {@link com.taskadapter.redmineapi.bean.IssueCategory}s of the {@link Project}
     * @throws RedmineAuthenticationException thrown in case something went wrong while trying to login
     * @throws RedmineException        thrown in case something went wrong in Redmine
     * @throws NotFoundException       thrown in case an object can not be found
     */
    public List<IssueCategory> getCategories(int projectID) throws RedmineException {
        return transport.getChildEntries(Project.class,
                Integer.toString(projectID), IssueCategory.class);
    }

    /**
     * creates a new {@link IssueCategory} for the {@link Project} contained. <br>
     * Pre-condition: the attribute {@link Project} for the {@link IssueCategory} must
     * not be null!
     *
     * @param category the {@link IssueCategory}. Must contain a {@link Project}.
     * @return the new {@link IssueCategory} created by Redmine
     * @throws IllegalArgumentException thrown in case the category does not contain a project.
     * @throws RedmineAuthenticationException  thrown in case something went wrong while trying to login
     * @throws RedmineException         thrown in case something went wrong in Redmine
     * @throws NotFoundException        thrown in case an object can not be found
     */
    public IssueCategory createCategory(IssueCategory category) throws RedmineException {
        if (category.getProject() == null
                || category.getProject().getId() == null) {
            throw new IllegalArgumentException(
                    "IssueCategory must contain an existing project");
        }

        return transport.addChildEntry(Project.class, category.getProject()
                .getId().toString(), category);
    }

    /**
     * deletes an {@link IssueCategory}. <br>
     *
     * @param category the {@link IssueCategory}.
     * @throws RedmineAuthenticationException thrown in case something went wrong while trying to login
     * @throws RedmineException        thrown in case something went wrong in Redmine
     * @throws NotFoundException       thrown in case an object can not be found
     */
    public void deleteCategory(IssueCategory category) throws RedmineException {
        transport.deleteObject(IssueCategory.class,
                Integer.toString(category.getId()));
    }

    /**
     * Delivers a list of existing {@link com.taskadapter.redmineapi.bean.IssueStatus}es.
     *
     * @return a list of existing {@link com.taskadapter.redmineapi.bean.IssueStatus}es.
     * @throws RedmineAuthenticationException thrown in case something went wrong while trying to login
     * @throws RedmineException        thrown in case something went wrong in Redmine
     * @throws NotFoundException       thrown in case an object can not be found
     */
    public List<IssueStatus> getStatuses() throws RedmineException {
        return transport.getObjectsList(IssueStatus.class);
    }

    /**
     * @return a list of all {@link com.taskadapter.redmineapi.bean.Tracker}s available (like "Bug", "Task", "Feature")
     * @throws RedmineAuthenticationException thrown in case something went wrong while trying to login
     * @throws RedmineException        thrown in case something went wrong in Redmine
     * @throws NotFoundException       thrown in case an object can not be found
     */
    public List<Tracker> getTrackers() throws RedmineException {
        return transport.getObjectsList(Tracker.class);
    }

    /**
     * Get "saved queries" for the given project available to the current user.
     *
     * <p>This REST API feature was added in Redmine 1.3.0. See http://www.redmine.org/issues/5737</p>
     */
    public List<SavedQuery> getSavedQueries(String projectKey) throws RedmineException {
        Set<NameValuePair> params = new HashSet<NameValuePair>();

        if ((projectKey != null) && (projectKey.length() > 0)) {
            params.add(new BasicNameValuePair("project_id", projectKey));
        }

        return transport.getObjectsList(SavedQuery.class, params);
    }

    /**
     * Get all "saved queries" available to the current user.
     * 
     * <p>This REST API feature was added in Redmine 1.3.0. See http://www.redmine.org/issues/5737</p>
     */
    public List<SavedQuery> getSavedQueries() throws RedmineException {
        return transport.getObjectsList(SavedQuery.class);
    }

}
