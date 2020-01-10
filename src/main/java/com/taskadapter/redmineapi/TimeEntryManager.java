package com.taskadapter.redmineapi;

import com.taskadapter.redmineapi.bean.TimeEntry;
import com.taskadapter.redmineapi.bean.TimeEntryActivity;
import com.taskadapter.redmineapi.internal.DirectObjectsSearcher;
import com.taskadapter.redmineapi.internal.RequestParam;
import com.taskadapter.redmineapi.internal.ResultsWrapper;
import com.taskadapter.redmineapi.internal.Transport;

import java.util.List;
import java.util.Map;

/**
 * Class to operate on Time Entry instances.
 * <p>
 * Sample usage:
 * <pre>
 RedmineManager redmineManager = RedmineManagerFactory.createWithUserAuth(redmineURI, login, password);
 redmineManager.getTimeEntryManager();
 * </pre>
 */
public final class TimeEntryManager {
    private final Transport transport;

    TimeEntryManager(Transport transport) {
        this.transport = transport;
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
                new RequestParam("issue_id", Integer.toString(issueId)));
    }

    /**
     * Direct method to search for objects using any Redmine REST API parameters you want.
     * <p>Unlike other getXXXObjects() methods in this library, this one does NOT handle paging for you so
     * you have to provide "offset" and "limit" parameters if you want to control paging.
     *
     * <p>Sample usage:
     <pre>
     final Map<String, String> params = new HashMap<String, String>();
     params.put("project_id", projectId);
     params.put("activity_id", activityId);
     final List<TimeEntry> elements = issueManager.getTimeEntries(params);
     </pre>

     * see other possible parameters on Redmine REST doc page:
     * http://www.redmine.org/projects/redmine/wiki/Rest_TimeEntries#Listing-time-entries
     *
     * @param parameters the http parameters key/value pairs to append to the rest api request
     * @return resultsWrapper with raw response from Redmine REST API
     * @throws RedmineAuthenticationException invalid or no API access key is used with the server, which
     *                                 requires authorization. Check the constructor arguments.
     * @throws RedmineException
     */
    public ResultsWrapper<TimeEntry> getTimeEntries(Map<String, String> parameters) throws RedmineException {
        return DirectObjectsSearcher.getObjectsListNoPaging(transport, parameters, TimeEntry.class);
    }

    @Deprecated
    public TimeEntry createTimeEntry(TimeEntry obj) throws RedmineException {
        return obj.create();
    }

    @Deprecated
    public void deleteTimeEntry(Integer id) throws RedmineException {
        new TimeEntry(transport).setId(id).delete();
    }

    public List<TimeEntryActivity> getTimeEntryActivities() throws RedmineException {
        return transport.getObjectsList(TimeEntryActivity.class);
    }

    @Deprecated
    public void update(TimeEntry obj) throws RedmineException {
        obj.update();
    }
}
