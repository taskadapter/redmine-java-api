package com.taskadapter.redmineapi;

import com.taskadapter.redmineapi.bean.Issue;
import com.taskadapter.redmineapi.bean.IssueFactory;
import com.taskadapter.redmineapi.bean.Project;
import com.taskadapter.redmineapi.bean.ProjectFactory;
import com.taskadapter.redmineapi.bean.TimeEntry;
import com.taskadapter.redmineapi.bean.TimeEntryFactory;
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
    private static IssueManager issueManager;
    private static String projectKey;
    private static Integer projectId;

    @BeforeClass
    public static void oneTimeSetUp() {
        RedmineManager mgr = IntegrationTestHelper.createRedmineManager();
        timeEntryManager = mgr.getTimeEntryManager();
        projectManager = mgr.getProjectManager();
        issueManager = mgr.getIssueManager();

        Project junitTestProject = ProjectFactory.create("test project", "test" + Calendar.getInstance().getTimeInMillis());

        try {
            Project createdProject = projectManager.createProject(junitTestProject);
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
                projectManager.deleteProject(projectKey);
            }
        } catch (Exception e) {
            Assert.fail("can't delete the test project '" + projectKey
                    + ". reason: " + e.getMessage());
        }
    }

    @Test
    public void testTimeEntryDefaults() throws RedmineException {
        final TimeEntry template = TimeEntryFactory.create();

        final Issue tmp = IssueFactory.create(projectId, "aaabbbccc");
        final Issue tmpIssue = issueManager.createIssue(tmp);
        try {
            template.setHours(123.f);
            template.setActivityId(ACTIVITY_ID);
            template.setIssueId(tmpIssue.getId());
            final TimeEntry result = timeEntryManager.createTimeEntry(template);
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
                timeEntryManager.deleteTimeEntry(result.getId());
            }
        } finally {
            issueManager.deleteIssue(tmpIssue.getId());
        }
    }


    /**
     * Test for issue 64 (time entry format)
     */
    @Test
    public void testTimeEntryComments() throws RedmineException {
        Issue issue = createIssues(issueManager, projectId, 1).get(0);
        Integer issueId = issue.getId();

        TimeEntry entry = TimeEntryFactory.create();
        Float hours = 11f;
        entry.setHours(hours);
        entry.setIssueId(issueId);
        final String comment = "This is a comment although it may not look like it";
        entry.setComment(comment);
        // TODO We don't know activities IDs!
        // see feature request http://www.redmine.org/issues/7506
        entry.setActivityId(ACTIVITY_ID);
        TimeEntry createdEntry = timeEntryManager.createTimeEntry(entry);

        assertNotNull(createdEntry);
        assertEquals(comment, createdEntry.getComment());

        createdEntry.setComment("New comment");
        timeEntryManager.update(createdEntry);
        final TimeEntry updatedEntry = timeEntryManager.getTimeEntry(createdEntry.getId());
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
        Issue issue = createIssues(issueManager, projectId, 1).get(0);
        Integer issueId = issue.getId();

        TimeEntry entry = TimeEntryFactory.create();
        Float hours = 11f;
        entry.setHours(hours);
        entry.setIssueId(issueId);
        // TODO We don't know activities IDs!
        // see feature request http://www.redmine.org/issues/7506
        entry.setActivityId(ACTIVITY_ID);
        TimeEntry createdEntry = timeEntryManager.createTimeEntry(entry);

        assertNotNull(createdEntry);
        assertEquals(hours, createdEntry.getHours());

        Float newHours = 22f;
        createdEntry.setHours(newHours);

        timeEntryManager.update(createdEntry);

        TimeEntry updatedEntry = timeEntryManager.getTimeEntry(createdEntry.getId());
        assertEquals(newHours, updatedEntry.getHours());
    }

    @Test(expected = NotFoundException.class)
    public void testCreateDeleteTimeEntry() throws RedmineException {
        Issue issue = createIssues(issueManager, projectId, 1).get(0);
        Integer issueId = issue.getId();

        TimeEntry entry = TimeEntryFactory.create();
        Float hours = 4f;
        entry.setHours(hours);
        entry.setIssueId(issueId);
        entry.setActivityId(ACTIVITY_ID);
        TimeEntry createdEntry = timeEntryManager.createTimeEntry(entry);
        assertNotNull(createdEntry);

        timeEntryManager.deleteTimeEntry(createdEntry.getId());
        timeEntryManager.getTimeEntry(createdEntry.getId());
    }

    @Test
    public void testGetTimeEntriesForIssue() throws RedmineException {
        Issue issue = createIssues(issueManager, projectId, 1).get(0);
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
        TimeEntry entry = TimeEntryFactory.create();
        entry.setHours(hours);
        entry.setIssueId(issueId);
        entry.setActivityId(activityId);
        return timeEntryManager.createTimeEntry(entry);
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
        TimeEntry timeEntry = TimeEntryFactory.create();
        timeEntry.setActivityId(ACTIVITY_ID);
        timeEntry.setSpentOn(new Date());
        timeEntry.setHours(1.5f);
        return timeEntry;
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
        Issue createdIssue = createIssue(issueManager, projectId);

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
            issueManager.deleteIssue(createdIssueId);
        }
    }


}