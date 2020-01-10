package com.taskadapter.redmineapi;

import com.taskadapter.redmineapi.bean.Issue;
import com.taskadapter.redmineapi.bean.IssueCategory;
import com.taskadapter.redmineapi.bean.IssuePriority;
import com.taskadapter.redmineapi.bean.IssueRelation;
import com.taskadapter.redmineapi.bean.IssueStatus;
import com.taskadapter.redmineapi.bean.Project;
import com.taskadapter.redmineapi.bean.SavedQuery;
import com.taskadapter.redmineapi.bean.Tracker;
import com.taskadapter.redmineapi.bean.Watcher;
import com.taskadapter.redmineapi.internal.DirectObjectsSearcher;
import com.taskadapter.redmineapi.internal.Joiner;
import com.taskadapter.redmineapi.internal.RequestParam;
import com.taskadapter.redmineapi.internal.ResultsWrapper;
import com.taskadapter.redmineapi.internal.Transport;

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
                    new RequestParam("subject", summaryField),
                    new RequestParam("project_id", projectKey));
        } else {
            return transport.getObjectsList(Issue.class,
                    new RequestParam("subject", summaryField));
        }
    }

    /**
     * Direct method to search for issues using any Redmine REST API parameters you want.
     * <p>Unlike other getXXXObjects() methods in this library, this one does NOT handle paging for you so
     * you have to provide "offset" and "limit" parameters if you want to control paging.
     *
     * <p>Sample usage:
     <pre>
     final Map<String, String> params = new HashMap<String, String>();
     params.put("project_id", projectId);
     params.put("subject", "~free_form_search");
     final List<Issue> issues = issueManager.getIssues(params);
     </pre>

     * @param parameters the http parameters key/value pairs to append to the rest api request
     * @return resultsWrapper with raw response from Redmine REST API
     * @throws RedmineAuthenticationException invalid or no API access key is used with the server, which
     *                                 requires authorization. Check the constructor arguments.
     * @throws RedmineException
     */
    public ResultsWrapper<Issue> getIssues(Map<String, String> parameters) throws RedmineException {
        return DirectObjectsSearcher.getObjectsListNoPaging(transport, parameters, Issue.class);
    }

    /**
     * Free-form search that does not do any paging for you. Btw, where is Redmine free-form search documentation??
     * <p>
     * Sample usage:
     * <pre>

     Params params = new Params()
     .add("set_filter", "1")
     .add("f[]", "summary")
     .add("op[summary]", "~")
     .add("v[summary]", "another")
     .add("f[]", "description")
     .add("op[description]", "~")
     .add("v[description][]", "abc");

     list = issueManager.getIssues(params);

     * </pre>
     * @param parameters
     */
    public ResultsWrapper<Issue> getIssues(Params parameters) throws RedmineException {
        return transport.getObjectsListNoPaging(Issue.class, parameters.getList());
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
        return transport.getObject(Issue.class, id, new RequestParam("include", value));
    }

    /**
     * DEPRECATED. use issue.addWatcher(..)
     */
    @Deprecated
    public void addWatcherToIssue(Watcher watcher, Issue issue) throws RedmineException {
        transport.addWatcherToIssue(watcher.getId(), issue.getId());
    }

    /**
     * DEPRECATED. use issue.deleteWatcher(..)
     */
    @Deprecated
    public void deleteWatcherFromIssue(Watcher watcher, Issue issue) throws RedmineException {
        transport.deleteChildId(Issue.class, Integer.toString(issue.getId()), watcher, watcher.getId());
    }

    /**
     * Deprecated. Use the new fluent-style API: new Issue(...).create();
     *
     * @param issue      the Issue object to create on the server.
     * @return the newly created Issue.
     * @throws RedmineAuthenticationException invalid or no API access key is used with the server, which
     *                                 requires authorization. Check the constructor arguments.
     * @throws NotFoundException       the project is not found
     * @throws RedmineException
     */
    @Deprecated
    public Issue createIssue(Issue issue) throws RedmineException {
        return issue.create();
    }

    /**
     * Deprecated. use issue.delete() instead.
     *
     * @throws RedmineException
     */
    @Deprecated
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
        List<RequestParam> params = new ArrayList<>();
        if (queryId != null) {
            params.add(new RequestParam("query_id", String.valueOf(queryId)));
        }

        if ((projectKey != null) && (projectKey.length() > 0)) {
            params.add(new RequestParam("project_id", projectKey));
        }
        String includeStr = Joiner.join(",", include);
        params.add(new RequestParam("include", includeStr));

        return transport.getObjectsList(Issue.class, params);
    }

    /**
     * DEPRECATED. use relation.create()
     */
    @Deprecated
    public IssueRelation createRelation(Integer issueId, Integer issueToId, String type) throws RedmineException {
        return new IssueRelation(transport, issueId, issueToId, type)
                .create();
    }

    /**
     * DEPRECATED. use relation.delete()
     */
    @Deprecated
    public void deleteRelation(Integer id) throws RedmineException {
        new IssueRelation(transport).setId(id).delete();
    }

    /**
     * DEPRECATED. use relation.delete()
     */
    @Deprecated
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

    /**
     * Deprecated. use issue.update() instead.
     */
    @Deprecated
    public void update(Issue obj) throws RedmineException {
        obj.update();
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
     * DEPRECATED. use category.create() instead.
     *
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
    @Deprecated
    public IssueCategory createCategory(IssueCategory category) throws RedmineException {
        if (category.getProjectId() == null) {
            throw new IllegalArgumentException(
                    "IssueCategory must contain projectId");
        }

        return transport.addChildEntry(Project.class, category.getProjectId().toString(), category);
    }

    /**
     * DEPRECATED. use category.delete() instead
     *
     * deletes an {@link IssueCategory}. <br>
     *
     * @param category the {@link IssueCategory}.
     * @throws RedmineAuthenticationException thrown in case something went wrong while trying to login
     * @throws RedmineException        thrown in case something went wrong in Redmine
     * @throws NotFoundException       thrown in case an object can not be found
     */
    @Deprecated
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
        Set<RequestParam> params = new HashSet<>();

        if ((projectKey != null) && (projectKey.length() > 0)) {
            params.add(new RequestParam("project_id", projectKey));
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
