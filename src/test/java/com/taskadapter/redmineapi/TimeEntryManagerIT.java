package com.taskadapter.redmineapi;

import com.taskadapter.redmineapi.bean.Issue;
import com.taskadapter.redmineapi.bean.Project;
import com.taskadapter.redmineapi.bean.TimeEntry;
import com.taskadapter.redmineapi.internal.Transport;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.taskadapter.redmineapi.IssueHelper.createIssue;
import static com.taskadapter.redmineapi.IssueHelper.createIssues;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class TimeEntryManagerIT {
    // TODO We don't know activities' IDs!
    // see feature request http://www.redmine.org/issues/7506
    private static final Integer ACTIVITY_ID = 8;

    private static ProjectManager projectManager;
    private static TimeEntryManager timeEntryManager;
    private static String projectKey;
    private static Integer projectId;
    private static Transport transport;
    private static Project createdProject;

    @BeforeClass
    public static void oneTimeSetUp() {
        RedmineManager mgr = IntegrationTestHelper.createRedmineManager();
        transport = mgr.getTransport();
        timeEntryManager = mgr.getTimeEntryManager();
        projectManager = mgr.getProjectManager();

        try {
            createdProject = new Project(transport, "test project",
                    "test" + Calendar.getInstance().getTimeInMillis())
                    .create();
            projectKey = createdProject.getIdentifier();
            projectId = createdProject.getId();
        } catch (Exception e) {
            Assert.fail("can't create a test project. " + e.getMessage());
        }
    }

    @AfterClass
    public static void oneTimeTearDown() {
        try {
            if (projectManager != null && projectKey != null) {
                createdProject.delete();
            }
        } catch (Exception e) {
            Assert.fail("can't delete the test project '" + projectKey
                    + ". reason: " + e.getMessage());
        }
    }

    @Test
    public void testTimeEntryDefaults() throws RedmineException {
        Issue issue = new Issue(transport, projectId, "aaabbbccc").create();

        try {
            TimeEntry result = new TimeEntry(transport)
                    .setHours(123.f)
                    .setActivityId(ACTIVITY_ID)
                    .setIssueId(issue.getId())
                    .create();
            try {
                Assert.assertNotNull(result.getId());
                Assert.assertNotNull(result.getIssueId());
                Assert.assertNotNull(result.getProjectId());
                Assert.assertNotNull(result.getProjectName());
                Assert.assertNotNull(result.getUserName());
                Assert.assertNotNull(result.getUserId());
                Assert.assertNotNull(result.getActivityName());
                Assert.assertNotNull(result.getActivityId());
                Assert.assertEquals(Float.valueOf(123.0f), result.getHours());
                Assert.assertEquals("", result.getComment());
                Assert.assertNotNull(result.getSpentOn());
                Assert.assertNotNull(result.getCreatedOn());
                Assert.assertNotNull(result.getUpdatedOn());
            } finally {
                result.delete();
            }
        } finally {
            issue.delete();
        }
    }


    /**
     * Test for issue 64 (time entry format)
     */
    @Test
    public void testTimeEntryComments() throws RedmineException {
        Issue issue = createIssues(transport, projectId, 1).get(0);
        Integer issueId = issue.getId();

        String comment = "This is a comment although it may not look like it";
        TimeEntry entry = new TimeEntry(transport)
                .setHours(11f)
                .setIssueId(issueId)
                .setComment(comment)
                // TODO We don't know activities IDs! see feature request http://www.redmine.org/issues/7506
                .setActivityId(ACTIVITY_ID)
                .create();

        assertNotNull(entry);
        assertEquals(comment, entry.getComment());

        entry.setComment("New comment")
                .update();
        TimeEntry updatedEntry = timeEntryManager.getTimeEntry(entry.getId());
        assertEquals("New comment", updatedEntry.getComment());
    }

    @Test
    public void testTimeEntryActivities() throws RedmineException {
        assertTrue(timeEntryManager.getTimeEntryActivities().size() > 0);
    }

    @Test
    public void testGetTimeEntries() throws RedmineException {
        List<TimeEntry> list = timeEntryManager.getTimeEntries();
        assertNotNull(list);
    }

    @Test
    public void testCreateGetTimeEntry() throws RedmineException {
        Issue issue = createIssues(transport, projectId, 1).get(0);
        Integer issueId = issue.getId();

        Float hours = 11f;
        TimeEntry entry = new TimeEntry(transport)
                .setHours(11f)
                .setIssueId(issueId)
                // TODO We don't know activities IDs! see feature request http://www.redmine.org/issues/7506
                .setActivityId(ACTIVITY_ID)
                .create();

        assertNotNull(entry);
        assertEquals(hours, entry.getHours());

        Float newHours = 22f;
        entry.setHours(newHours)
                .update();

        TimeEntry updatedEntry = timeEntryManager.getTimeEntry(entry.getId());
        assertEquals(newHours, updatedEntry.getHours());
    }

    @Test(expected = NotFoundException.class)
    public void testCreateDeleteTimeEntry() throws RedmineException {
        Issue issue = createIssues(transport, projectId, 1).get(0);
        Integer issueId = issue.getId();

        Float hours = 4f;
        TimeEntry entry = new TimeEntry(transport)
                .setHours(hours)
                .setIssueId(issueId)
                .setActivityId(ACTIVITY_ID)
                .create();
        assertNotNull(entry);

        entry.delete();
        timeEntryManager.getTimeEntry(entry.getId());
    }

    @Test
    public void testGetTimeEntriesForIssue() throws RedmineException {
        Issue issue = createIssues(transport, projectId, 1).get(0);
        Integer issueId = issue.getId();
        Float hours1 = 2f;
        Float hours2 = 7f;
        Float totalHoursExpected = hours1 + hours2;
        TimeEntry createdEntry1 = createTimeEntry(issueId, hours1);
        TimeEntry createdEntry2 = createTimeEntry(issueId, hours2);
        assertNotNull(createdEntry1);
        assertNotNull(createdEntry2);

        List<TimeEntry> entries = timeEntryManager.getTimeEntriesForIssue(issueId);
        assertEquals(2, entries.size());
        Float totalTime = 0f;
        for (TimeEntry timeEntry : entries) {
            totalTime += timeEntry.getHours();
        }
        assertEquals(totalHoursExpected, totalTime);
    }

    private TimeEntry createTimeEntry(Integer issueId, float hours) throws RedmineException {
        return createTimeEntry(issueId, hours, ACTIVITY_ID);
    }

    private TimeEntry createTimeEntry(Integer issueId, float hours, int activityId)
            throws RedmineException {
        return new TimeEntry(transport)
                .setHours(hours)
                .setIssueId(issueId)
                .setActivityId(activityId)
                .create();
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidTimeEntryFailsWithIAEOnCreate() throws RedmineException {
        timeEntryManager.createTimeEntry(createIncompleteTimeEntry());
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidTimeEntryFailsWithIAEOnUpdate() throws RedmineException {
        timeEntryManager.update(createIncompleteTimeEntry());
    }

    private TimeEntry createIncompleteTimeEntry() {
        return new TimeEntry(transport)
                .setActivityId(ACTIVITY_ID)
                .setSpentOn(new Date())
                .setHours(1.5f);
    }

    @Test
    public void testViolateTimeEntryConstraint_ProjectOrIssueID() throws RedmineException {
        TimeEntry timeEntry = createIncompleteTimeEntry();
        timeEntry.setProjectId(projectId);
        timeEntryManager.createTimeEntry(timeEntry);
        // no exceptions - good.
    }

    /**
     * This integration test requires activities with ids 8 and 9 to be present on
     * the redmine server. we cannot detect Ids of existing activities -
     * see Redmine feature request http://www.redmine.org/issues/7506
     */
    @Test
    public void timeEntriesAreFoundByFreeFormSearch() throws RedmineException {
        // create some random issues in the project
        Issue createdIssue = createIssue(transport, projectId);

        // TODO We don't know activities' IDs
        // see feature request http://www.redmine.org/issues/7506
        Integer createdIssueId = createdIssue.getId();
        createTimeEntry(createdIssueId, 2, 8);
        createTimeEntry(createdIssueId, 6, 8);
        createTimeEntry(createdIssueId, 10, 8);
        createTimeEntry(createdIssueId, 30, 9);

        try {
            Map<String, String> paramsForActivity8 = new HashMap<>();
            paramsForActivity8.put("issue_id", Integer.toString(createdIssueId));
            paramsForActivity8.put("activity_id", ACTIVITY_ID + "");
            List<TimeEntry> timeEntriesForActivity8 = timeEntryManager.getTimeEntries(paramsForActivity8).getResults();
            assertThat(timeEntriesForActivity8.size()).isEqualTo(3);

            Map<String, String> paramsForActivity9 = new HashMap<>();
            paramsForActivity9.put("issue_id", Integer.toString(createdIssueId));
            paramsForActivity9.put("activity_id", "9");
            List<TimeEntry> timeEntriesForActivity9 = timeEntryManager.getTimeEntries(paramsForActivity9).getResults();
            assertThat(timeEntriesForActivity9.size()).isEqualTo(1);
        } finally {
            createdIssue.delete();
        }
    }


}