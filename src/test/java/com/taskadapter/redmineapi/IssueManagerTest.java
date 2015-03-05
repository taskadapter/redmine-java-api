package com.taskadapter.redmineapi;

import com.taskadapter.redmineapi.bean.Changeset;
import com.taskadapter.redmineapi.bean.CustomFieldDefinition;
import com.taskadapter.redmineapi.bean.CustomFieldFactory;
import com.taskadapter.redmineapi.bean.Issue;
import com.taskadapter.redmineapi.bean.IssueCategory;
import com.taskadapter.redmineapi.bean.IssueCategoryFactory;
import com.taskadapter.redmineapi.bean.IssueFactory;
import com.taskadapter.redmineapi.bean.IssueRelation;
import com.taskadapter.redmineapi.bean.IssueStatus;
import com.taskadapter.redmineapi.bean.Journal;
import com.taskadapter.redmineapi.bean.JournalDetail;
import com.taskadapter.redmineapi.bean.Project;
import com.taskadapter.redmineapi.bean.SavedQuery;
import com.taskadapter.redmineapi.bean.TimeEntry;
import com.taskadapter.redmineapi.bean.TimeEntryFactory;
import com.taskadapter.redmineapi.bean.Tracker;
import com.taskadapter.redmineapi.bean.User;
import com.taskadapter.redmineapi.bean.Version;
import com.taskadapter.redmineapi.bean.VersionFactory;
import com.taskadapter.redmineapi.bean.Watcher;
import com.taskadapter.redmineapi.bean.WatcherFactory;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static com.taskadapter.redmineapi.IssueHelper.createIssue;
import static com.taskadapter.redmineapi.IssueHelper.createIssues;
import com.taskadapter.redmineapi.bean.CustomField;
import java.util.Arrays;
import java.util.Collections;
import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class IssueManagerTest {

    private static final Logger logger = LoggerFactory.getLogger(IssueManagerTest.class);

    // TODO We don't know activities' IDs!
    // see feature request http://www.redmine.org/issues/7506
    private static final Integer ACTIVITY_ID = 8;

    private static IssueManager issueManager;
    private static ProjectManager projectManager;
    private static String projectKey;
    private static String projectKey2;
    private static RedmineManager mgr;
    private static UserManager userManager;

    @BeforeClass
    public static void oneTimeSetup() {
        mgr = IntegrationTestHelper.createRedmineManager();
        userManager = mgr.getUserManager();
        issueManager = mgr.getIssueManager();
        projectManager = mgr.getProjectManager();
        projectKey = IntegrationTestHelper.createProject(mgr);
        projectKey2 = IntegrationTestHelper.createProject(mgr);
    }

    @AfterClass
    public static void oneTimeTearDown() {
        IntegrationTestHelper.deleteProject(mgr, projectKey);
        IntegrationTestHelper.deleteProject(mgr, projectKey2);
    }

    @Test
    public void issueCreated() {
        try {
            Issue issueToCreate = IssueFactory.createWithSubject("test zzx");

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

            Issue newIssue = issueManager.createIssue(projectKey, issueToCreate);
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
            Issue parentIssue = IssueFactory.createWithSubject("parent 1");
            Issue newParentIssue = issueManager.createIssue(projectKey, parentIssue);

            assertNotNull("Checking parent was created", newParentIssue);
            assertNotNull("Checking ID of parent issue is not null",
                    newParentIssue.getId());

            // Integer parentId = 46;
            Integer parentId = newParentIssue.getId();

            Issue childIssue = IssueFactory.createWithSubject("child 1");
            childIssue.setParentId(parentId);

            Issue newChildIssue = issueManager.createIssue(projectKey, childIssue);

            assertEquals("Checking parent ID of the child issue",
                    parentId, newChildIssue.getParentId());

        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

   @Test
    public void issueClearParent() {
        try {
            Issue parentIssue = IssueFactory.createWithSubject("parent 1");
            Issue newParentIssue = issueManager.createIssue(projectKey, parentIssue);

            assertNotNull("Checking parent was created", newParentIssue);
            assertNotNull("Checking ID of parent issue is not null",
                    newParentIssue.getId());

            Integer parentId = newParentIssue.getId();

            Issue childIssue = IssueFactory.createWithSubject("child 1");
            childIssue.setParentId(parentId);

            Issue newChildIssue = issueManager.createIssue(projectKey, childIssue);

            assertEquals("Checking parent ID of the child issue",
                    parentId, newChildIssue.getParentId());

            Issue updateIssue = IssueFactory.create(newChildIssue.getId());
            updateIssue.setUpdateTracking(true);
            updateIssue.setParentId(null);
            
            issueManager.update(updateIssue);
            
            newChildIssue = issueManager.getIssueById(newChildIssue.getId());
            
            assertNull("Checking parent ID of the child issue - it should be null now",
                    newChildIssue.getParentId());  
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }
    
    @Test
    public void testUpdateIssue() {
        try {
            String originalSubject = "Issue " + new Date();
            Issue issue = IssueFactory.createWithSubject(originalSubject);

            Issue newIssue = issueManager.createIssue(projectKey, issue);
            String changedSubject = "changed subject";
            newIssue.setSubject(changedSubject);

            issueManager.update(newIssue);

            Issue reloadedFromRedmineIssue = issueManager.getIssueById(newIssue.getId());

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
     * @throws RedmineAuthenticationException thrown in case something went wrong while trying to login
     * @throws com.taskadapter.redmineapi.NotFoundException
     *                                        thrown in case the objects requested for could not be found
     */
    @Test
    public void testGetIssueById() throws RedmineException {
        String originalSubject = "Issue " + new Date();
        Issue issue = IssueFactory.createWithSubject(originalSubject);

        Issue newIssue = issueManager.createIssue(projectKey, issue);

        Issue reloadedFromRedmineIssue = issueManager.getIssueById(newIssue.getId());

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
            Issue issueToCreate = IssueFactory.createWithSubject("testGetIssues: " + new Date());
            Issue newIssue = issueManager.createIssue(projectKey, issueToCreate);

            List<Issue> issues = issueManager.getIssues(projectKey, null);
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
        issueManager.getIssues(projectKey, invalidQueryId);
    }

    @Test
    public void testCreateIssueNonUnicodeSymbols() {
        try {
            String nonLatinSymbols = "Example with accents A��o";
            Issue toCreate = IssueFactory.createWithSubject(nonLatinSymbols);
            Issue created = issueManager.createIssue(projectKey, toCreate);
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

            Issue newIssue = issueManager.createIssue(projectKey, issueToCreate);
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
        Issue issueToCreate = IssueFactory.createWithSubject("Summary line 100");
        issueManager.createIssue("someNotExistingProjectKey", issueToCreate);
    }

    @Test(expected = NotFoundException.class)
    public void testGetIssueNonExistingId() throws RedmineException {
        int someNonExistingID = 999999;
        issueManager.getIssueById(someNonExistingID);
    }

    @Test(expected = NotFoundException.class)
    public void testUpdateIssueNonExistingId() throws RedmineException {
        int nonExistingId = 999999;
        Issue issue = IssueFactory.create(nonExistingId);
        issueManager.update(issue);
    }

    @Test
    public void testGetIssuesPaging() {
        try {
            // create 27 issues. default page size is 25.
            createIssues(issueManager, projectKey, 27);
            // mgr.setObjectsPerPage(5); <-- does not work now
            List<Issue> issues = issueManager.getIssues(projectKey, null);
            assertTrue(issues.size() > 26);

            Set<Issue> issueSet = new HashSet<Issue>(issues);
            assertEquals(issues.size(), issueSet.size());
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    @Test(expected = NotFoundException.class)
    public void testDeleteIssue() throws RedmineException {
        Issue issue = createIssues(issueManager, projectKey, 1).get(0);
        Issue retrievedIssue = issueManager.getIssueById(issue.getId());
        assertEquals(issue, retrievedIssue);

        issueManager.deleteIssue(issue.getId());
        issueManager.getIssueById(issue.getId());
    }

    @Test
    public void testUpdateIssueSpecialXMLtags() throws Exception {
        Issue issue = createIssues(issueManager, projectKey, 1).get(0);
        String newSubject = "\"text in quotes\" and <xml> tags";
        String newDescription = "<taghere>\"abc\"</here>";
        issue.setSubject(newSubject);
        issue.setDescription(newDescription);
        issueManager.update(issue);

        Issue updatedIssue = issueManager.getIssueById(issue.getId());
        assertEquals(newSubject, updatedIssue.getSubject());
        assertEquals(newDescription, updatedIssue.getDescription());
    }

    @Test
    public void testCreateRelation() {
        try {
            List<Issue> issues = createIssues(issueManager, projectKey, 2);
            Issue src = issues.get(0);
            Issue target = issues.get(1);

            String relationText = IssueRelation.TYPE.precedes.toString();
            IssueRelation r = issueManager.createRelation(src.getId(), target.getId(),
                    relationText);
            assertEquals(src.getId(), r.getIssueId());
            assertEquals(target.getId(), r.getIssueToId());
            assertEquals(relationText, r.getType());
        } catch (Exception e) {
            fail(e.toString());
        }
    }

    private IssueRelation createTwoRelatedIssues() throws RedmineException {
        List<Issue> issues = createIssues(issueManager, projectKey, 2);
        Issue src = issues.get(0);
        Issue target = issues.get(1);

        String relationText = IssueRelation.TYPE.precedes.toString();
        return issueManager.createRelation(src.getId(), target.getId(), relationText);
    }

    @Test
    public void issueRelationsAreCreatedAndLoadedOK() {
        try {
            IssueRelation relation = createTwoRelatedIssues();
            Issue issue = issueManager.getIssueById(relation.getIssueId(),
                    Include.relations);
            Issue issueTarget = issueManager.getIssueById(relation.getIssueToId(),
                    Include.relations);

            assertThat(issue.getRelations().size()).isEqualTo(1);
            assertThat(issueTarget.getRelations().size()).isEqualTo(1);

            IssueRelation relation1 = issue.getRelations().iterator().next();
            assertEquals(issue.getId(), relation1.getIssueId());
            assertEquals(issueTarget.getId(), relation1.getIssueToId());
            assertEquals("precedes", relation1.getType());
            assertEquals((Integer) 0, relation1.getDelay());

            IssueRelation reverseRelation = issueTarget.getRelations().iterator().next();
            // both forward and reverse relations are the same!
            assertEquals(relation1, reverseRelation);
        } catch (Exception e) {
            fail(e.toString());
        }
    }

    @Test
    public void testIssureRelationDelete() throws RedmineException {
        IssueRelation relation = createTwoRelatedIssues();

        issueManager.deleteRelation(relation.getId());
        Issue issue = issueManager
                .getIssueById(relation.getIssueId(), Include.relations);
        assertTrue(issue.getRelations().isEmpty());
    }

    @Test
    public void testIssueRelationsDelete() throws RedmineException {
        List<Issue> issues = createIssues(issueManager, projectKey, 3);
        Issue src = issues.get(0);
        Issue target = issues.get(1);
        String relationText = IssueRelation.TYPE.precedes.toString();

        issueManager.createRelation(src.getId(), target.getId(), relationText);

        target = issues.get(2);
        issueManager.createRelation(src.getId(), target.getId(), relationText);

        src = issueManager.getIssueById(src.getId(), Include.relations);
        issueManager.deleteIssueRelations(src);

        Issue issue = issueManager.getIssueById(src.getId(), Include.relations);
        assertTrue(issue.getRelations().isEmpty());
    }

    /**
     * Requires Redmine 2.3
     */
    @Test
    public void testAddIssueWatcher() throws RedmineException {
        final Issue issue = createIssues(issueManager, projectKey, 1).get(0);
        final Issue retrievedIssue = issueManager.getIssueById(issue.getId());
        assertEquals(issue, retrievedIssue);

        final User newUser = userManager.createUser(UserGenerator.generateRandomUser());
        try {
            Watcher watcher = WatcherFactory.create(newUser.getId());
            issueManager.addWatcherToIssue(watcher, issue);
        } finally {
            userManager.deleteUser(newUser.getId());
        }

        issueManager.getIssueById(issue.getId());
    }

    /**
     * Requires Redmine 2.3
     */
    @Test
    public void testDeleteIssueWatcher() throws RedmineException {
        final Issue issue = createIssues(issueManager, projectKey, 1).get(0);
        final Issue retrievedIssue = issueManager.getIssueById(issue.getId());
        assertEquals(issue, retrievedIssue);

        final User newUser = userManager.createUser(UserGenerator.generateRandomUser());
        try {
            Watcher watcher = WatcherFactory.create(newUser.getId());
            issueManager.addWatcherToIssue(watcher, issue);
            issueManager.deleteWatcherFromIssue(watcher, issue);
        } finally {
            userManager.deleteUser(newUser.getId());
        }

        issueManager.deleteIssue(issue.getId());
    }

    /**
     * Requires Redmine 2.3
     */
    @Test
    public void testGetIssueWatcher() throws RedmineException {
        final Issue issue = createIssues(issueManager, projectKey, 1).get(0);
        final Issue retrievedIssue = issueManager.getIssueById(issue.getId());
        assertEquals(issue, retrievedIssue);

        final User newUser = userManager.createUser(UserGenerator.generateRandomUser());
        try {
            Watcher watcher = WatcherFactory.create(newUser.getId());
            issueManager.addWatcherToIssue(watcher, issue);
            final Issue includeWatcherIssue = issueManager.getIssueById(issue.getId(),
                    Include.watchers);
            if (!includeWatcherIssue.getWatchers().isEmpty()) {
                Watcher watcher1 = includeWatcherIssue.getWatchers().iterator().next();
                assertThat(watcher1.getId()).isEqualTo(newUser.getId());
            }
        } finally {
            userManager.deleteUser(newUser.getId());
        }

        issueManager.getIssueById(issue.getId());
    }

    @Test
    public void testAddIssueWithWatchers() throws RedmineException {
        final Issue issue = IssueHelper.generateRandomIssue();

        final User newUserWatcher = userManager.createUser(UserGenerator.generateRandomUser());

        try {
            List<Watcher> watchers = new ArrayList<Watcher>();
            Watcher watcher = WatcherFactory.create(newUserWatcher.getId());
            watchers.add(watcher);

            issue.addWatchers(watchers);

            final Issue retrievedIssue = issueManager.createIssue(projectKey, issue);
            final Issue retrievedIssueWithWatchers =  issueManager.getIssueById(retrievedIssue.getId(), Include.watchers);

            assertNotNull(retrievedIssueWithWatchers);
            assertNotNull(retrievedIssueWithWatchers.getWatchers());
            assertEquals(watchers.size(), retrievedIssueWithWatchers.getWatchers().size());
            assertEquals(watcher.getId(), retrievedIssueWithWatchers.getWatchers().iterator().next().getId());
        } finally {
            userManager.deleteUser(newUserWatcher.getId());
        }
    }

    @Test
    public void testGetIssuesBySummary() {
        String summary = "issue with subject ABC";
        try {
            Issue issue = IssueFactory.createWithSubject(summary);
            User assignee = IntegrationTestHelper.getOurUser();
            issue.setAssignee(assignee);

            Issue newIssue = issueManager.createIssue(projectKey, issue);
            assertNotNull("Checking returned result", newIssue);
            assertNotNull("New issue must have some ID",
                    newIssue.getId());

            // try to find the issue
            List<Issue> foundIssues = issueManager.getIssuesBySummary(projectKey,
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
            List<Issue> foundIssues = issueManager.getIssuesBySummary(projectKey,
                    summary);
            assertNotNull("Search result must be not null", foundIssues);
            assertTrue("Search result list must be empty",
                    foundIssues.isEmpty());
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test(expected = RedmineAuthenticationException.class)
    public void noAPIKeyOnCreateIssueThrowsAE() throws Exception {
        TestConfig testConfig = new TestConfig();
        RedmineManager redmineMgrEmpty = RedmineManagerFactory.createUnauthenticated(testConfig.getURI());
        Issue issue = IssueFactory.createWithSubject("test zzx");
        redmineMgrEmpty.getIssueManager().createIssue(projectKey, issue);
    }

    @Test(expected = RedmineAuthenticationException.class)
    public void wrongAPIKeyOnCreateIssueThrowsAE() throws Exception {
        TestConfig testConfig = new TestConfig();
        RedmineManager redmineMgrInvalidKey = RedmineManagerFactory.createWithApiKey(
                testConfig.getURI(), "wrong_key");
        Issue issue = IssueFactory.createWithSubject("test zzx");
        redmineMgrInvalidKey.getIssueManager().createIssue(projectKey, issue);
    }

    @Test
    public void testIssueDoneRatio() {
        try {
            Issue issue = new Issue();
            String subject = "Issue " + new Date();
            issue.setSubject(subject);

            Issue createdIssue = issueManager.createIssue(projectKey, issue);
            assertEquals("Initial 'done ratio' must be 0", (Integer) 0,
                    createdIssue.getDoneRatio());
            Integer doneRatio = 50;
            createdIssue.setDoneRatio(doneRatio);
            issueManager.update(createdIssue);

            Integer issueId = createdIssue.getId();
            Issue reloadedFromRedmineIssue = issueManager.getIssueById(issueId);
            assertEquals(
                    "Checking if 'update issue' operation changed 'done ratio' field",
                    doneRatio, reloadedFromRedmineIssue.getDoneRatio());

            Integer invalidDoneRatio = 130;
            reloadedFromRedmineIssue.setDoneRatio(invalidDoneRatio);
            try {
                issueManager.update(reloadedFromRedmineIssue);
            } catch (RedmineProcessingException e) {
                assertEquals("Must be 1 error", 1, e.getErrors().size());
                assertEquals("Checking error text",
                        "% Done is not included in the list", e.getErrors()
                                .get(0));
            }

            Issue reloadedFromRedmineIssueUnchanged = issueManager.getIssueById(issueId);
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

            Issue createdIssue = issueManager.createIssue(projectKey, issue);
            assertEquals("Checking description", descr,
                    createdIssue.getDescription());

            createdIssue.setDescription(null);
            issueManager.update(createdIssue);

            Integer issueId = createdIssue.getId();
            Issue reloadedFromRedmineIssue = issueManager.getIssueById(issueId);
            assertEquals("Description must not be erased", descr,
                    reloadedFromRedmineIssue.getDescription());

            reloadedFromRedmineIssue.setDescription("");
            issueManager.update(reloadedFromRedmineIssue);

            Issue reloadedFromRedmineIssueUnchanged = issueManager.getIssueById(issueId);
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
            Issue newIssue = issueManager.createIssue(projectKey, issueToCreate);

            Issue loadedIssueWithJournals = issueManager.getIssueById(newIssue.getId(),
                    Include.journals);
            assertTrue(loadedIssueWithJournals.getJournals().isEmpty());

            String commentDescribingTheUpdate = "some comment describing the issue update";
            loadedIssueWithJournals.setSubject("new subject");
            loadedIssueWithJournals.setNotes(commentDescribingTheUpdate);
            issueManager.update(loadedIssueWithJournals);

            Issue loadedIssueWithJournals2 = issueManager.getIssueById(newIssue.getId(),
                    Include.journals);
            assertEquals(1, loadedIssueWithJournals2.getJournals()
                    .size());

            Journal journalItem = loadedIssueWithJournals2.getJournals().iterator().next();
            assertEquals(commentDescribingTheUpdate, journalItem.getNotes());
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

            Issue loadedIssueWithoutJournals = issueManager.getIssueById(newIssue.getId());
            assertTrue(loadedIssueWithoutJournals.getJournals().isEmpty());

        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }
    @Test
    public void emptyDescriptionReturnedAsEmptyString() throws RedmineException {
        Issue issue = IssueFactory.createWithSubject("Issue " + new Date());
        Issue createdIssue = issueManager.createIssue(projectKey, issue);
        assertEquals("Description must be an empty string, not NULL", "",
                createdIssue.getDescription());
    }

    @Test
    public void updateIssueDescription() throws RedmineException {
        Issue issue = new Issue();
        issue.setSubject("test123");
        final Issue iss1 = issueManager.createIssue(projectKey, issue);
        final Issue iss2 = IssueFactory.create(iss1.getId());
        iss2.setDescription("This is a test");
        issueManager.update(iss2);
        final Issue iss3 = issueManager.getIssueById(iss2.getId());
        assertEquals("test123", iss3.getSubject());
        assertEquals("This is a test", iss3.getDescription());
    }

    @Test
    public void updateIssueTitle() throws RedmineException {
        Issue issue = new Issue();
        issue.setSubject("test123");
        issue.setDescription("Original description");
        final Issue iss1 = issueManager.createIssue(projectKey, issue);
        final Issue iss2 = IssueFactory.create(iss1.getId());
        iss2.setSubject("New subject");
        issueManager.update(iss2);
        final Issue iss3 = issueManager.getIssueById(iss2.getId());
        assertEquals("New subject", iss3.getSubject());
        assertEquals("Original description", iss3.getDescription());
    }

    /**
     * Test for issue 64 (time entry format)
     */
    @Test
    public void testTimeEntryComments() throws RedmineException {
        Issue issue = createIssues(issueManager, projectKey, 1).get(0);
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
        TimeEntry createdEntry = issueManager.createTimeEntry(entry);

        assertNotNull(createdEntry);
        assertEquals(comment, createdEntry.getComment());

        createdEntry.setComment("New comment");
        issueManager.update(createdEntry);
        final TimeEntry updatedEntry = issueManager.getTimeEntry(createdEntry.getId());
        assertEquals("New comment", updatedEntry.getComment());
    }

    @Test
    public void testIssuePriorities() throws RedmineException {
        assertTrue(issueManager.getIssuePriorities().size() > 0);
    }

    @Test
    public void testTimeEntryActivities() throws RedmineException {
        assertTrue(issueManager.getTimeEntryActivities().size() > 0);
    }
    @Test
    public void testGetTimeEntries() throws RedmineException {
        List<TimeEntry> list = issueManager.getTimeEntries();
        assertNotNull(list);
    }

    @Test
    public void testCreateGetTimeEntry() throws RedmineException {
        Issue issue = createIssues(issueManager, projectKey, 1).get(0);
        Integer issueId = issue.getId();

        TimeEntry entry = TimeEntryFactory.create();
        Float hours = 11f;
        entry.setHours(hours);
        entry.setIssueId(issueId);
        // TODO We don't know activities IDs!
        // see feature request http://www.redmine.org/issues/7506
        entry.setActivityId(ACTIVITY_ID);
        TimeEntry createdEntry = issueManager.createTimeEntry(entry);

        assertNotNull(createdEntry);
        logger.debug("Created time entry " + createdEntry);
        assertEquals(hours, createdEntry.getHours());

        Float newHours = 22f;
        createdEntry.setHours(newHours);

        issueManager.update(createdEntry);

        TimeEntry updatedEntry = issueManager.getTimeEntry(createdEntry.getId());
        assertEquals(newHours, updatedEntry.getHours());
    }

    @Test(expected = NotFoundException.class)
    public void testCreateDeleteTimeEntry() throws RedmineException {
        Issue issue = createIssues(issueManager, projectKey, 1).get(0);
        Integer issueId = issue.getId();

        TimeEntry entry = TimeEntryFactory.create();
        Float hours = 4f;
        entry.setHours(hours);
        entry.setIssueId(issueId);
        entry.setActivityId(ACTIVITY_ID);
        TimeEntry createdEntry = issueManager.createTimeEntry(entry);
        assertNotNull(createdEntry);

        issueManager.deleteTimeEntry(createdEntry.getId());
        issueManager.getTimeEntry(createdEntry.getId());
    }

    @Test
    public void testGetTimeEntriesForIssue() throws RedmineException {
        Issue issue = createIssues(issueManager, projectKey, 1).get(0);
        Integer issueId = issue.getId();
        Float hours1 = 2f;
        Float hours2 = 7f;
        Float totalHoursExpected = hours1 + hours2;
        TimeEntry createdEntry1 = createTimeEntry(issueId, hours1);
        TimeEntry createdEntry2 = createTimeEntry(issueId, hours2);
        assertNotNull(createdEntry1);
        assertNotNull(createdEntry2);

        List<TimeEntry> entries = issueManager.getTimeEntriesForIssue(issueId);
        assertEquals(2, entries.size());
        Float totalTime = 0f;
        for (TimeEntry timeEntry : entries) {
            totalTime += timeEntry.getHours();
        }
        assertEquals(totalHoursExpected, totalTime);
    }

    private TimeEntry createTimeEntry(Integer issueId, float hours)
            throws RedmineException {
        TimeEntry entry = TimeEntryFactory.create();
        entry.setHours(hours);
        entry.setIssueId(issueId);
        entry.setActivityId(ACTIVITY_ID);
        return issueManager.createTimeEntry(entry);
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
        Issue toCreate = IssueHelper.generateRandomIssue();
        Version v = VersionFactory.create(1);
        String versionName = "1.0";
        v.setName("1.0");
        v.setProject(mgr.getProjectManager().getProjectByKey(projectKey));
        v = mgr.getProjectManager().createVersion(v);
        toCreate.setTargetVersion(v);
        Issue createdIssue = issueManager.createIssue(existingProjectKey, toCreate);

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

            Issue createdIssue = issueManager.createIssue(projectKey, issue);
            Issue newIssue = issueManager.getIssueById(createdIssue.getId());
            assertEquals((Float) spentHours, newIssue.getSpentHours());
        } catch (Exception e) {
            fail();
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidTimeEntryFailsWithIAEOnCreate() throws RedmineException {
        issueManager.createTimeEntry(createIncompleteTimeEntry());
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidTimeEntryFailsWithIAEOnUpdate() throws RedmineException {
        issueManager.update(createIncompleteTimeEntry());
    }

    private TimeEntry createIncompleteTimeEntry() {
        TimeEntry timeEntry = TimeEntryFactory.create();
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
        int projectId = mgr.getProjectManager().getProjects().get(0).getId();
        timeEntry.setProjectId(projectId);
        try {
            TimeEntry created = issueManager.createTimeEntry(timeEntry);
            logger.debug("Created time entry " + created);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Unexpected " + e.getClass().getSimpleName() + ": "
                    + e.getMessage());
        }
    }

    @Test
    public void testUpdateIssueDoesNotChangeEstimatedTime() {
        try {
            Issue issue = new Issue();
            String originalSubject = "Issue " + new Date();
            issue.setSubject(originalSubject);

            Issue newIssue = issueManager.createIssue(projectKey, issue);
            assertEquals("Estimated hours must be NULL", null,
                    newIssue.getEstimatedHours());

            issueManager.update(newIssue);

            Issue reloadedFromRedmineIssue = issueManager.getIssueById(newIssue.getId());
            assertEquals("Estimated hours must be NULL", null,
                    reloadedFromRedmineIssue.getEstimatedHours());
        } catch (Exception e) {
            fail();
        }
    }

    /**
     * tests the retrieval of statuses.
     *
     * @throws RedmineProcessingException     thrown in case something went wrong in Redmine
     * @throws java.io.IOException                    thrown in case something went wrong while performing I/O
     *                                        operations
     * @throws RedmineAuthenticationException thrown in case something went wrong while trying to login
     * @throws NotFoundException              thrown in case the objects requested for could not be found
     */
    @Test
    public void testGetStatuses() throws RedmineException {
        // TODO we should create some statuses first, but the Redmine Java API
        // does not support this presently
        List<IssueStatus> statuses = issueManager.getStatuses();
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
     * tests the creation and deletion of a {@link com.taskadapter.redmineapi.bean.IssueCategory}.
     *
     * @throws RedmineException               thrown in case something went wrong in Redmine
     * @throws java.io.IOException                    thrown in case something went wrong while performing I/O
     *                                        operations
     * @throws RedmineAuthenticationException thrown in case something went wrong while trying to login
     * @throws NotFoundException              thrown in case the objects requested for could not be found
     */
    @Test
    public void testCreateAndDeleteIssueCategory() throws RedmineException {
        Project project = projectManager.getProjectByKey(projectKey);
        IssueCategory category = IssueCategoryFactory.create(project, "Category" + new Date().getTime());
        category.setAssignee(IntegrationTestHelper.getOurUser());
        IssueCategory newIssueCategory = issueManager.createCategory(category);
        assertNotNull("Expected new category not to be null", newIssueCategory);
        assertNotNull("Expected project of new category not to be null", newIssueCategory.getProject());
        assertNotNull("Expected assignee of new category not to be null",
                newIssueCategory.getAssignee());
        // now delete category
        issueManager.deleteCategory(newIssueCategory);
        // assert that the category is gone
        List<IssueCategory> categories = issueManager.getCategories(project.getId());
        assertTrue(
                "List of categories of test project must be empty now but is "
                        + categories, categories.isEmpty());
    }

    /**
     * tests the retrieval of {@link IssueCategory}s.
     *
     * @throws RedmineException               thrown in case something went wrong in Redmine
     * @throws java.io.IOException                    thrown in case something went wrong while performing I/O
     *                                        operations
     * @throws RedmineAuthenticationException thrown in case something went wrong while trying to login
     * @throws NotFoundException              thrown in case the objects requested for could not be found
     */
    @Test
    public void testGetIssueCategories() throws RedmineException {
        Project project = projectManager.getProjectByKey(projectKey);
        // create some categories
        IssueCategory testIssueCategory1 = IssueCategoryFactory.create(project,
                "Category" + new Date().getTime());
        testIssueCategory1.setAssignee(IntegrationTestHelper.getOurUser());
        IssueCategory newIssueCategory1 = issueManager.createCategory(testIssueCategory1);
        IssueCategory testIssueCategory2 = IssueCategoryFactory.create(project,
                "Category" + new Date().getTime());
        testIssueCategory2.setAssignee(IntegrationTestHelper.getOurUser());
        IssueCategory newIssueCategory2 = issueManager.createCategory(testIssueCategory2);
        try {
            List<IssueCategory> categories = issueManager.getCategories(project.getId());
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
                issueManager.deleteCategory(newIssueCategory1);
            }
            if (newIssueCategory2 != null) {
                issueManager.deleteCategory(newIssueCategory2);
            }
        }
    }

    /**
     * tests the creation of an invalid {@link IssueCategory}.
     *
     * @throws RedmineException               thrown in case something went wrong in Redmine
     * @throws java.io.IOException                    thrown in case something went wrong while performing I/O
     *                                        operations
     * @throws RedmineAuthenticationException thrown in case something went wrong while trying to login
     * @throws NotFoundException              thrown in case the objects requested for could not be found
     */
    @Test(expected = IllegalArgumentException.class)
    public void testCreateInvalidIssueCategory() throws RedmineException {
        IssueCategory category = IssueCategoryFactory.create(null, "InvalidCategory"
                + new Date().getTime());
        issueManager.createCategory(category);
    }

    /**
     * tests the deletion of an invalid {@link IssueCategory}. Expects a
     * {@link NotFoundException} to be thrown.
     *
     * @throws RedmineException               thrown in case something went wrong in Redmine
     * @throws java.io.IOException                    thrown in case something went wrong while performing I/O
     *                                        operations
     * @throws RedmineAuthenticationException thrown in case something went wrong while trying to login
     * @throws NotFoundException              thrown in case the objects requested for could not be found
     */
    @Test(expected = NotFoundException.class)
    public void testDeleteInvalidIssueCategory() throws RedmineException {
        // create new test category
        IssueCategory category = IssueCategoryFactory.create(-1);
        category.setName("InvalidCategory" + new Date().getTime());
        // now try deleting the category
        issueManager.deleteCategory(category);
    }

    /**
     * Tests the creation and retrieval of an
     * {@link com.taskadapter.redmineapi.bean.Issue} with a
     * {@link IssueCategory}.
     *
     * @throws RedmineException               thrown in case something went wrong in Redmine
     * @throws java.io.IOException                    thrown in case something went wrong while performing I/O
     *                                        operations
     * @throws RedmineAuthenticationException thrown in case something went wrong while trying to login
     * @throws NotFoundException              thrown in case the objects requested for could not be found
     */
    @Test
    public void testCreateAndGetIssueWithCategory() throws RedmineException {
        IssueCategory newIssueCategory = null;
        Issue newIssue = null;
        try {
            Project project = projectManager.getProjectByKey(projectKey);
            // create an issue category
            IssueCategory category = IssueCategoryFactory.create(project, "Category_"
                    + new Date().getTime());
            category.setAssignee(IntegrationTestHelper.getOurUser());
            newIssueCategory = issueManager.createCategory(category);
            // create an issue
            Issue issueToCreate = IssueFactory.createWithSubject("getIssueWithCategory_" + UUID.randomUUID());
            issueToCreate.setCategory(newIssueCategory);
            newIssue = issueManager.createIssue(projectKey, issueToCreate);
            // retrieve issue
            Issue retrievedIssue = issueManager.getIssueById(newIssue.getId());
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
                issueManager.deleteIssue(newIssue.getId());
            }
            if (newIssueCategory != null) {
                issueManager.deleteCategory(newIssueCategory);
            }
        }
    }

    @Test
    public void nullStartDateIsPreserved() {
        try {
            Issue issue = IssueFactory.createWithSubject("test start date");
            issue.setStartDate(null);

            Issue newIssue = issueManager.createIssue(projectKey, issue);

            Issue loadedIssue = issueManager.getIssueById(newIssue.getId());
            assertNull(loadedIssue.getStartDate());
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    /**
     * The custom fields used here MUST ALREADY EXIST on the server and be
     * associated with the required task type (bug/feature/task/..).
     * <p/>
     * See feature request http://www.redmine.org/issues/9664
     */
    @Test
    public void testCustomFields() throws Exception {
        Issue issue = createIssues(issueManager, projectKey, 1).get(0);

        // TODO this needs to be reworked, when Redmine gains a real CRUD interface for custom fields
        //
        // To test right now the test system needs:
        //
        // Custom Field with ID 1 needs to be:
        // name: my_custom_1
        // format: Text (string)
        // for all project, for all trackers
        //
        // Custom Field with ID 2 needs to be:
        // name: custom_boolean_1
        // format: Boolean (bool)
        // 
        // Custom Field with ID 3 needs to be:
        // name: custom_multi_list
        // format: List (list)
        // multiple values: enabled
        // possible values: V1, V2, V3
        // default value: V2
        //
        // All fields: need to be issue fields, for all project, for all trackers
        List<CustomFieldDefinition> customFieldDefinitions = mgr.getCustomFieldManager().getCustomFieldDefinitions();
        CustomFieldDefinition customField1 = getCustomFieldByName(customFieldDefinitions, "my_custom_1");
        CustomFieldDefinition customField2 = getCustomFieldByName(customFieldDefinitions, "custom_boolean_1");

        // default empty values
        assertThat(issue.getCustomFields().size()).isEqualTo(3);
        
        issue.clearCustomFields();

        String custom1Value = "some value 123";
        String custom2Value = "true";
        issue.addCustomField(CustomFieldFactory.create(customField1.getId(), customField1.getName(), custom1Value));
        issue.addCustomField(CustomFieldFactory.create(customField2.getId(), customField2.getName(), custom2Value));
        issueManager.update(issue);

        Issue updatedIssue = issueManager.getIssueById(issue.getId());
        assertThat(updatedIssue.getCustomFields().size()).isEqualTo(3);
        assertThat(updatedIssue.getCustomField(customField1.getName())).isEqualTo(custom1Value);
        assertThat(updatedIssue.getCustomField(customField2.getName())).isEqualTo(custom2Value);
    }

    private static CustomFieldDefinition getCustomFieldByName(List<CustomFieldDefinition> customFieldDefinitions, String fieldName) {
        for (CustomFieldDefinition customFieldDefinition : customFieldDefinitions) {
            if (customFieldDefinition.getName().equals(fieldName)) {
                return customFieldDefinition;
            }
        }
        throw new RuntimeException("Custom Field definition '" + fieldName + "' is not found on server.");
    }

    @Test
    public void defaultValueUsedWhenCustomFieldNotProvidedWhenCreatingIssue() throws Exception {
        Issue newIssue = IssueFactory.createWithSubject("test for custom multi fields");
        Issue createdIssue = issueManager.createIssue(projectKey, newIssue);
        CustomField customField = createdIssue.getCustomFieldByName("custom_multi_list");
        assertThat(customField).isNotNull();
        assertThat(customField.getValues().size()).isEqualTo(1);
        assertThat(customField.getValues().get(0)).isEqualTo("V2");
        issueManager.deleteIssue(createdIssue.getId());
    }

    @Test
    public void setOneValueForMultiLineCustomField() throws Exception {
        Issue newIssue = IssueFactory.createWithSubject("test for custom multi fields - set one value");
        CustomFieldDefinition multiFieldDefinition = loadMultiLineCustomFieldDefinition();
        CustomField customField = CustomFieldFactory.create(multiFieldDefinition.getId());

        customField.setValues(Collections.singletonList("V1"));
        newIssue.addCustomField(customField);
        Issue createdIssue = issueManager.createIssue(projectKey, newIssue);
        customField = createdIssue.getCustomFieldByName("custom_multi_list");
        assertThat(customField).isNotNull();
        assertThat(customField.getValues().size()).isEqualTo(1);
        assertThat(customField.getValues().get(0)).isEqualTo("V1");
        issueManager.deleteIssue(createdIssue.getId());
    }

    /**
     * See check for https://github.com/taskadapter/redmine-java-api/issues/54
     */
    @Test
    public void setMultiValuesForMultiLineCustomField() throws Exception {
        Issue newIssue = IssueFactory.createWithSubject("test for custom multi fields - set multiple values");
        CustomFieldDefinition multiFieldDefinition = loadMultiLineCustomFieldDefinition();
        CustomField customField = CustomFieldFactory.create(multiFieldDefinition.getId());
        customField.setValues(Arrays.asList("V1", "V3"));
        newIssue.addCustomField(customField);
        Issue createdIssue = issueManager.createIssue(projectKey, newIssue);
        customField = createdIssue.getCustomFieldByName("custom_multi_list");
        assertThat(customField).isNotNull();
        assertThat(customField.getValues().size()).isEqualTo(2);
        List<String> values = new ArrayList<String>(customField.getValues());
        Collections.sort(values);
        assertThat(customField.getValues().get(0)).isEqualTo("V1");
        assertThat(customField.getValues().get(1)).isEqualTo("V3");
        issueManager.deleteIssue(createdIssue.getId());
    }

    /**
     * This is to make sure we have a workaround for a known bug in redmine 2.6.
     */
    @Test
    public void createIssueWithEmptyListInMultilineCustomFields() throws Exception {
        Issue newIssue = IssueFactory.createWithSubject("test for custom multi fields - set multiple values");
        CustomFieldDefinition multiFieldDefinition = loadMultiLineCustomFieldDefinition();
        CustomField customField = CustomFieldFactory.create(multiFieldDefinition.getId());
        customField.setValues(Collections.EMPTY_LIST);
        newIssue.addCustomField(customField);
        Issue createdIssue = issueManager.createIssue(projectKey, newIssue);
        customField = createdIssue.getCustomFieldByName("custom_multi_list");
        assertThat(customField).isNotNull();
        assertThat(customField.getValues().size()).isEqualTo(0);
        issueManager.deleteIssue(createdIssue.getId());
    }

    private static CustomFieldDefinition loadMultiLineCustomFieldDefinition() throws RedmineException {
        List<CustomFieldDefinition> customFieldDefinitions = mgr.getCustomFieldManager().getCustomFieldDefinitions();
        return getCustomFieldByName(customFieldDefinitions, "custom_multi_list");
    }

    @Ignore
    @Test
    public void testChangesets() throws RedmineException {
        final Issue issue = issueManager.getIssueById(89, Include.changesets);
        assertThat(issue.getChangesets().size()).isEqualTo(2);
        final Changeset firstChange = issue.getChangesets().iterator().next();
        assertNotNull(firstChange.getComments());
    }

    /**
     * Tests the retrieval of {@link Tracker}s.
     *
     * @throws RedmineException               thrown in case something went wrong in Redmine
     * @throws RedmineAuthenticationException thrown in case something went wrong while trying to login
     * @throws NotFoundException              thrown in case the objects requested for could not be found
     */
    @Test
    public void testGetTrackers() throws RedmineException {
        List<Tracker> trackers = issueManager.getTrackers();
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

    @Test
    public void getSavedQueriesDoesNotFailForTempProject()
            throws RedmineException {
        issueManager.getSavedQueries(projectKey);
    }

    @Test
    public void getSavedQueriesDoesNotFailForNULLProject()
            throws RedmineException {
        issueManager.getSavedQueries(null);
    }

    @Ignore("This test requires a specific project configuration")
    @Test
    public void testSavedQueries() throws RedmineException {
        final Collection<SavedQuery> queries = issueManager.getSavedQueries("test");
        assertTrue(queries.size() > 0);
    }

    @Test
    public void statusIsUpdated() throws RedmineException {
        Issue issue = createIssues(issueManager, projectKey, 1).get(0);
        Issue retrievedIssue = issueManager.getIssueById(issue.getId());
        Integer initialStatusId = retrievedIssue.getStatusId();

        List<IssueStatus> statuses = issueManager.getStatuses();
        // get some status ID that is not equal to the initial one
        Integer newStatusId = null;
        for (IssueStatus status : statuses) {
            if (!status.getId().equals(initialStatusId)) {
                newStatusId = status.getId();
                break;
            }
        }
        if (newStatusId == null) {
            throw new RuntimeException("can't run this test: no Issue Statuses are available except for the initial one");
        }
        retrievedIssue.setStatusId(newStatusId);
        issueManager.update(retrievedIssue);

        Issue issueWithUpdatedStatus = issueManager.getIssueById(retrievedIssue.getId());
        assertThat(issueWithUpdatedStatus.getStatusId()).isEqualTo(newStatusId);
    }
    
    @Test
    public void changeProject() throws RedmineException {
        Project project1 = mgr.getProjectManager().getProjectByKey(projectKey);
        Project project2 = mgr.getProjectManager().getProjectByKey(projectKey2);
        Issue issue = createIssue(issueManager, projectKey);
        Issue retrievedIssue = issueManager.getIssueById(issue.getId());
        assertEquals(retrievedIssue.getProject(), project1);
        issue.setProject(project2);
        issueManager.update(issue);
        retrievedIssue = issueManager.getIssueById(issue.getId());
        assertEquals(retrievedIssue.getProject(), project2);
        deleteIssueIfNotNull(issue);
    }

    @Test
    public void issueCanBeCreatedOnBehalfOfAnotherUser() throws RedmineException {
        final User newUser = userManager.createUser(UserGenerator.generateRandomUser());
        Issue issue = null;
        try {
            RedmineManager managerOnBehalfOfUser = IntegrationTestHelper.createRedmineManager();
            managerOnBehalfOfUser.setOnBehalfOfUser(newUser.getLogin());

            issue = createIssue(managerOnBehalfOfUser.getIssueManager(), projectKey);
            assertThat(issue.getAuthor().getFirstName()).isEqualTo(newUser.getFirstName());
            assertThat(issue.getAuthor().getLastName()).isEqualTo(newUser.getLastName());
        } finally {
            userManager.deleteUser(newUser.getId());
            deleteIssueIfNotNull(issue);
        }
    }

    private void deleteIssueIfNotNull(Issue issue) throws RedmineException {
        if (issue != null) {
            issueManager.deleteIssue(issue.getId());
        }
    }
}
