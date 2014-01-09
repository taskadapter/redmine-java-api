package com.taskadapter.redmineapi;

import com.taskadapter.redmineapi.RedmineManager.INCLUDE;
import com.taskadapter.redmineapi.bean.Changeset;
import com.taskadapter.redmineapi.bean.CustomField;
import com.taskadapter.redmineapi.bean.Group;
import com.taskadapter.redmineapi.bean.Issue;
import com.taskadapter.redmineapi.bean.IssueCategory;
import com.taskadapter.redmineapi.bean.IssueRelation;
import com.taskadapter.redmineapi.bean.IssueStatus;
import com.taskadapter.redmineapi.bean.Journal;
import com.taskadapter.redmineapi.bean.JournalDetail;
import com.taskadapter.redmineapi.bean.Membership;
import com.taskadapter.redmineapi.bean.Project;
import com.taskadapter.redmineapi.bean.Role;
import com.taskadapter.redmineapi.bean.SavedQuery;
import com.taskadapter.redmineapi.bean.TimeEntry;
import com.taskadapter.redmineapi.bean.Tracker;
import com.taskadapter.redmineapi.bean.User;
import com.taskadapter.redmineapi.bean.Version;
import com.taskadapter.redmineapi.bean.Watcher;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Integration tests for Redmine Java API.
 * <p/>
 * This class and its dependencies are located in com.taskadapter.redmineapi
 * project.
 */
public class RedmineManagerTest {

    // TODO We don't know activities' IDs!
    // see feature request http://www.redmine.org/issues/7506
    private static final Integer ACTIVITY_ID = 8;

    private static final Logger logger = LoggerFactory
            .getLogger(RedmineManagerTest.class);

    private static RedmineManager mgr;

    private static String projectKey;
    private static TestConfig testConfig;

    @BeforeClass
    public static void oneTimeSetup() {
        testConfig = new TestConfig();
        mgr = IntegrationTestHelper.createRedmineManager();
        projectKey = IntegrationTestHelper.createProject(mgr);
    }

    @AfterClass
    public static void oneTimeTearDown() {
        IntegrationTestHelper.deleteProject(mgr, projectKey);
    }

    @Test
    public void issueCreated() {
        try {
            Issue issueToCreate = new Issue();
            issueToCreate.setSubject("test zzx");

            Calendar startCal = Calendar.getInstance();
            // have to clear them because they are ignored by Redmine and
            // prevent from comparison later
            startCal.clear(Calendar.HOUR_OF_DAY);
            startCal.clear(Calendar.MINUTE);
            startCal.clear(Calendar.SECOND);
            startCal.clear(Calendar.MILLISECOND);

            startCal.add(Calendar.DATE, 5);
            issueToCreate.setStartDate(startCal.getTime());

            Calendar due = Calendar.getInstance();
            due.add(Calendar.MONTH, 1);
            issueToCreate.setDueDate(due.getTime());
            User assignee = IntegrationTestHelper.getOurUser();
            issueToCreate.setAssignee(assignee);

            String description = "This is the description for the new task."
                    + "\nIt has several lines." + "\nThis is the last line.";
            issueToCreate.setDescription(description);

            float estimatedHours = 44;
            issueToCreate.setEstimatedHours(estimatedHours);

            Issue newIssue = mgr.createIssue(projectKey, issueToCreate);
            assertNotNull("Checking returned result", newIssue);
            assertNotNull("New issue must have some ID",
                    newIssue.getId());

            // check startDate
            Calendar returnedStartCal = Calendar.getInstance();
            returnedStartCal.setTime(newIssue.getStartDate());

            assertEquals(startCal.get(Calendar.YEAR),
                    returnedStartCal.get(Calendar.YEAR));
            assertEquals(startCal.get(Calendar.MONTH),
                    returnedStartCal.get(Calendar.MONTH));
            assertEquals(startCal.get(Calendar.DAY_OF_MONTH),
                    returnedStartCal.get(Calendar.DAY_OF_MONTH));

            // check dueDate
            Calendar returnedDueCal = Calendar.getInstance();
            returnedDueCal.setTime(newIssue.getDueDate());

            assertEquals(due.get(Calendar.YEAR),
                    returnedDueCal.get(Calendar.YEAR));
            assertEquals(due.get(Calendar.MONTH),
                    returnedDueCal.get(Calendar.MONTH));
            assertEquals(due.get(Calendar.DAY_OF_MONTH),
                    returnedDueCal.get(Calendar.DAY_OF_MONTH));

            // check ASSIGNEE
            User actualAssignee = newIssue.getAssignee();
            assertNotNull("Checking assignee not null", actualAssignee);
            assertEquals("Checking assignee id", assignee.getId(),
                    actualAssignee.getId());

            // check AUTHOR
            Integer EXPECTED_AUTHOR_ID = IntegrationTestHelper.getOurUser().getId();
            assertEquals(EXPECTED_AUTHOR_ID, newIssue.getAuthor()
                    .getId());

            // check ESTIMATED TIME
            assertEquals((Float) estimatedHours,
                    newIssue.getEstimatedHours());

            // check multi-line DESCRIPTION
            String regexpStripExtra = "\\r|\\n|\\s";
            description = description.replaceAll(regexpStripExtra, "");
            String actualDescription = newIssue.getDescription();
            actualDescription = actualDescription.replaceAll(regexpStripExtra,
                    "");
            assertEquals(description, actualDescription);

            // PRIORITY
            assertNotNull(newIssue.getPriorityId());
            assertTrue(newIssue.getPriorityId() > 0);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void issueWithParentCreated() {
        try {
            Issue parentIssue = new Issue();
            parentIssue.setSubject("parent 1");
            Issue newParentIssue = mgr.createIssue(projectKey, parentIssue);
            logger.debug("created parent: " + newParentIssue);

            assertNotNull("Checking parent was created", newParentIssue);
            assertNotNull("Checking ID of parent issue is not null",
                    newParentIssue.getId());

            // Integer parentId = 46;
            Integer parentId = newParentIssue.getId();

            Issue childIssue = new Issue();
            childIssue.setSubject("child 1");
            childIssue.setParentId(parentId);

            Issue newChildIssue = mgr.createIssue(projectKey, childIssue);
            logger.debug("created child: " + newChildIssue);

            assertEquals("Checking parent ID of the child issue",
                    parentId, newChildIssue.getParentId());

        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void nullStartDateIsPreserved() {
        try {
            Issue issue = new Issue();
            issue.setSubject("test start date");
            issue.setStartDate(null);

            Issue newIssue = mgr.createIssue(projectKey, issue);

            Issue loadedIssue = mgr.getIssueById(newIssue.getId());
            assertNull(loadedIssue.getStartDate());
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void testGetIssuesBySummary() {
        String summary = "issue with subject ABC";
        try {
            Issue issue = new Issue();
            issue.setSubject(summary);
            User assignee = IntegrationTestHelper.getOurUser();
            issue.setAssignee(assignee);

            Issue newIssue = mgr.createIssue(projectKey, issue);
            logger.debug("created: " + newIssue);
            assertNotNull("Checking returned result", newIssue);
            assertNotNull("New issue must have some ID",
                    newIssue.getId());

            // try to find the issue
            List<Issue> foundIssues = mgr.getIssuesBySummary(projectKey,
                    summary);

            assertNotNull("Checking if search results is not NULL",
                    foundIssues);
            assertTrue("Search results must be not empty",
                    !(foundIssues.isEmpty()));

            Issue loadedIssue1 = RedmineTestUtils.findIssueInList(foundIssues,
                    newIssue.getId());
            assertNotNull(loadedIssue1);
            assertEquals(summary, loadedIssue1.getSubject());
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void findByNonExistingSummaryReturnsEmptyList() {
        String summary = "some summary here for issue which does not exist";
        try {
            // try to find the issue
            List<Issue> foundIssues = mgr.getIssuesBySummary(projectKey,
                    summary);
            assertNotNull("Search result must be not null", foundIssues);
            assertTrue("Search result list must be empty",
                    foundIssues.isEmpty());
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @SuppressWarnings("unused")
    @Test(expected = IllegalArgumentException.class)
    public void testNULLHostParameter() {
        new RedmineManager(null);
    }

    @Test(expected = IllegalArgumentException.class)
    @SuppressWarnings("unused")
    public void testEmptyHostParameter() throws RuntimeException {
        new RedmineManager("");
    }

    @Test(expected = RedmineAuthenticationException.class)
    public void noAPIKeyOnCreateIssueThrowsAE() throws Exception {
        RedmineManager redmineMgrEmpty = new RedmineManager(testConfig.getURI());
        Issue issue = new Issue();
        issue.setSubject("test zzx");
        redmineMgrEmpty.createIssue(projectKey, issue);
    }

    @Test(expected = RedmineAuthenticationException.class)
    public void wrongAPIKeyOnCreateIssueThrowsAE() throws Exception {
        RedmineManager redmineMgrInvalidKey = new RedmineManager(
                testConfig.getURI(), "wrong_key");
        Issue issue = new Issue();
        issue.setSubject("test zzx");
        redmineMgrInvalidKey.createIssue(projectKey, issue);
    }

    @Test
    public void testUpdateIssue() {
        try {
            Issue issue = new Issue();
            String originalSubject = "Issue " + new Date();
            issue.setSubject(originalSubject);

            Issue newIssue = mgr.createIssue(projectKey, issue);
            String changedSubject = "changed subject";
            newIssue.setSubject(changedSubject);

            mgr.update(newIssue);

            Issue reloadedFromRedmineIssue = mgr.getIssueById(newIssue.getId());

            assertEquals(
                    "Checking if 'update issue' operation changed the 'subject' field",
                    changedSubject, reloadedFromRedmineIssue.getSubject());
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    /**
     * Tests the retrieval of an {@link Issue} by its ID.
     *
     * @throws com.taskadapter.redmineapi.RedmineException
     *                                        thrown in case something went wrong in Redmine
     * @throws IOException                    thrown in case something went wrong while performing I/O
     *                                        operations
     * @throws RedmineAuthenticationException thrown in case something went wrong while trying to login
     * @throws com.taskadapter.redmineapi.NotFoundException
     *                                        thrown in case the objects requested for could not be found
     */
    @Test
    public void testGetIssueById() throws RedmineException {
        Issue issue = new Issue();
        String originalSubject = "Issue " + new Date();
        issue.setSubject(originalSubject);

        Issue newIssue = mgr.createIssue(projectKey, issue);

        Issue reloadedFromRedmineIssue = mgr.getIssueById(newIssue.getId());

        assertEquals(
                "Checking if 'get issue by ID' operation returned issue with same 'subject' field",
                originalSubject, reloadedFromRedmineIssue.getSubject());
        Tracker tracker = reloadedFromRedmineIssue.getTracker();
        assertNotNull("Tracker of issue should not be null", tracker);
        assertNotNull("ID of tracker of issue should not be null",
                tracker.getId());
        assertNotNull("Name of tracker of issue should not be null",
                tracker.getName());
    }

    @Test
    public void testGetIssues() {
        try {
            // create at least 1 issue
            Issue issueToCreate = new Issue();
            issueToCreate.setSubject("testGetIssues: " + new Date());
            Issue newIssue = mgr.createIssue(projectKey, issueToCreate);

            List<Issue> issues = mgr.getIssues(projectKey, null);
            logger.debug("getIssues() loaded " + issues.size() + " issues");// using
            // query
            // #"
            // +
            // queryIdIssuesCreatedLast2Days);
            assertTrue(issues.size() > 0);
            boolean found = false;
            for (Issue issue : issues) {
                if (issue.getId().equals(newIssue.getId())) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                fail("getIssues() didn't return the issue we just created. The query "
                        + " must have returned all issues created during the last 2 days");
            }
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    @Test(expected = NotFoundException.class)
    public void testGetIssuesInvalidQueryId() throws RedmineException {
        Integer invalidQueryId = 9999999;
        mgr.getIssues(projectKey, invalidQueryId);
    }

    @Test
    public void testCreateIssueNonUnicodeSymbols() {
        try {
            String nonLatinSymbols = "Example with accents A��o";
            Issue toCreate = new Issue();
            toCreate.setSubject(nonLatinSymbols);
            Issue created = mgr.createIssue(projectKey, toCreate);
            assertEquals(nonLatinSymbols, created.getSubject());
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testCreateIssueSummaryOnly() {
        try {
            Issue issueToCreate = new Issue();
            issueToCreate.setSubject("This is the summary line 123");

            Issue newIssue = mgr.createIssue(projectKey, issueToCreate);
            assertNotNull("Checking returned result", newIssue);
            assertNotNull("New issue must have some ID",
                    newIssue.getId());

            // check AUTHOR
            Integer EXPECTED_AUTHOR_ID = IntegrationTestHelper.getOurUser().getId();
            assertEquals(EXPECTED_AUTHOR_ID, newIssue.getAuthor()
                    .getId());

        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test(expected = NotFoundException.class)
    public void testCreateIssueInvalidProjectKey() throws RedmineException {
        Issue issueToCreate = new Issue();
        issueToCreate.setSubject("Summary line 100");
        mgr.createIssue("someNotExistingProjectKey", issueToCreate);
    }

    @Test(expected = NotFoundException.class)
    public void testGetIssueNonExistingId() throws RedmineException {
        int someNonExistingID = 999999;
        mgr.getIssueById(someNonExistingID);
    }

    @Test(expected = NotFoundException.class)
    public void testUpdateIssueNonExistingId() throws RedmineException {
        int nonExistingId = 999999;
        Issue issue = new Issue();
        issue.setId(nonExistingId);
        mgr.update(issue);
    }

    @Test
    public void testGetIssuesPaging() {
        try {
            // create 27 issues. default page size is 25.
            createIssues(27);
            // mgr.setObjectsPerPage(5); <-- does not work now
            List<Issue> issues = mgr.getIssues(projectKey, null);
            logger.debug("testGetIssuesPaging() loaded " + issues.size()
                    + " issues");// using query #" +
            // queryIdIssuesCreatedLast2Days);
            assertTrue(issues.size() > 26);

            Set<Issue> issueSet = new HashSet<Issue>(issues);
            assertEquals(issues.size(), issueSet.size());
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    private List<Issue> createIssues(int issuesNumber) throws RedmineException {
        List<Issue> issues = new ArrayList<Issue>(issuesNumber);
        for (int i = 0; i < issuesNumber; i++) {
            Issue issueToCreate = new Issue();
            issueToCreate.setSubject("some issue " + i + " " + new Date());
            Issue issue = mgr.createIssue(projectKey, issueToCreate);
            issues.add(issue);
        }
        return issues;
    }

    private Issue generateRandomIssue() {
        Random r = new Random();
        Issue issue = new Issue();
        issue.setSubject("some issue " + r.nextInt() + " " + new Date());
        return issue;
    }

    @Test
    public void testGetTimeEntries() throws RedmineException {
        List<TimeEntry> list = mgr.getTimeEntries();
        assertNotNull(list);
    }

    @Test
    public void testCreateGetTimeEntry() throws RedmineException {
        Issue issue = createIssues(1).get(0);
        Integer issueId = issue.getId();

        TimeEntry entry = new TimeEntry();
        Float hours = 11f;
        entry.setHours(hours);
        entry.setIssueId(issueId);
        // TODO We don't know activities IDs!
        // see feature request http://www.redmine.org/issues/7506
        entry.setActivityId(ACTIVITY_ID);
        TimeEntry createdEntry = mgr.createTimeEntry(entry);

        assertNotNull(createdEntry);
        logger.debug("Created time entry " + createdEntry);
        assertEquals(hours, createdEntry.getHours());

        Float newHours = 22f;
        createdEntry.setHours(newHours);

        mgr.update(createdEntry);

        TimeEntry updatedEntry = mgr.getTimeEntry(createdEntry.getId());
        assertEquals(newHours, updatedEntry.getHours());
    }

    @Test(expected = NotFoundException.class)
    public void testCreateDeleteTimeEntry() throws RedmineException {
        Issue issue = createIssues(1).get(0);
        Integer issueId = issue.getId();

        TimeEntry entry = new TimeEntry();
        Float hours = 4f;
        entry.setHours(hours);
        entry.setIssueId(issueId);
        entry.setActivityId(ACTIVITY_ID);
        TimeEntry createdEntry = mgr.createTimeEntry(entry);
        assertNotNull(createdEntry);

        mgr.deleteTimeEntry(createdEntry.getId());
        mgr.getTimeEntry(createdEntry.getId());
    }

    @Test
    public void testGetTimeEntriesForIssue() throws RedmineException {
        Issue issue = createIssues(1).get(0);
        Integer issueId = issue.getId();
        Float hours1 = 2f;
        Float hours2 = 7f;
        Float totalHoursExpected = hours1 + hours2;
        TimeEntry createdEntry1 = createTimeEntry(issueId, hours1);
        TimeEntry createdEntry2 = createTimeEntry(issueId, hours2);
        assertNotNull(createdEntry1);
        assertNotNull(createdEntry2);

        List<TimeEntry> entries = mgr.getTimeEntriesForIssue(issueId);
        assertEquals(2, entries.size());
        Float totalTime = 0f;
        for (TimeEntry timeEntry : entries) {
            totalTime += timeEntry.getHours();
        }
        assertEquals(totalHoursExpected, totalTime);
    }

    private TimeEntry createTimeEntry(Integer issueId, float hours)
            throws RedmineException {
        TimeEntry entry = new TimeEntry();
        entry.setHours(hours);
        entry.setIssueId(issueId);
        entry.setActivityId(ACTIVITY_ID);
        return mgr.createTimeEntry(entry);
    }

    @Test(expected = NotFoundException.class)
    public void testDeleteIssue() throws RedmineException {
        Issue issue = createIssues(1).get(0);
        Issue retrievedIssue = mgr.getIssueById(issue.getId());
        assertEquals(issue, retrievedIssue);

        mgr.deleteIssue(issue.getId());
        mgr.getIssueById(issue.getId());
    }

    @Test
    public void testUpdateIssueSpecialXMLtags() throws Exception {
        Issue issue = createIssues(1).get(0);
        String newSubject = "\"text in quotes\" and <xml> tags";
        String newDescription = "<taghere>\"abc\"</here>";
        issue.setSubject(newSubject);
        issue.setDescription(newDescription);
        mgr.update(issue);

        Issue updatedIssue = mgr.getIssueById(issue.getId());
        assertEquals(newSubject, updatedIssue.getSubject());
        assertEquals(newDescription, updatedIssue.getDescription());
    }

    /**
     * The custom fields used here MUST ALREADY EXIST on the server and be
     * associated with the required task type (bug/feature/task/..).
     * <p/>
     * See feature request http://www.redmine.org/issues/9664
     */
    @Test
    public void testCustomFields() throws Exception {
        Issue issue = createIssues(1).get(0);
        // default empty values
        assertEquals(2, issue.getCustomFields().size());

        // TODO update this!
        int id1 = 1; // TODO this is pretty much a hack, we don't generally know
        // these ids!
        String custom1FieldName = "my_custom_1";
        String custom1Value = "some value 123";

        int id2 = 2;
        String custom2FieldName = "custom_boolean_1";
        String custom2Value = "true";

        issue.setCustomFields(new ArrayList<CustomField>());

        issue.getCustomFields().add(
                new CustomField(id1, custom1FieldName, custom1Value));
        issue.getCustomFields().add(
                new CustomField(id2, custom2FieldName, custom2Value));
        mgr.update(issue);

        Issue updatedIssue = mgr.getIssueById(issue.getId());
        assertEquals(2, updatedIssue.getCustomFields().size());
        assertEquals(custom1Value,
                updatedIssue.getCustomField(custom1FieldName));
        assertEquals(custom2Value,
                updatedIssue.getCustomField(custom2FieldName));
    }

    @Test
    public void testUpdateIssueDoesNotChangeEstimatedTime() {
        try {
            Issue issue = new Issue();
            String originalSubject = "Issue " + new Date();
            issue.setSubject(originalSubject);

            Issue newIssue = mgr.createIssue(projectKey, issue);
            assertEquals("Estimated hours must be NULL", null,
                    newIssue.getEstimatedHours());

            mgr.update(newIssue);

            Issue reloadedFromRedmineIssue = mgr.getIssueById(newIssue.getId());
            assertEquals("Estimated hours must be NULL", null,
                    reloadedFromRedmineIssue.getEstimatedHours());
        } catch (Exception e) {
            fail();
        }
    }

    /**
     * Tests the correct retrieval of the parent id of sub {@link Project}.
     *
     * @throws RedmineProcessingException     thrown in case something went wrong in Redmine
     * @throws IOException                    thrown in case something went wrong while performing I/O
     *                                        operations
     * @throws RedmineAuthenticationException thrown in case something went wrong while trying to login
     * @throws NotFoundException              thrown in case the objects requested for could not be found
     */
    @Test
    public void testSubProjectIsCreatedWithCorrectParentId()
            throws RedmineException {
        Project createdMainProject = null;
        try {
            createdMainProject = createProject();
            Project subProject = createSubProject(createdMainProject);
            assertEquals("Must have correct parent ID",
                    createdMainProject.getId(), subProject.getParentId());
        } finally {
            if (createdMainProject != null) {
                mgr.deleteProject(createdMainProject.getIdentifier());
            }
        }
    }

    private Project createProject() throws RedmineException {
        Project mainProject = new Project();
        long id = new Date().getTime();
        mainProject.setName("project" + id);
        mainProject.setIdentifier("project" + id);
        return mgr.createProject(mainProject);
    }

    private Project createSubProject(Project parent) throws RedmineException {
        Project project = new Project();
        long id = new Date().getTime();
        project.setName("sub_pr" + id);
        project.setIdentifier("subpr" + id);
        project.setParentId(parent.getId());
        return mgr.createProject(project);
    }

    @Test
    public void testIssueDoneRatio() {
        try {
            Issue issue = new Issue();
            String subject = "Issue " + new Date();
            issue.setSubject(subject);

            Issue createdIssue = mgr.createIssue(projectKey, issue);
            assertEquals("Initial 'done ratio' must be 0", (Integer) 0,
                    createdIssue.getDoneRatio());
            Integer doneRatio = 50;
            createdIssue.setDoneRatio(doneRatio);
            mgr.update(createdIssue);

            Integer issueId = createdIssue.getId();
            Issue reloadedFromRedmineIssue = mgr.getIssueById(issueId);
            assertEquals(
                    "Checking if 'update issue' operation changed 'done ratio' field",
                    doneRatio, reloadedFromRedmineIssue.getDoneRatio());

            Integer invalidDoneRatio = 130;
            reloadedFromRedmineIssue.setDoneRatio(invalidDoneRatio);
            try {
                mgr.update(reloadedFromRedmineIssue);
            } catch (RedmineProcessingException e) {
                assertEquals("Must be 1 error", 1, e.getErrors().size());
                assertEquals("Checking error text",
                        "% Done is not included in the list", e.getErrors()
                        .get(0));
            }

            Issue reloadedFromRedmineIssueUnchanged = mgr.getIssueById(issueId);
            assertEquals(
                    "'done ratio' must have remained unchanged after invalid value",
                    doneRatio, reloadedFromRedmineIssueUnchanged.getDoneRatio());
        } catch (Exception e) {
            fail(e.toString());
        }
    }

    @Test
    public void testIssueNullDescriptionDoesNotEraseIt() {
        try {
            Issue issue = new Issue();
            String subject = "Issue " + new Date();
            String descr = "Some description";
            issue.setSubject(subject);
            issue.setDescription(descr);

            Issue createdIssue = mgr.createIssue(projectKey, issue);
            assertEquals("Checking description", descr,
                    createdIssue.getDescription());

            createdIssue.setDescription(null);
            mgr.update(createdIssue);

            Integer issueId = createdIssue.getId();
            Issue reloadedFromRedmineIssue = mgr.getIssueById(issueId);
            assertEquals("Description must not be erased", descr,
                    reloadedFromRedmineIssue.getDescription());

            reloadedFromRedmineIssue.setDescription("");
            mgr.update(reloadedFromRedmineIssue);

            Issue reloadedFromRedmineIssueUnchanged = mgr.getIssueById(issueId);
            assertEquals("Description must be erased", "",
                    reloadedFromRedmineIssueUnchanged.getDescription());
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void testIssueJournals() {
        try {
            // create at least 1 issue
            Issue issueToCreate = new Issue();
            issueToCreate.setSubject("testGetIssues: " + new Date());
            Issue newIssue = mgr.createIssue(projectKey, issueToCreate);

            Issue loadedIssueWithJournals = mgr.getIssueById(newIssue.getId(),
                    INCLUDE.journals);
            assertTrue(loadedIssueWithJournals.getJournals().isEmpty());

            String commentDescribingTheUpdate = "some comment describing the issue update";
            loadedIssueWithJournals.setSubject("new subject");
            loadedIssueWithJournals.setNotes(commentDescribingTheUpdate);
            mgr.update(loadedIssueWithJournals);

            Issue loadedIssueWithJournals2 = mgr.getIssueById(newIssue.getId(),
                    INCLUDE.journals);
            assertEquals(1, loadedIssueWithJournals2.getJournals()
                    .size());

            Journal journalItem = loadedIssueWithJournals2.getJournals().get(0);
            assertEquals(commentDescribingTheUpdate,
                    journalItem.getNotes());
            User ourUser = IntegrationTestHelper.getOurUser();
            // can't compare User objects because either of them is not
            // completely filled
            assertEquals(ourUser.getId(), journalItem.getUser().getId());
            assertEquals(ourUser.getFirstName(), journalItem.getUser()
                    .getFirstName());
            assertEquals(ourUser.getLastName(), journalItem.getUser()
                    .getLastName());
            assertEquals(1, journalItem.getDetails().size());
            final JournalDetail journalDetail = journalItem.getDetails().get(0);
            assertEquals("new subject", journalDetail.getNewValue());
            assertEquals("subject", journalDetail.getName());
            assertEquals("attr", journalDetail.getProperty());

            Issue loadedIssueWithoutJournals = mgr.getIssueById(newIssue
                    .getId());
            assertTrue(loadedIssueWithoutJournals.getJournals().isEmpty());

        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    @Test
    public void testCreateRelation() {
        try {
            List<Issue> issues = createIssues(2);
            Issue src = issues.get(0);
            Issue target = issues.get(1);

            String relationText = IssueRelation.TYPE.precedes.toString();
            IssueRelation r = mgr.createRelation(src.getId(), target.getId(),
                    relationText);
            assertEquals(src.getId(), r.getIssueId());
            assertEquals(target.getId(), r.getIssueToId());
            assertEquals(relationText, r.getType());
        } catch (Exception e) {
            fail(e.toString());
        }
    }

    private IssueRelation createTwoRelatedIssues() throws RedmineException {
        List<Issue> issues = createIssues(2);
        Issue src = issues.get(0);
        Issue target = issues.get(1);

        String relationText = IssueRelation.TYPE.precedes.toString();
        return mgr.createRelation(src.getId(), target.getId(), relationText);
    }

    @Test
    public void issueRelationsAreCreatedAndLoadedOK() {
        try {
            IssueRelation relation = createTwoRelatedIssues();
            Issue issue = mgr.getIssueById(relation.getIssueId(),
                    INCLUDE.relations);
            Issue issueTarget = mgr.getIssueById(relation.getIssueToId(),
                    INCLUDE.relations);

            assertEquals(1, issue.getRelations().size());
            assertEquals(1, issueTarget.getRelations().size());

            IssueRelation relation1 = issue.getRelations().get(0);
            assertEquals(issue.getId(), relation1.getIssueId());
            assertEquals(issueTarget.getId(), relation1.getIssueToId());
            assertEquals("precedes", relation1.getType());
            assertEquals((Integer) 0, relation1.getDelay());

            IssueRelation reverseRelation = issueTarget.getRelations().get(0);
            // both forward and reverse relations are the same!
            assertEquals(relation1, reverseRelation);
        } catch (Exception e) {
            fail(e.toString());
        }
    }

    @Test
    public void testIssureRelationDelete() throws RedmineException {
        IssueRelation relation = createTwoRelatedIssues();

        mgr.deleteRelation(relation.getId());
        Issue issue = mgr
                .getIssueById(relation.getIssueId(), INCLUDE.relations);
        assertEquals(0, issue.getRelations().size());
    }

    @Test
    public void testIssueRelationsDelete() throws RedmineException {
        List<Issue> issues = createIssues(3);
        Issue src = issues.get(0);
        Issue target = issues.get(1);
        String relationText = IssueRelation.TYPE.precedes.toString();

        mgr.createRelation(src.getId(), target.getId(), relationText);

        target = issues.get(2);
        mgr.createRelation(src.getId(), target.getId(), relationText);

        src = mgr.getIssueById(src.getId(), INCLUDE.relations);
        mgr.deleteIssueRelations(src);

        Issue issue = mgr.getIssueById(src.getId(), INCLUDE.relations);
        assertEquals(0, issue.getRelations().size());
    }

    /**
     * this test is ignored because: 1) we can't create Versions. see
     * http://www.redmine.org/issues/9088 2) we don't currently set versions
     * when creating issues.
     * <p/>
     * 3) setting "fixed_version_id" does not work on creating issue.
     */
    @Ignore
    @Test
    public void issueFixVersionIsSet() throws Exception {

        String existingProjectKey = "test";
        Issue toCreate = generateRandomIssue();
        Version v = new Version();
        String versionName = "1.0";
        v.setName("1.0");
        v.setId(1);
        v.setProject(mgr.getProjectByKey(projectKey));
        v = mgr.createVersion(v);
        toCreate.setTargetVersion(v);
        Issue createdIssue = mgr.createIssue(existingProjectKey, toCreate);

        assertNotNull(createdIssue.getTargetVersion());
        assertEquals(createdIssue.getTargetVersion().getName(),
                versionName);
    }

    /**
     * Not supported by Redmine REST API.
     */
    @Ignore
    @Test
    public void testSpentTimeFieldLoaded() {
        try {
            Issue issue = new Issue();
            String subject = "Issue " + new Date();
            issue.setSubject(subject);
            float spentHours = 2;
            issue.setSpentHours(spentHours);

            Issue createdIssue = mgr.createIssue(projectKey, issue);
            Issue newIssue = mgr.getIssueById(createdIssue.getId());
            assertEquals((Float) spentHours, newIssue.getSpentHours());
        } catch (Exception e) {
            fail();
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidTimeEntryFailsWithIAEOnCreate() throws RedmineException {
        mgr.createTimeEntry(createIncompleteTimeEntry());
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidTimeEntryFailsWithIAEOnUpdate() throws RedmineException {
        mgr.update(createIncompleteTimeEntry());
    }

    private TimeEntry createIncompleteTimeEntry() {
        TimeEntry timeEntry = new TimeEntry();
        timeEntry.setActivityId(ACTIVITY_ID);
        timeEntry.setSpentOn(new Date());
        timeEntry.setHours(1.5f);
        return timeEntry;
    }

    @Test
    public void testViolateTimeEntryConstraint_ProjectOrIssueID_issue66()
            throws RedmineException {
        TimeEntry timeEntry = createIncompleteTimeEntry();
        // Now can try to verify with project ID (only test with issue ID seems
        // to be already covered)
        int projectId = mgr.getProjects().get(0).getId();
        timeEntry.setProjectId(projectId);
        try {
            TimeEntry created = mgr.createTimeEntry(timeEntry);
            logger.debug("Created time entry " + created);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Unexpected " + e.getClass().getSimpleName() + ": "
                    + e.getMessage());
        }
    }

    /**
     * tests the retrieval of statuses.
     *
     * @throws RedmineProcessingException     thrown in case something went wrong in Redmine
     * @throws IOException                    thrown in case something went wrong while performing I/O
     *                                        operations
     * @throws RedmineAuthenticationException thrown in case something went wrong while trying to login
     * @throws NotFoundException              thrown in case the objects requested for could not be found
     */
    @Test
    public void testGetStatuses() throws RedmineException {
        // TODO we should create some statuses first, but the Redmine Java API
        // does not support this presently
        List<IssueStatus> statuses = mgr.getStatuses();
        assertFalse("Expected list of statuses not to be empty",
                statuses.isEmpty());
        for (IssueStatus issueStatus : statuses) {
            // asserts on status
            assertNotNull("ID of status must not be null", issueStatus.getId());
            assertNotNull("Name of status must not be null",
                    issueStatus.getName());
        }
    }

    /**
     * tests the creation of an invalid {@link Version}.
     *
     * @throws RedmineException               thrown in case something went wrong in Redmine
     * @throws RedmineAuthenticationException thrown in case something went wrong while trying to login
     * @throws NotFoundException              thrown in case the objects requested for could not be found
     */
    @Test(expected = IllegalArgumentException.class)
    public void testCreateInvalidVersion() throws RedmineException {
        Version version = new Version(null, "Invalid test version "
                + UUID.randomUUID().toString());
        mgr.createVersion(version);
    }

    /**
     * tests the deletion of an invalid {@link Version}. Expects a
     * {@link NotFoundException} to be thrown.
     *
     * @throws RedmineException               thrown in case something went wrong in Redmine
     * @throws RedmineAuthenticationException thrown in case something went wrong while trying to login
     * @throws NotFoundException              thrown in case the objects requested for could not be found
     */
    @Test(expected = NotFoundException.class)
    public void testDeleteInvalidVersion() throws RedmineException {
        // create new test version
        Version version = new Version(null, "Invalid test version "
                + UUID.randomUUID().toString());
        version.setDescription("An invalid test version created by "
                + this.getClass());
        // set invalid id
        version.setId(-1);
        // now try to delete version
        mgr.deleteVersion(version);
    }

    /**
     * tests the deletion of a {@link Version}.
     *
     * @throws RedmineException               thrown in case something went wrong in Redmine
     * @throws RedmineAuthenticationException thrown in case something went wrong while trying to login
     * @throws NotFoundException              thrown in case the objects requested for could not be found
     */
    @Test
    public void testDeleteVersion() throws RedmineException {
        Project project = createProject();
        try {
            String name = "Test version " + UUID.randomUUID().toString();
            Version version = new Version(project, name);
            version.setDescription("A test version created by " + this.getClass());
            version.setStatus("open");
            Version newVersion = mgr.createVersion(version);
            assertEquals("checking version name", name, newVersion.getName());

            mgr.deleteVersion(newVersion);
            List<Version> versions = mgr.getVersions(project.getId());
            assertTrue("List of versions of test project must be empty now but is "
                    + versions, versions.isEmpty());
        } finally {
            mgr.deleteProject(project.getIdentifier());
        }
    }

    /**
     * tests the retrieval of {@link Version}s.
     *
     * @throws RedmineException               thrown in case something went wrong in Redmine
     * @throws RedmineAuthenticationException thrown in case something went wrong while trying to login
     * @throws NotFoundException              thrown in case the objects requested for could not be found
     */
    @Test
    public void testGetVersions() throws RedmineException {
        Project project = createProject();
        Version testVersion1 = null;
        Version testVersion2 = null;
        try {
            testVersion1 = mgr.createVersion(new Version(project, "Version" + UUID.randomUUID()));
            testVersion2 = mgr.createVersion(new Version(project, "Version" + UUID.randomUUID()));
            List<Version> versions = mgr.getVersions(project.getId());
            assertEquals("Wrong number of versions for project "
                    + project.getName() + " delivered by Redmine Java API", 2,
                    versions.size());
            for (Version version : versions) {
                // assert version
                assertNotNull("ID of version must not be null", version.getId());
                assertNotNull("Name of version must not be null", version.getName());
                assertNotNull("Project of version must not be null", version.getProject());
            }
        } finally {
            if (testVersion1 != null) {
                mgr.deleteVersion(testVersion1);
            }
            if (testVersion2 != null) {
                mgr.deleteVersion(testVersion2);
            }
            mgr.deleteProject(project.getIdentifier());
        }
    }

    @Test
    public void versionIsRetrievedById() throws RedmineException {
        Project project = mgr.getProjectByKey(projectKey);
        Version createdVersion = mgr.createVersion(new Version(project,
                "Version_1_" + UUID.randomUUID()));
        Version versionById = mgr.getVersionById(createdVersion.getId());
        assertEquals(createdVersion, versionById);
    }

    @Test
    public void versionIsUpdated() throws RedmineException {
        Project project = mgr.getProjectByKey(projectKey);
        Version createdVersion = mgr.createVersion(new Version(project,
                "Version_1_" + UUID.randomUUID()));
        String description = "new description";
        createdVersion.setDescription(description);
        mgr.update(createdVersion);
        Version versionById = mgr.getVersionById(createdVersion.getId());
        assertEquals(description, versionById.getDescription());
    }

    @Test
    public void versionIsUpdatedIncludingDueDate() throws RedmineException {
        Project project = mgr.getProjectByKey(projectKey);
        Version createdVersion = mgr.createVersion(new Version(project,
                "Version_1_" + UUID.randomUUID()));
        String description = "new description";
        createdVersion.setDescription(description);
        createdVersion.setDueDate(new Date());
        mgr.update(createdVersion);
        Version versionById = mgr.getVersionById(createdVersion.getId());
        assertEquals(description, versionById.getDescription());
    }

    /**
     * tests the creation and deletion of a {@link IssueCategory}.
     *
     * @throws RedmineException               thrown in case something went wrong in Redmine
     * @throws IOException                    thrown in case something went wrong while performing I/O
     *                                        operations
     * @throws RedmineAuthenticationException thrown in case something went wrong while trying to login
     * @throws NotFoundException              thrown in case the objects requested for could not be found
     */
    @Test
    public void testCreateAndDeleteIssueCategory() throws RedmineException {
        Project project = mgr.getProjectByKey(projectKey);
        IssueCategory category = new IssueCategory(project, "Category" + new Date().getTime());
        category.setAssignee(IntegrationTestHelper.getOurUser());
        IssueCategory newIssueCategory = mgr.createCategory(category);
        assertNotNull("Expected new category not to be null", newIssueCategory);
        assertNotNull("Expected project of new category not to be null", newIssueCategory.getProject());
        assertNotNull("Expected assignee of new category not to be null",
                newIssueCategory.getAssignee());
        // now delete category
        mgr.deleteCategory(newIssueCategory);
        // assert that the category is gone
        List<IssueCategory> categories = mgr.getCategories(project.getId());
        assertTrue(
                "List of categories of test project must be empty now but is "
                        + categories, categories.isEmpty());
    }

    /**
     * tests the retrieval of {@link IssueCategory}s.
     *
     * @throws RedmineException               thrown in case something went wrong in Redmine
     * @throws IOException                    thrown in case something went wrong while performing I/O
     *                                        operations
     * @throws RedmineAuthenticationException thrown in case something went wrong while trying to login
     * @throws NotFoundException              thrown in case the objects requested for could not be found
     */
    @Test
    public void testGetIssueCategories() throws RedmineException {
        Project project = mgr.getProjectByKey(projectKey);
        // create some categories
        IssueCategory testIssueCategory1 = new IssueCategory(project,
                "Category" + new Date().getTime());
        testIssueCategory1.setAssignee(IntegrationTestHelper.getOurUser());
        IssueCategory newIssueCategory1 = mgr
                .createCategory(testIssueCategory1);
        IssueCategory testIssueCategory2 = new IssueCategory(project,
                "Category" + new Date().getTime());
        testIssueCategory2.setAssignee(IntegrationTestHelper.getOurUser());
        IssueCategory newIssueCategory2 = mgr
                .createCategory(testIssueCategory2);
        try {
            List<IssueCategory> categories = mgr.getCategories(project.getId());
            assertEquals("Wrong number of categories for project "
                    + project.getName() + " delivered by Redmine Java API", 2,
                    categories.size());
            for (IssueCategory category : categories) {
                // assert category
                assertNotNull("ID of category must not be null",
                        category.getId());
                assertNotNull("Name of category must not be null",
                        category.getName());
                assertNotNull("Project of category must not be null",
                        category.getProject());
                assertNotNull("Assignee of category must not be null",
                        category.getAssignee());
            }
        } finally {
            // scrub test categories
            if (newIssueCategory1 != null) {
                mgr.deleteCategory(newIssueCategory1);
            }
            if (newIssueCategory2 != null) {
                mgr.deleteCategory(newIssueCategory2);
            }
        }
    }

    /**
     * tests the creation of an invalid {@link IssueCategory}.
     *
     * @throws RedmineException               thrown in case something went wrong in Redmine
     * @throws IOException                    thrown in case something went wrong while performing I/O
     *                                        operations
     * @throws RedmineAuthenticationException thrown in case something went wrong while trying to login
     * @throws NotFoundException              thrown in case the objects requested for could not be found
     */
    @Test(expected = IllegalArgumentException.class)
    public void testCreateInvalidIssueCategory() throws RedmineException {
        IssueCategory category = new IssueCategory(null, "InvalidCategory"
                + new Date().getTime());
        mgr.createCategory(category);
    }

    /**
     * tests the deletion of an invalid {@link IssueCategory}. Expects a
     * {@link NotFoundException} to be thrown.
     *
     * @throws RedmineException               thrown in case something went wrong in Redmine
     * @throws IOException                    thrown in case something went wrong while performing I/O
     *                                        operations
     * @throws RedmineAuthenticationException thrown in case something went wrong while trying to login
     * @throws NotFoundException              thrown in case the objects requested for could not be found
     */
    @Test(expected = NotFoundException.class)
    public void testDeleteInvalidIssueCategory() throws RedmineException {
        // create new test category
        IssueCategory category = new IssueCategory(null, "InvalidCategory"
                + new Date().getTime());
        // set invalid id
        category.setId(-1);
        // now try to delete category
        mgr.deleteCategory(category);
    }

    /**
     * Tests the retrieval of {@link Tracker}s.
     *
     * @throws RedmineException               thrown in case something went wrong in Redmine
     * @throws IOException                    thrown in case something went wrong while performing I/O
     *                                        operations
     * @throws RedmineAuthenticationException thrown in case something went wrong while trying to login
     * @throws NotFoundException              thrown in case the objects requested for could not be found
     */
    @Test
    public void testGetTrackers() throws RedmineException {
        List<Tracker> trackers = mgr.getTrackers();
        assertNotNull("List of trackers returned should not be null", trackers);
        assertFalse("List of trackers returned should not be empty",
                trackers.isEmpty());
        for (Tracker tracker : trackers) {
            assertNotNull("Tracker returned should not be null", tracker);
            assertNotNull("ID of tracker returned should not be null",
                    tracker.getId());
            assertNotNull("Name of tracker returned should not be null",
                    tracker.getName());
        }
    }

    /**
     * Tests the creation and retrieval of an
     * {@link com.taskadapter.redmineapi.bean.Issue} with a
     * {@link IssueCategory}.
     *
     * @throws RedmineException               thrown in case something went wrong in Redmine
     * @throws IOException                    thrown in case something went wrong while performing I/O
     *                                        operations
     * @throws RedmineAuthenticationException thrown in case something went wrong while trying to login
     * @throws NotFoundException              thrown in case the objects requested for could not be found
     */
    @Test
    public void testCreateAndGetIssueWithCategory() throws RedmineException {
        IssueCategory newIssueCategory = null;
        Issue newIssue = null;
        try {
            Project project = mgr.getProjectByKey(projectKey);
            // create an issue category
            IssueCategory category = new IssueCategory(project, "Category_"
                    + new Date().getTime());
            category.setAssignee(IntegrationTestHelper.getOurUser());
            newIssueCategory = mgr.createCategory(category);
            // create an issue
            Issue issueToCreate = new Issue();
            issueToCreate.setSubject("getIssueWithCategory_" + UUID.randomUUID());
            issueToCreate.setCategory(newIssueCategory);
            newIssue = mgr.createIssue(projectKey, issueToCreate);
            // retrieve issue
            Issue retrievedIssue = mgr.getIssueById(newIssue.getId());
            // assert retrieved category of issue
            IssueCategory retrievedCategory = retrievedIssue.getCategory();
            assertNotNull("Category retrieved for issue " + newIssue.getId()
                    + " should not be null", retrievedCategory);
            assertEquals("ID of category retrieved for issue "
                    + newIssue.getId() + " is wrong", newIssueCategory.getId(),
                    retrievedCategory.getId());
            assertEquals("Name of category retrieved for issue "
                    + newIssue.getId() + " is wrong",
                    newIssueCategory.getName(), retrievedCategory.getName());
        } finally {
            if (newIssue != null) {
                mgr.deleteIssue(newIssue.getId());
            }
            if (newIssueCategory != null) {
                mgr.deleteCategory(newIssueCategory);
            }
        }
    }

    @Test
    public void getNewsDoesNotFailForNULLProject() throws RedmineException {
        mgr.getNews(null);
    }

    @Test
    public void getNewsDoesNotFailForTempProject() throws RedmineException {
        mgr.getNews(projectKey);
    }

    @Test
    public void getSavedQueriesDoesNotFailForTempProject()
            throws RedmineException {
        mgr.getSavedQueries(projectKey);
    }

    @Test
    public void getSavedQueriesDoesNotFailForNULLProject()
            throws RedmineException {
        mgr.getSavedQueries(null);
    }

    @Test
    public void emptyDescriptionReturnedAsEmptyString() throws RedmineException {
        Issue issue = new Issue();
        String subject = "Issue " + new Date();
        issue.setSubject(subject);

        Issue createdIssue = mgr.createIssue(projectKey, issue);
        assertEquals("Description must be an empty string, not NULL", "",
                createdIssue.getDescription());
    }

    @Test
    public void updateIssueDescription() throws RedmineException {
        Issue issue = new Issue();
        issue.setSubject("test123");
        final Issue iss1 = mgr.createIssue(projectKey, issue);
        final Issue iss2 = new Issue();
        iss2.setId(iss1.getId());
        iss2.setDescription("This is a test");
        mgr.update(iss2);
        final Issue iss3 = mgr.getIssueById(iss2.getId());
        assertEquals("test123", iss3.getSubject());
        assertEquals("This is a test", iss3.getDescription());
    }

    @Test
    public void updateIssueTitle() throws RedmineException {
        Issue issue = new Issue();
        issue.setSubject("test123");
        issue.setDescription("Original description");
        final Issue iss1 = mgr.createIssue(projectKey, issue);
        final Issue iss2 = new Issue();
        iss2.setId(iss1.getId());
        iss2.setSubject("New subject");
        mgr.update(iss2);
        final Issue iss3 = mgr.getIssueById(iss2.getId());
        assertEquals("New subject", iss3.getSubject());
        assertEquals("Original description", iss3.getDescription());
    }

    @Test
    public void testGetRoles() throws RedmineException {
        assertTrue(mgr.getRoles().size() > 0);
    }

    @Test
    public void testGetMemberships() throws RedmineException {
        final List<Membership> result = mgr.getMemberships(projectKey);
        assertNotNull(result);
    }

    @Test
    public void testMemberships() throws RedmineException {
        final List<Role> roles = mgr.getRoles();

        final Membership newMembership = new Membership();
        final Project project = new Project();
        project.setIdentifier(projectKey);
        newMembership.setProject(project);
        final User currentUser = mgr.getCurrentUser();
        newMembership.setUser(currentUser);
        newMembership.setRoles(roles);

        mgr.addMembership(newMembership);
        final List<Membership> memberships1 = mgr.getMemberships(project);
        assertEquals(1, memberships1.size());
        final Membership createdMembership = memberships1.get(0);
        assertEquals(currentUser.getId(), createdMembership.getUser()
                .getId());
        assertEquals(roles.size(), createdMembership.getRoles().size());

        final Membership membershipById = mgr.getMembership(createdMembership
                .getId());
        assertEquals(createdMembership, membershipById);

        final Membership emptyMembership = new Membership();
        emptyMembership.setId(createdMembership.getId());
        emptyMembership.setProject(createdMembership.getProject());
        emptyMembership.setUser(createdMembership.getUser());
        emptyMembership.setRoles(Collections.singletonList(roles.get(0)));

        mgr.update(emptyMembership);
        final Membership updatedEmptyMembership = mgr
                .getMembership(createdMembership.getId());

        assertEquals(1, updatedEmptyMembership.getRoles().size());
        mgr.delete(updatedEmptyMembership);
    }

    @Test
    public void testUserMemberships() throws RedmineException {
        final List<Role> roles = mgr.getRoles();
        final Membership newMembership = new Membership();
        final Project project = new Project();
        project.setIdentifier(projectKey);
        newMembership.setProject(project);
        final User currentUser = mgr.getCurrentUser();
        newMembership.setUser(currentUser);
        newMembership.setRoles(roles);

        mgr.addMembership(newMembership);

        final User userWithMembership = mgr.getUserById(currentUser.getId());
        assertTrue(userWithMembership.getMemberships().size() > 0);
    }

    @Test
    public void testUnknownHostException() throws RedmineException, IOException {
        final RedmineManager mgr1 = new RedmineManager(
                "http://The.unknown.host");
        try {
            mgr1.getProjects();
        } catch (RedmineTransportException e1) {
            assertTrue(e1.getMessage().startsWith(
                    "Cannot fetch data from http://The.unknown.host/"));
            assertTrue(e1.getCause() instanceof UnknownHostException);
        }
    }

    @Ignore
    @Test
    public void testChangesets() throws RedmineException {
        final Issue issue = mgr.getIssueById(89, INCLUDE.changesets);
        assertEquals(2, issue.getChangesets().size());
        final Changeset firstChange = issue.getChangesets().get(0);
        assertNotNull(firstChange.getComments());
    }

    @Ignore("This test requires a specific project configuration")
    @Test
    public void testSavedQueries() throws RedmineException {
        final Collection<SavedQuery> queries = mgr.getSavedQueries("test");
        assertTrue(queries.size() > 0);
    }

    /**
     * Requires Redmine 2.1
     */
    @Test
    public void testAddUserToGroup() throws RedmineException {
        final Group template = new Group();
        template.setName("testAddUserToGroup " + System.currentTimeMillis());
        final Group group = mgr.createGroup(template);
        try {
            final User newUser = mgr.createUser(UserGenerator.generateRandomUser());
            try {
                mgr.addUserToGroup(newUser, group);
                final List<Group> userGroups = mgr.getUserById(newUser.getId()).getGroups();
                assertTrue(userGroups.size() == 1);
                assertTrue(group.getName().equals(userGroups.get(0).getName()));
            } finally {
                mgr.deleteUser(newUser.getId());
            }
        } finally {
            mgr.deleteGroup(group);
        }
    }

    /**
     * Requires Redmine 2.1
     */
    @Test
    public void addingUserToGroupTwiceDoesNotGiveErrors() throws RedmineException {
        final Group template = new Group();
        template.setName("some test " + System.currentTimeMillis());
        final Group group = mgr.createGroup(template);
        try {
            final User newUser = mgr.createUser(UserGenerator.generateRandomUser());
            try {
                mgr.addUserToGroup(newUser, group);
                mgr.addUserToGroup(newUser, group);
                assertTrue(mgr.getUserById(newUser.getId()).getGroups().size() == 1);
            } finally {
                mgr.deleteUser(newUser.getId());
            }
        } finally {
            mgr.deleteGroup(group);
        }
    }

    @Test
    public void testGroupCRUD() throws RedmineException {
        final Group template = new Group();
        template.setName("Template group " + System.currentTimeMillis());
        final Group created = mgr.createGroup(template);

        try {
            assertEquals(template.getName(), created.getName());
            final Group loaded = mgr.getGroupById(created.getId());
            assertEquals(template.getName(), loaded.getName());

            final Group update = new Group();
            update.setId(loaded.getId());
            update.setName("Group update " + System.currentTimeMillis());

            mgr.update(update);

            final Group loaded2 = mgr.getGroupById(created.getId());
            assertEquals(update.getName(), loaded2.getName());
        } finally {
            mgr.deleteGroup(created);
        }

        try {
            mgr.getGroupById(created.getId());
            fail("Group should be deleted but was found");
        } catch (NotFoundException e) {
            // OK!
        }
    }

    @Test
    public void testGetRoleById() throws RedmineException {
        final Collection<Role> roles = mgr.getRoles();
        for (Role r : roles) {
            final Role loaded = mgr.getRoleById(r.getId());
            assertEquals(r.getName(), loaded.getName());
            assertEquals(r.getInherited(), loaded.getInherited());
        }
    }

    @Test
    public void testRolesHasPermissions() throws RedmineException {
        final Collection<Role> roles = mgr.getRoles();
        for (Role r : roles) {
            final Role loaded = mgr.getRoleById(r.getId());
            if (loaded.getPermissions() != null && !loaded.getPermissions().isEmpty())
                return;

        }
        fail("Failed to find a role with a permissions");
    }

    /**
     * Test for issue 64 (time entry format)
     */
    @Test
    public void testTimeEntryComments() throws RedmineException {
        Issue issue = createIssues(1).get(0);
        Integer issueId = issue.getId();

        TimeEntry entry = new TimeEntry();
        Float hours = 11f;
        entry.setHours(hours);
        entry.setIssueId(issueId);
        final String comment = "This is a comment although it may not look like it";
        entry.setComment(comment);
        // TODO We don't know activities IDs!
        // see feature request http://www.redmine.org/issues/7506
        entry.setActivityId(ACTIVITY_ID);
        TimeEntry createdEntry = mgr.createTimeEntry(entry);

        assertNotNull(createdEntry);
        assertEquals(comment, createdEntry.getComment());

        createdEntry.setComment("New comment");
        mgr.update(createdEntry);
        final TimeEntry updatedEntry = mgr.getTimeEntry(createdEntry.getId());
        assertEquals("New comment", updatedEntry.getComment());
    }

    @Test
    public void testIssuePriorities() throws RedmineException {
        assertTrue(mgr.getIssuePriorities().size() > 0);
    }

    @Test
    public void testTimeEntryActivities() throws RedmineException {
        assertTrue(mgr.getTimeEntryActivities().size() > 0);
    }

    /**
     * Requires Redmine 2.3
     */
    @Test
    public void testAddIssueWatcher() throws RedmineException {
        final Issue issue = createIssues(1).get(0);
        final Issue retrievedIssue = mgr.getIssueById(issue.getId());
        assertEquals(issue, retrievedIssue);

        final User newUser = mgr.createUser(UserGenerator.generateRandomUser());
        try {
            Watcher watcher = new Watcher(newUser.getId(), null);
            mgr.addWatcherToIssue(watcher, issue);
        } finally {
            mgr.deleteUser(newUser.getId());
        }

        mgr.getIssueById(issue.getId());
    }

    /**
     * Requires Redmine 2.3
     */
    @Test
    public void testDeleteIssueWatcher() throws RedmineException {
        final Issue issue = createIssues(1).get(0);
        final Issue retrievedIssue = mgr.getIssueById(issue.getId());
        assertEquals(issue, retrievedIssue);

        final User newUser = mgr.createUser(UserGenerator.generateRandomUser());
        try {
            Watcher watcher = new Watcher(newUser.getId(), null);
            mgr.addWatcherToIssue(watcher, issue);
            mgr.deleteWatcherFromIssue(watcher, issue);
        } finally {
            mgr.deleteUser(newUser.getId());
        }

        mgr.deleteIssue(issue.getId());
    }

    /**
     * Requires Redmine 2.3
     */
    @Test
    public void testGetIssueWatcher() throws RedmineException {
        final Issue issue = createIssues(1).get(0);
        final Issue retrievedIssue = mgr.getIssueById(issue.getId());
        assertEquals(issue, retrievedIssue);

        final User newUser = mgr.createUser(UserGenerator.generateRandomUser());
        try {
            Watcher watcher = new Watcher(newUser.getId(), null);
            mgr.addWatcherToIssue(watcher, issue);
            final Issue includeWatcherIssue = mgr.getIssueById(issue.getId(),
                    INCLUDE.watchers);
            if (0 < includeWatcherIssue.getWatchers().size()) {
                assertEquals(newUser.getId(), includeWatcherIssue
                        .getWatchers().get(0).getId());
            }
        } finally {
            mgr.deleteUser(newUser.getId());
        }

        mgr.getIssueById(issue.getId());
    }
}