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
import com.taskadapter.redmineapi.bean.ProjectFactory;
import com.taskadapter.redmineapi.bean.SavedQuery;
import com.taskadapter.redmineapi.bean.TimeEntry;
import com.taskadapter.redmineapi.bean.TimeEntryFactory;
import com.taskadapter.redmineapi.bean.Tracker;
import com.taskadapter.redmineapi.bean.User;
import com.taskadapter.redmineapi.bean.Version;
import com.taskadapter.redmineapi.bean.VersionFactory;
import com.taskadapter.redmineapi.bean.Watcher;
import com.taskadapter.redmineapi.bean.WatcherFactory;
import org.apache.http.client.HttpClient;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static com.taskadapter.redmineapi.CustomFieldResolver.getCustomFieldByName;
import static com.taskadapter.redmineapi.IssueHelper.createIssue;
import static com.taskadapter.redmineapi.IssueHelper.createIssues;
import com.taskadapter.redmineapi.bean.CustomField;
import java.util.Arrays;
import java.util.Collections;
import static org.assertj.core.api.Assertions.assertThat;
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
    private static final Integer ACTIVITY_ID = 1;

    private static IssueManager issueManager;
    private static ProjectManager projectManager;
    private static Project project;
    private static int projectId;
    private static String projectKey;
    private static Project project2;
    private static String projectKey2;
    private static RedmineManager mgr;
    private static UserManager userManager;

    @BeforeClass
    public static void oneTimeSetup() {
        mgr = IntegrationTestHelper.createRedmineManager();
        userManager = mgr.getUserManager();
        issueManager = mgr.getIssueManager();
        projectManager = mgr.getProjectManager();
        project = IntegrationTestHelper.createProject(mgr);
        projectId = project.getId();
        projectKey = project.getIdentifier();
        project2 = IntegrationTestHelper.createProject(mgr);
        projectKey2 = project2.getIdentifier();
    }

    //@AfterClass
    //public static void oneTimeTearDown() {
    //    IntegrationTestHelper.deleteProject(mgr, project.getIdentifier());
    //    IntegrationTestHelper.deleteProject(mgr, project2.getIdentifier());
    //}

    @Test
    public void issueCreated() throws RedmineException {
        Issue issueToCreate = IssueFactory.create(projectId, "test zzx");

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
        
        issueToCreate.setIsPrivate(Boolean.TRUE);
        

        Issue newIssue = issueManager.createIssue(issueToCreate);
        
        assertNotNull("Checking is_private", newIssue.getIsPrivate());
        
        System.out.println(newIssue.getIsPrivate());
        
        assertNotNull("Checking returned result", newIssue);
        assertNotNull("New issue must have some ID", newIssue.getId());

        // check startDate
        Calendar returnedStartCal = Calendar.getInstance();
        returnedStartCal.setTime(newIssue.getStartDate());

        assertEquals(startCal.get(Calendar.YEAR), returnedStartCal.get(Calendar.YEAR));
        assertEquals(startCal.get(Calendar.MONTH), returnedStartCal.get(Calendar.MONTH));
        assertEquals(startCal.get(Calendar.DAY_OF_MONTH), returnedStartCal.get(Calendar.DAY_OF_MONTH));

        // check dueDate
        Calendar returnedDueCal = Calendar.getInstance();
        returnedDueCal.setTime(newIssue.getDueDate());

        assertEquals(due.get(Calendar.YEAR), returnedDueCal.get(Calendar.YEAR));
        assertEquals(due.get(Calendar.MONTH), returnedDueCal.get(Calendar.MONTH));
        assertEquals(due.get(Calendar.DAY_OF_MONTH), returnedDueCal.get(Calendar.DAY_OF_MONTH));

        // check ASSIGNEE
        User actualAssignee = newIssue.getAssignee();
        assertNotNull("Checking assignee not null", actualAssignee);
        assertEquals("Checking assignee id", assignee.getId(), actualAssignee.getId());

        // check AUTHOR
        Integer EXPECTED_AUTHOR_ID = IntegrationTestHelper.getOurUser().getId();
        assertEquals(EXPECTED_AUTHOR_ID, newIssue.getAuthor().getId());

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
    }

    @Test
    public void issueWithParentCreated() throws RedmineException {
        Issue parentIssue = IssueFactory.create(projectId, "parent 1");
        Issue newParentIssue = issueManager.createIssue(parentIssue);

        assertNotNull("Checking parent was created", newParentIssue);
        assertNotNull("Checking ID of parent issue is not null", newParentIssue.getId());

        // Integer parentId = 46;
        Integer parentId = newParentIssue.getId();

        Issue childIssue = IssueFactory.create(projectId, "child 1");
        childIssue.setParentId(parentId);

        Issue newChildIssue = issueManager.createIssue(childIssue);
        assertEquals("Checking parent ID of the child issue", parentId, newChildIssue.getParentId());
    }

    @Test
    public void testUpdateIssue() throws RedmineException {
        String originalSubject = "Issue " + new Date();
        Issue issue = IssueFactory.create(projectId, originalSubject);
        Issue newIssue = issueManager.createIssue(issue);
        String changedSubject = "changed subject";
        newIssue.setSubject(changedSubject);

        issueManager.update(newIssue);

        Issue reloadedFromRedmineIssue = issueManager.getIssueById(newIssue.getId());

        assertEquals("Checking if 'update issue' operation changed 'subject' field",
                changedSubject, reloadedFromRedmineIssue.getSubject());
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
        Issue issue = IssueFactory.create(projectId, originalSubject);

        Issue newIssue = issueManager.createIssue(issue);

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
    public void testGetIssues() throws RedmineException {
        // create at least 1 issue
        Issue issueToCreate = IssueFactory.create(projectId, "testGetIssues: " + new Date());
        Issue newIssue = issueManager.createIssue(issueToCreate);

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
    }

    @Test(expected = NotFoundException.class)
    public void testGetIssuesInvalidQueryId() throws RedmineException {
        Integer invalidQueryId = 9999999;
        issueManager.getIssues(projectKey, invalidQueryId);
    }

    @Test
    public void testCreateIssueNonUnicodeSymbols() throws RedmineException {
        String nonLatinSymbols = "Example with accents A��o";
        Issue toCreate = IssueFactory.create(projectId, nonLatinSymbols);
        Issue created = issueManager.createIssue(toCreate);
        assertEquals(nonLatinSymbols, created.getSubject());
    }

    @Test
    public void testCreateIssueSummaryOnly() throws RedmineException {
        Issue issueToCreate = IssueFactory.create(projectId, "This is the summary line 123");

        Issue newIssue = issueManager.createIssue(issueToCreate);
        assertNotNull("Checking returned result", newIssue);
        assertNotNull("New issue must have some ID", newIssue.getId());

        // check AUTHOR
        Integer EXPECTED_AUTHOR_ID = IntegrationTestHelper.getOurUser().getId();
        assertEquals(EXPECTED_AUTHOR_ID, newIssue.getAuthor().getId());
    }

    /* this test fails with Redmine 3.0.0-3.0.3 because Redmine 3.0.x started
     * returning "not authorized" instead of "not found" for projects with unknown Ids.
     * This worked differently with Redmine 2.6.x.
     * <p>
     * This test is not critical for the release of Redmine Java API library. I am marking it as "ignored" for now.
    */
    @Ignore
    @Test(expected = NotFoundException.class)
    public void creatingIssueWithNonExistingProjectIdGivesNotFoundException() throws RedmineException {
        int nonExistingProjectId = 99999999; // hopefully this does not exist :)
        Issue issueToCreate = IssueFactory.create(nonExistingProjectId, "Summary line 100");
        issueManager.createIssue(issueToCreate);
    }

    @Test(expected = NotFoundException.class)
    public void retrievingIssueWithNonExistingIdGivesNotFoundException() throws RedmineException {
        int someNonExistingID = 999999;
        issueManager.getIssueById(someNonExistingID);
    }

    @Test(expected = NotFoundException.class)
    public void updatingIssueWithNonExistingIdGivesNotFoundException() throws RedmineException {
        int nonExistingId = 999999;
        Issue issue = IssueFactory.create(nonExistingId);
        issueManager.update(issue);
    }

    @Test
    public void testGetIssuesPaging() throws RedmineException {
        // create 27 issues. default page size is 25.
        createIssues(issueManager, projectId, 27);
        List<Issue> issues = issueManager.getIssues(projectKey, null);
        assertThat(issues.size()).isGreaterThan(26);

        // check that there are no duplicates in the list.
        Set<Issue> issueSet = new HashSet<>(issues);
        assertThat(issueSet.size()).isEqualTo(issues.size());
    }

    @Test
    public void canControlLimitAndOffsetDirectly() throws RedmineException {
        // create 27 issues. default Redmine page size is usually 25 (unless changed in the server settings).
        createIssues(issueManager, projectId, 27);
        Map<String, String> params = new HashMap<>();
        params.put("limit", "3");
        params.put("offset", "0");
        params.put("project_id", projectId + "");
        List<Issue> issues = issueManager.getIssues(params);
        // only the requested number of issues is loaded, not all result pages.
        assertThat(issues.size()).isEqualTo(3);
    }

    @Test(expected = NotFoundException.class)
    public void testDeleteIssue() throws RedmineException {
        Issue issue = createIssues(issueManager, projectId, 1).get(0);
        Issue retrievedIssue = issueManager.getIssueById(issue.getId());
        assertEquals(issue, retrievedIssue);

        issueManager.deleteIssue(issue.getId());
        issueManager.getIssueById(issue.getId());
    }

    @Test
    public void testUpdateIssueSpecialXMLtags() throws Exception {
        Issue issue = createIssues(issueManager, projectId, 1).get(0);
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
    public void testCreateRelation() throws RedmineException {
        List<Issue> issues = createIssues(issueManager, projectId, 2);
        Issue src = issues.get(0);
        Issue target = issues.get(1);

        String relationText = IssueRelation.TYPE.precedes.toString();
        IssueRelation r = issueManager.createRelation(src.getId(), target.getId(), relationText);
        assertEquals(src.getId(), r.getIssueId());
        assertEquals(target.getId(), r.getIssueToId());
        assertEquals(relationText, r.getType());
    }

    private IssueRelation createTwoRelatedIssues() throws RedmineException {
        List<Issue> issues = createIssues(issueManager, projectId, 2);
        Issue src = issues.get(0);
        Issue target = issues.get(1);

        String relationText = IssueRelation.TYPE.precedes.toString();
        return issueManager.createRelation(src.getId(), target.getId(), relationText);
    }

    @Test
    public void issueRelationsAreCreatedAndLoadedOK() throws RedmineException {
        IssueRelation relation = createTwoRelatedIssues();
        Issue issue = issueManager.getIssueById(relation.getIssueId(), Include.relations);
        Issue issueTarget = issueManager.getIssueById(relation.getIssueToId(), Include.relations);

        assertThat(issue.getRelations().size()).isEqualTo(1);
        assertThat(issueTarget.getRelations().size()).isEqualTo(1);

        IssueRelation relation1 = issue.getRelations().iterator().next();
        assertThat(relation1.getIssueId()).isEqualTo(issue.getId());
        assertThat(relation1.getIssueToId()).isEqualTo(issueTarget.getId());
        assertThat(relation1.getType()).isEqualTo("precedes");
        assertThat(relation1.getDelay()).isEqualTo((Integer) 0);

        IssueRelation reverseRelation = issueTarget.getRelations().iterator().next();
        // both forward and reverse relations are the same!
        assertThat(reverseRelation).isEqualTo(relation1);
    }

    @Test
    public void issueRelationIsDeleted() throws RedmineException {
        IssueRelation relation = createTwoRelatedIssues();
        issueManager.deleteRelation(relation.getId());
        Issue issue = issueManager.getIssueById(relation.getIssueId(), Include.relations);
        assertThat(issue.getRelations()).isEmpty();
    }

    @Test
    public void testIssueRelationsDelete() throws RedmineException {
        List<Issue> issues = createIssues(issueManager, projectId, 3);
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
        final Issue issue = createIssues(issueManager, projectId, 1).get(0);
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
        final Issue issue = createIssues(issueManager, projectId, 1).get(0);
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
        final Issue issue = createIssues(issueManager, projectId, 1).get(0);
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
        final Issue issue = IssueHelper.generateRandomIssue(projectId);

        final User newUserWatcher = userManager.createUser(UserGenerator.generateRandomUser());

        try {
            List<Watcher> watchers = new ArrayList<>();
            Watcher watcher = WatcherFactory.create(newUserWatcher.getId());
            watchers.add(watcher);

            issue.addWatchers(watchers);

            final Issue retrievedIssue = issueManager.createIssue(issue);
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
    public void testGetIssuesBySummary() throws RedmineException {
        String summary = "issue with subject ABC";
        Issue issue = IssueFactory.create(projectId, summary);
        User assignee = IntegrationTestHelper.getOurUser();
        issue.setAssignee(assignee);

        Issue newIssue = issueManager.createIssue(issue);
        assertNotNull("Checking returned result", newIssue);
        assertNotNull("New issue must have some ID", newIssue.getId());

        List<Issue> foundIssues = issueManager.getIssuesBySummary(projectKey, summary);

        assertNotNull("Checking if search results is not NULL",
                foundIssues);
        assertTrue("Search results must be not empty",
                !(foundIssues.isEmpty()));

        Issue loadedIssue1 = RedmineTestUtils.findIssueInList(foundIssues,
                newIssue.getId());
        assertNotNull(loadedIssue1);
        assertEquals(summary, loadedIssue1.getSubject());
    }

    @Test
    public void findByNonExistingSummaryReturnsEmptyList() throws RedmineException {
        String summary = "some summary here for issue which does not exist";
        List<Issue> foundIssues = issueManager.getIssuesBySummary(projectKey, summary);
        assertNotNull("Search result must be not null", foundIssues);
        assertTrue("Search result list must be empty", foundIssues.isEmpty());
    }

    @Test(expected = RedmineAuthenticationException.class)
    public void noAPIKeyOnCreateIssueThrowsAE() throws Exception {
        TestConfig testConfig = new TestConfig();
        final HttpClient httpClient = IntegrationTestHelper.getHttpClientForTestServer();

        RedmineManager redmineMgrEmpty = RedmineManagerFactory.createUnauthenticated(testConfig.getURI(),
                httpClient);
        Issue issue = IssueFactory.create(projectId, "test zzx");
        redmineMgrEmpty.getIssueManager().createIssue(issue);
    }

    @Test(expected = RedmineAuthenticationException.class)
    public void wrongAPIKeyOnCreateIssueThrowsAE() throws Exception {
        TestConfig testConfig = new TestConfig();
        final HttpClient httpClient = IntegrationTestHelper.getHttpClientForTestServer();

        RedmineManager redmineMgrInvalidKey = RedmineManagerFactory.createWithApiKey(
                testConfig.getURI(), "wrong_key", httpClient);
        Issue issue = IssueFactory.create(projectId, "test zzx");
        redmineMgrInvalidKey.getIssueManager().createIssue(issue);
    }

    @Test
    public void testIssueDoneRatio() throws RedmineException {
        Issue issue = IssueFactory.create(projectId, "Issue " + new Date());
        Issue createdIssue = issueManager.createIssue(issue);
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
    }

    @Test
    public void testIssueNullDescriptionDoesNotEraseIt() throws RedmineException {
        Issue issue = new Issue();
        String subject = "Issue " + new Date();
        String descr = "Some description";
        issue.setSubject(subject);
        issue.setDescription(descr);
        issue.setProject(ProjectFactory.create(projectId));

        Issue createdIssue = issueManager.createIssue(issue);
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
    }

    @Test
    public void testIssueJournals() throws RedmineException {
        // create at least 1 issue
        Issue issueToCreate = new Issue();
        issueToCreate.setSubject("testGetIssues: " + new Date());
        issueToCreate.setProject(ProjectFactory.create(projectId));
        Issue newIssue = issueManager.createIssue(issueToCreate);

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
    }

    @Test
    public void emptyDescriptionReturnedAsEmptyString() throws RedmineException {
        Issue issue = IssueFactory.create(projectId, "Issue " + new Date());
        Issue createdIssue = issueManager.createIssue(issue);
        assertEquals("Description must be an empty string, not NULL", "",
                createdIssue.getDescription());
    }

    @Test
    public void updateIssueDescription() throws RedmineException {
        Issue issue = IssueFactory.create(projectId, "test123");
        final Issue iss1 = issueManager.createIssue(issue);
        final Issue iss2 = IssueFactory.create(iss1.getId());
        iss2.setProject(ProjectFactory.create(projectId));
        iss2.setDescription("This is a test");
        issueManager.update(iss2);
        final Issue iss3 = issueManager.getIssueById(iss2.getId());
        assertEquals("test123", iss3.getSubject());
        assertEquals("This is a test", iss3.getDescription());
    }

    @Test
    public void updateIssueTitle() throws RedmineException {
        Issue issue = IssueFactory.create(projectId, "test123");
        issue.setDescription("Original description");
        final Issue iss1 = issueManager.createIssue(issue);
        final Issue iss2 = IssueFactory.create(iss1.getId());
        iss2.setSubject("New subject");
        iss2.setProject(ProjectFactory.create(projectId));
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
        Issue issue = createIssues(issueManager, projectId, 1).get(0);
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
        Issue issue = createIssues(issueManager, projectId, 1).get(0);
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
        Issue issue = createIssues(issueManager, projectId, 1).get(0);
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

        final String versionName = "1.0";
        final Issue issueToCreate = IssueHelper.generateRandomIssue(projectId);
        final Version version = VersionFactory.create(1);
        version.setName(versionName);
        Project project = mgr.getProjectManager().getProjectByKey(projectKey);
        version.setProject(project);
        final Version createdVersion = mgr.getProjectManager().createVersion(version);
        issueToCreate.setTargetVersion(createdVersion);
        issueToCreate.setProject(project);
        final Issue createdIssue = issueManager.createIssue(issueToCreate);

        assertNotNull(createdIssue.getTargetVersion());
        assertEquals(createdIssue.getTargetVersion().getName(),
                versionName);
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
    public void testViolateTimeEntryConstraint_ProjectOrIssueID() throws RedmineException {
        TimeEntry timeEntry = createIncompleteTimeEntry();
        int projectId = mgr.getProjectManager().getProjects().get(0).getId();
        timeEntry.setProjectId(projectId);
        issueManager.createTimeEntry(timeEntry);
        // no exceptions - good.
    }

    @Test
    public void testUpdateIssueDoesNotChangeEstimatedTime() throws RedmineException {
        String originalSubject = "Issue " + new Date();
        Issue issue = IssueFactory.create(projectId, originalSubject);
        Issue newIssue = issueManager.createIssue(issue);
        assertEquals("Estimated hours must be NULL", null, newIssue.getEstimatedHours());

        issueManager.update(newIssue);

        Issue reloadedFromRedmineIssue = issueManager.getIssueById(newIssue.getId());
        assertEquals("Estimated hours must be NULL", null, reloadedFromRedmineIssue.getEstimatedHours());
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
            Issue issueToCreate = IssueFactory.create(projectId, "getIssueWithCategory_" + UUID.randomUUID());
            issueToCreate.setCategory(newIssueCategory);
            newIssue = issueManager.createIssue(issueToCreate);
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
    public void nullStartDateIsPreserved() throws RedmineException {
        Issue issue = IssueFactory.create(projectId, "test start date");
        issue.setStartDate(null);
        Issue newIssue = issueManager.createIssue(issue);
        Issue loadedIssue = issueManager.getIssueById(newIssue.getId());
        assertNull(loadedIssue.getStartDate());
    }

    /**
     * The custom fields used here MUST ALREADY EXIST on the server and be
     * associated with the required task type (bug/feature/task/..).
     * <p/>
     * See feature request http://www.redmine.org/issues/9664
     */
    @Test
    public void testCustomFields() throws Exception {
        Issue issue = createIssue(issueManager, projectId);

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
        assertThat(updatedIssue.getCustomFieldByName(customField1.getName()).getValue()).isEqualTo(custom1Value);
        assertThat(updatedIssue.getCustomFieldByName(customField2.getName()).getValue()).isEqualTo(custom2Value);
    }

    @Test
    public void defaultValueUsedWhenCustomFieldNotProvidedWhenCreatingIssue() throws Exception {
        Issue newIssue = IssueFactory.create(projectId, "test for custom multi fields");
        Issue createdIssue = issueManager.createIssue(newIssue);
        CustomField customField = createdIssue.getCustomFieldByName("custom_multi_list");
        assertThat(customField).isNotNull();
        assertThat(customField.getValues().size()).isEqualTo(1);
        assertThat(customField.getValues().get(0)).isEqualTo("V2");
        issueManager.deleteIssue(createdIssue.getId());
    }

    @Test
    public void setOneValueForMultiLineCustomField() throws Exception {
        Issue newIssue = IssueFactory.create(projectId, "test for custom multi fields - set one value");
        CustomFieldDefinition multiFieldDefinition = loadMultiLineCustomFieldDefinition();
        CustomField customField = CustomFieldFactory.create(multiFieldDefinition.getId());

        String defaultValue = multiFieldDefinition.getDefaultValue();
        customField.setValues(Collections.singletonList(defaultValue));
        newIssue.addCustomField(customField);
        Issue createdIssue = issueManager.createIssue(newIssue);
        customField = createdIssue.getCustomFieldByName("custom_multi_list");
        assertThat(customField).isNotNull();
        assertThat(customField.getValues().size()).isEqualTo(1);
        assertThat(customField.getValues().get(0)).isEqualTo(defaultValue);
        issueManager.deleteIssue(createdIssue.getId());
    }

    /**
     * See check for https://github.com/taskadapter/redmine-java-api/issues/54
     *
     * BUG in Redmine 3.0.0: multi-line custom fields values are ignored by Redmine 3.0.0 for new issues
     * without tracker_id value.
     * the server ignores values V1, V3 and assigns default V2 value to that multi-line custom field.
     * I submitted this as http://www.redmine.org/issues/19368 - fixed in Redmine 3.0.1
     */
    @Test
    public void setMultiValuesForMultiLineCustomField() throws Exception {
        Issue issue = IssueFactory.create(projectId, "test for custom multi fields - set multiple values");
        CustomFieldDefinition multiFieldDefinition = loadMultiLineCustomFieldDefinition();
        CustomField customField = CustomFieldFactory.create(multiFieldDefinition.getId());
        customField.setValues(Arrays.asList("V1", "V3"));
        issue.addCustomField(customField);
        Issue createdIssue = issueManager.createIssue(issue);

        CustomField loadedCustomField = createdIssue.getCustomFieldByName("custom_multi_list");
        assertThat(loadedCustomField).isNotNull();
        assertThat(loadedCustomField.getValues().size()).isEqualTo(2);
        List<String> values = new ArrayList<>(loadedCustomField.getValues());
        Collections.sort(values);
        assertThat(loadedCustomField.getValues().get(0)).isEqualTo("V1");
        assertThat(loadedCustomField.getValues().get(1)).isEqualTo("V3");
        issueManager.deleteIssue(createdIssue.getId());
    }

    /**
     * This is to make sure we have a workaround for a known bug in redmine 2.6.
     */
    @Test
    public void createIssueWithEmptyListInMultilineCustomFields() throws Exception {
        Issue newIssue = IssueFactory.create(projectId, "test for custom multi fields - set multiple values");
        CustomFieldDefinition multiFieldDefinition = loadMultiLineCustomFieldDefinition();
        CustomField customField = CustomFieldFactory.create(multiFieldDefinition.getId());
        customField.setValues(Collections.EMPTY_LIST);
        newIssue.addCustomField(customField);
        Issue createdIssue = issueManager.createIssue(newIssue);
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
        Issue issue = createIssues(issueManager, projectId, 1).get(0);
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
        Issue issue = createIssue(issueManager, projectId);
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

            issue = createIssue(managerOnBehalfOfUser.getIssueManager(), projectId);
            assertThat(issue.getAuthor().getFirstName()).isEqualTo(newUser.getFirstName());
            assertThat(issue.getAuthor().getLastName()).isEqualTo(newUser.getLastName());
        } finally {
            userManager.deleteUser(newUser.getId());
            deleteIssueIfNotNull(issue);
        }
    }

    @Test
    public void issuesCanBeFoundByFreeFormSearch() throws RedmineException {
        // create some random issues in the project
        createIssues(issueManager, projectId, 3);

        final String subject = "test for free_form_search.";
        final Issue issueToCreate = IssueFactory.create(projectId, subject);
        Integer createdIssueId = null;
        try {
            createdIssueId = issueManager.createIssue(issueToCreate).getId();

            Map<String, String> params = new HashMap<>();
            params.put("project_id", Integer.toString(projectId));
            params.put("subject", "~free_form_search");
            List<Issue> issues = issueManager.getIssues(params);
            assertThat(issues.size()).isEqualTo(1);
            final Issue loaded = issues.get(0);
            assertThat(loaded.getSubject()).isEqualTo(subject);
        } finally {
            issueManager.deleteIssue(createdIssueId);
        }
    }

    private void deleteIssueIfNotNull(Issue issue) throws RedmineException {
        if (issue != null) {
            issueManager.deleteIssue(issue.getId());
        }
    }
}
