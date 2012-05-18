package org.redmine.ta;

import org.junit.*;
import org.redmine.ta.RedmineManager.INCLUDE;
import org.redmine.ta.beans.*;
import org.redmine.ta.internal.logging.Logger;
import org.redmine.ta.internal.logging.LoggerFactory;

import java.io.IOException;
import java.util.*;

import static org.junit.Assert.*;

/**
 * This class and its dependencies are located in org.redmine.ta.api project.
 */
public class RedmineManagerTest {

    // TODO We don't know activities IDs!
    // see feature request http://www.redmine.org/issues/7506
    private static final Integer ACTIVITY_ID = 8;

    private static final Logger logger = LoggerFactory.getLogger(RedmineManagerTest.class);

    private static RedmineManager mgr;

    private static String projectKey;
    private static TestConfig testConfig;

    @BeforeClass
    public static void oneTimeSetUp() {
        testConfig = new TestConfig();
        logger.info("Running redmine tests using: " + testConfig.getURI());
//		mgr = new RedmineManager(TestConfig.getURI(), TestConfig.getApiKey());
        mgr = new RedmineManager(testConfig.getURI());
        mgr.setLogin(testConfig.getLogin());
        mgr.setPassword(testConfig.getPassword());
        Project junitTestProject = new Project();
        junitTestProject.setName("test project");
        junitTestProject.setIdentifier("test"
                + Calendar.getInstance().getTimeInMillis());

        try {
            Project createdProject = mgr.createProject(junitTestProject);
            projectKey = createdProject.getIdentifier();
        } catch (Exception e) {
            logger.error(e, "Exception while creating test project");
            Assert.fail("can't create a test project. " + e.getMessage());
        }
    }

    @AfterClass
    public static void oneTimeTearDown() {
        try {
            if (mgr != null && projectKey != null) {
                mgr.deleteProject(projectKey);
            }
        } catch (Exception e) {
            logger.error(e, "Exception while deleting test project");
            Assert.fail("can't delete the test project '" + projectKey + ". reason: "
                    + e.getMessage());
        }
    }

    @Test
    public void testCreateIssue() {
        try {
            Issue issueToCreate = new Issue();
            issueToCreate.setSubject("test zzx");

            Calendar startCal = Calendar.getInstance();
            // have to clear them because they are ignored by Redmine and prevent from comparison later
            startCal.clear(Calendar.HOUR_OF_DAY);
            startCal.clear(Calendar.MINUTE);
            startCal.clear(Calendar.SECOND);
            startCal.clear(Calendar.MILLISECOND);

            startCal.add(Calendar.DATE, 5);
            issueToCreate.setStartDate(startCal.getTime());

            Calendar due = Calendar.getInstance();
            due.add(Calendar.MONTH, 1);
            issueToCreate.setDueDate(due.getTime());
            User assignee = getOurUser();
            issueToCreate.setAssignee(assignee);

            String description = "This is the description for the new task." +
                    "\nIt has several lines." +
                    "\nThis is the last line.";
            issueToCreate.setDescription(description);

            float estimatedHours = 44;
            issueToCreate.setEstimatedHours(estimatedHours);

            Issue newIssue = mgr.createIssue(projectKey, issueToCreate);
//			System.out.println("created: " + newIssue);
            Assert.assertNotNull("Checking returned result", newIssue);
            Assert.assertNotNull("New issue must have some ID", newIssue.getId());

            // check startDate
            Calendar returnedStartCal = Calendar.getInstance();
            returnedStartCal.setTime(newIssue.getStartDate());

            Assert.assertEquals(startCal.get(Calendar.YEAR), returnedStartCal.get(Calendar.YEAR));
            Assert.assertEquals(startCal.get(Calendar.MONTH), returnedStartCal.get(Calendar.MONTH));
            Assert.assertEquals(startCal.get(Calendar.DAY_OF_MONTH), returnedStartCal.get(Calendar.DAY_OF_MONTH));

            // check dueDate
            Calendar returnedDueCal = Calendar.getInstance();
            returnedDueCal.setTime(newIssue.getDueDate());

            Assert.assertEquals(due.get(Calendar.YEAR), returnedDueCal.get(Calendar.YEAR));
            Assert.assertEquals(due.get(Calendar.MONTH), returnedDueCal.get(Calendar.MONTH));
            Assert.assertEquals(due.get(Calendar.DAY_OF_MONTH), returnedDueCal.get(Calendar.DAY_OF_MONTH));

            // check ASSIGNEE
            User actualAssignee = newIssue.getAssignee();
            Assert.assertNotNull("Checking assignee not null", actualAssignee);
            Assert.assertEquals("Checking assignee id", assignee.getId(),
                    actualAssignee.getId());

            // check AUTHOR
            Integer EXPECTED_AUTHOR_ID = getOurUser().getId();
            Assert.assertEquals(EXPECTED_AUTHOR_ID, newIssue.getAuthor().getId());

            // check ESTIMATED TIME
            Assert.assertEquals((Float) estimatedHours, newIssue.getEstimatedHours());

            // check multi-line DESCRIPTION
            String regexpStripExtra = "\\r|\\n|\\s";
            description = description.replaceAll(regexpStripExtra, "");
            String actualDescription = newIssue.getDescription();
            actualDescription = actualDescription.replaceAll(regexpStripExtra, "");
            Assert.assertEquals(description, actualDescription);

            // PRIORITY
            Assert.assertNotNull(newIssue.getPriorityId());
            Assert.assertTrue(newIssue.getPriorityId() > 0);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testCreateIssueWithParent() {
        try {
            Issue parentIssue = new Issue();
            parentIssue.setSubject("parent 1");
            Issue newParentIssue = mgr.createIssue(projectKey, parentIssue);
            logger.debug("created parent: " + newParentIssue);

            Assert.assertNotNull("Checking parent was created", newParentIssue);
            Assert.assertNotNull("Checking ID of parent issue is not null",
                    newParentIssue.getId());

            // Integer parentId = 46;
            Integer parentId = newParentIssue.getId();

            Issue childIssue = new Issue();
            childIssue.setSubject("child 1");
            childIssue.setParentId(parentId);

            Issue newChildIssue = mgr.createIssue(projectKey, childIssue);
            logger.debug("created child: " + newChildIssue);

            Assert.assertEquals("Checking parent ID of the child issue", parentId,
                    newChildIssue.getParentId());

        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testStartDateNull() {
        try {
            Issue issue = new Issue();
            issue.setSubject("test start date");
            issue.setStartDate(null);

            Issue newIssue = mgr.createIssue(projectKey, issue);

            Issue loadedIssue = mgr.getIssueById(newIssue.getId());
            Assert.assertNull(loadedIssue.getStartDate());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testGetIssuesBySummary() {
        String summary = "issue with subject ABC";
        try {
            Issue issue = new Issue();
            issue.setSubject(summary);
            User assignee = getOurUser();
            issue.setAssignee(assignee);

            Issue newIssue = mgr.createIssue(projectKey, issue);
            logger.debug("created: " + newIssue);
            Assert.assertNotNull("Checking returned result", newIssue);
            Assert.assertNotNull("New issue must have some ID", newIssue.getId());

            // try to find the issue
            List<Issue> foundIssues = mgr.getIssuesBySummary(projectKey,
                    summary);

            Assert.assertNotNull("Checking if search results is not NULL", foundIssues);
            Assert.assertTrue("Search results must be not empty",
                    !(foundIssues.isEmpty()));

            Issue loadedIssue1 = RedmineTestUtils.findIssueInList(foundIssues, newIssue.getId());
            Assert.assertNotNull(loadedIssue1);
            Assert.assertEquals(summary, loadedIssue1.getSubject());

            // User actualAssignee = newIssue.getAssignee();

            // assertNotNull("Checking assignee not null", actualAssignee);
            // assertEquals("Checking assignee Name", assignee.getName(),
            // actualAssignee.getName());
            // assertEquals("Checking assignee Id", assignee.getId(),
            // actualAssignee.getId());

        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testTryFindNonExistingIssue() {
        String summary = "some summary here for issue which does not exist";
        try {
            // try to find the issue
            List<Issue> foundIssues = mgr.getIssuesBySummary(projectKey,
                    summary);
            Assert.assertNotNull("Search result must be not null", foundIssues);
            Assert.assertTrue("Search result list must be empty",
                    foundIssues.isEmpty());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    private static User getOurUser() {
        Integer userId = Integer
                .parseInt(testConfig.getParam("createissue.userid"));
        String login = testConfig.getLogin();
        String fName = testConfig.getParam("userFName");
        String lName = testConfig.getParam("userLName");
        User user = new User();
        user.setId(userId);
        user.setLogin(login);
        user.setFirstName(fName);
        user.setLastName(lName);
        return user;
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
        RedmineManager redmineMgrInvalidKey = new RedmineManager(testConfig.getURI(), "wrong_key");
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

            Assert.assertEquals(
                    "Checking if 'update issue' operation changed the 'subject' field",
                    changedSubject, reloadedFromRedmineIssue.getSubject());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    /**
     * Tests the retrieval of an {@link Issue} by its ID.
     *
     * @throws RedmineException               thrown in case something went wrong in Redmine
     * @throws IOException                    thrown in case something went wrong while performing I/O
     *                                        operations
     * @throws RedmineAuthenticationException thrown in case something went wrong while trying to login
     * @throws NotFoundException              thrown in case the objects requested for could not be found
     */
    @Test
    public void testGetIssueById() throws RedmineException {
        Issue issue = new Issue();
        String originalSubject = "Issue " + new Date();
        issue.setSubject(originalSubject);

        Issue newIssue = mgr.createIssue(projectKey, issue);

        Issue reloadedFromRedmineIssue = mgr.getIssueById(newIssue.getId());

        Assert.assertEquals(
                "Checking if 'get issue by ID' operation returned issue with same 'subject' field",
                originalSubject, reloadedFromRedmineIssue.getSubject());
        Tracker tracker = reloadedFromRedmineIssue.getTracker();
        Assert.assertNotNull("Tracker of issue should not be null", tracker);
        Assert.assertNotNull("ID of tracker of issue should not be null", tracker.getId());
        Assert.assertNotNull("Name of tracker of issue should not be null", tracker.getName());
    }

    /**
     * Tests the retrieval of {@link Project}s.
     *
     * @throws RedmineException               thrown in case something went wrong in Redmine
     * @throws IOException                    thrown in case something went wrong while performing I/O
     *                                        operations
     * @throws RedmineAuthenticationException thrown in case something went wrong while trying to login
     * @throws NotFoundException              thrown in case the objects requested for could not be found
     */
    @Test
    public void testGetProjects() throws RedmineException {
        // retrieve projects
        List<Project> projects = mgr.getProjects();
        // asserts
        Assert.assertTrue(projects.size() > 0);
        boolean found = false;
        for (Project project : projects) {
            if (project.getIdentifier().equals(projectKey)) {
                found = true;
                break;
            }
        }
        if (!found) {
            Assert.fail("Our project with key '" + projectKey + "' is not found on the server");
        }
    }

    @Test
    public void testGetIssues() {
        try {
            // create at least 1 issue
            Issue issueToCreate = new Issue();
            issueToCreate.setSubject("testGetIssues: " + new Date());
            Issue newIssue = mgr.createIssue(projectKey, issueToCreate);

            List<Issue> issues = mgr.getIssues(projectKey, null);
            logger.debug("getIssues() loaded " + issues.size() + " issues");//using query #" + queryIdIssuesCreatedLast2Days);
            Assert.assertTrue(issues.size() > 0);
            boolean found = false;
            for (Issue issue : issues) {
                if (issue.getId().equals(newIssue.getId())) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                Assert.fail("getIssues() didn't return the issue we just created. The query "
                        + " must have returned all issues created during the last 2 days");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }

    @Test(expected = NotFoundException.class)
    public void testGetIssuesInvalidQueryId() throws RedmineException {
        Integer invalidQueryId = 9999999;
        mgr.getIssues(projectKey, invalidQueryId);
    }

    @Test
    public void testCreateProject() throws RedmineException {
        Project projectToCreate = generateRandomProject();
        String key = null;
        try {
            Project createdProject = mgr.createProject(projectToCreate);
            key = createdProject.getIdentifier();

            Assert.assertNotNull("checking that a non-null project is returned", createdProject);

            Assert.assertEquals(projectToCreate.getIdentifier(), createdProject.getIdentifier());
            Assert.assertEquals(projectToCreate.getName(), createdProject.getName());
            Assert.assertEquals(projectToCreate.getDescription(), createdProject.getDescription());
            Assert.assertEquals(projectToCreate.getHomepage(), createdProject.getHomepage());

            List<Tracker> trackers = createdProject.getTrackers();
            Assert.assertNotNull("checking that project has some trackers", trackers);
            Assert.assertTrue("checking that project has some trackers", !(trackers.isEmpty()));
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        } finally {
            if (key != null) {
                mgr.deleteProject(key);
            }
        }
    }

    @Test
    public void testCreateGetUpdateDeleteProject() throws RedmineException {
        Project projectToCreate = generateRandomProject();
        String key = null;
        try {
            projectToCreate.setIdentifier("id" + new Date().getTime());
            logger.debug("trying to create a project with id " + projectToCreate.getIdentifier());
            Project createdProject = mgr.createProject(projectToCreate);
            key = createdProject.getIdentifier();
            String newDescr = "NEW123";
            String newName = "new name here";

            createdProject.setName(newName);
            createdProject.setDescription(newDescr);
            mgr.update(createdProject);

            Project updatedProject = mgr.getProjectByKey(key);
            Assert.assertNotNull(updatedProject);

            Assert.assertEquals(createdProject.getIdentifier(), updatedProject.getIdentifier());
            Assert.assertEquals(newName, updatedProject.getName());
            Assert.assertEquals(newDescr, updatedProject.getDescription());
            List<Tracker> trackers = updatedProject.getTrackers();
            Assert.assertNotNull("checking that project has some trackers", trackers);
            Assert.assertTrue("checking that project has some trackers", !(trackers.isEmpty()));

        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        } finally {
            if (key != null) {
                mgr.deleteProject(key);
            }
        }
    }

    @Test
    public void createProjectFailsWithReservedIdentifier() throws Exception {
        Project projectToCreate = new Project();
        projectToCreate.setName("new");
        projectToCreate.setIdentifier("new");
        String createdProjectKey = null;
        try {
            Project createdProject = mgr.createProject(projectToCreate);
            // in case if the creation haven't failed (although it should have had!),
            // need to cleanup - delete this project
            createdProjectKey = createdProject.getIdentifier();

        } catch (RedmineProcessingException e) {
            Assert.assertNotNull(e.getErrors());
            Assert.assertEquals(1, e.getErrors().size());
            Assert.assertEquals("Identifier is reserved", e
                    .getErrors().get(0));
        } finally {
            if (createdProjectKey != null) {
                mgr.deleteProject(createdProjectKey);
            }
        }
    }

    private static Project generateRandomProject() {
        Project project = new Project();
        Long timeStamp = Calendar.getInstance().getTimeInMillis();
        String key = "projkey" + timeStamp;
        String name = "project number " + timeStamp;
        String description = "some description for the project";
        project.setIdentifier(key);
        project.setName(name);
        project.setDescription(description);
        project.setHomepage("www.randompage" + timeStamp + ".com");
        return project;
    }

    @Test
    public void testCreateIssueNonUnicodeSymbols() {
        try {
            String nonLatinSymbols = "Example with accents A��o";
            Issue toCreate = new Issue();
            toCreate.setSubject(nonLatinSymbols);
            Issue created = mgr.createIssue(projectKey, toCreate);
            Assert.assertEquals(nonLatinSymbols, created.getSubject());
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testCreateIssueSummaryOnly() {
        try {
            Issue issueToCreate = new Issue();
            issueToCreate.setSubject("This is the summary line 123");

            Issue newIssue = mgr.createIssue(projectKey, issueToCreate);
            Assert.assertNotNull("Checking returned result", newIssue);
            Assert.assertNotNull("New issue must have some ID", newIssue.getId());

            // check AUTHOR
            Integer EXPECTED_AUTHOR_ID = getOurUser().getId();
            Assert.assertEquals(EXPECTED_AUTHOR_ID, newIssue.getAuthor().getId());

        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test(expected = NotFoundException.class)
    public void testCreateIssueInvalidProjectKey() throws RedmineException {
        Issue issueToCreate = new Issue();
        issueToCreate.setSubject("Summary line 100");
        mgr.createIssue("someNotExistingProjectKey", issueToCreate);
    }

    @Test(expected = NotFoundException.class)
    public void testGetProjectNonExistingId() throws RedmineException {
        mgr.getProjectByKey("some-non-existing-key");
    }

    @Test(expected = NotFoundException.class)
    public void testDeleteNonExistingProject() throws RedmineException {
        mgr.deleteProject("some-non-existing-key");
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
    public void testGetUsers() {
        try {
            List<User> users = mgr.getUsers();
            Assert.assertTrue(users.size() > 0);
//			boolean found = false;
//			for (Project project : projects) {
//				if (project.getIdentifier().equals(projectKey)) {
//					found = true;
//					break;
//				}
//			}
//			if (!found) {
//				fail("Our project with key '" + projectKey+"' is not found on the server");
//			}
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testGetCurrentUser() throws RedmineException {
        User currentUser = mgr.getCurrentUser();
        Assert.assertEquals(getOurUser().getId(), currentUser.getId());
        Assert.assertEquals(getOurUser().getLogin(), currentUser.getLogin());
    }

    @Test
    public void testGetUserById() throws RedmineException {
        User loadedUser = mgr.getUserById(getOurUser().getId());
        Assert.assertEquals(getOurUser().getId(), loadedUser.getId());
        Assert.assertEquals(getOurUser().getLogin(), loadedUser.getLogin());
    }

    @Test(expected = NotFoundException.class)
    public void testGetUserNonExistingId() throws RedmineException {
        mgr.getUserById(999999);
    }

    @Test(expected = NotFoundException.class)
    public void testInvalidGetCurrentUser() throws RedmineException {
        RedmineManager invalidManager = new RedmineManager(testConfig.getURI() + "/INVALID");
        invalidManager.setLogin("Invalid");
        invalidManager.setPassword("Invalid");
        invalidManager.getCurrentUser();
    }

    @Test
    public void testCreateUser() throws RedmineException {
        User createdUser = null;
        try {
            User userToCreate = generateRandomUser();
            createdUser = mgr.createUser(userToCreate);

            Assert.assertNotNull("checking that a non-null project is returned", createdUser);

            Assert.assertEquals(userToCreate.getLogin(), createdUser.getLogin());
            Assert.assertEquals(userToCreate.getFirstName(), createdUser.getFirstName());
            Assert.assertEquals(userToCreate.getLastName(), createdUser.getLastName());
            Integer id = createdUser.getId();
            Assert.assertNotNull(id);

        } catch (Exception e) {
            Assert.fail(e.getMessage());
        } finally {
            if (createdUser != null) {
                mgr.deleteUser(createdUser.getId());
            }
        }
    }

    private static User generateRandomUser() {
        User user = new User();
        user.setFirstName("fname");
        user.setLastName("lname");
        long randomNumber = new Date().getTime();
        user.setLogin("login" + randomNumber);
        user.setMail("somemail" + randomNumber + "@somedomain.com");
        user.setPassword("zzzz");

        return user;
    }

    @Test
    public void testUpdateUser() throws RedmineAuthenticationException, NotFoundException {
        User userToCreate = new User();
        userToCreate.setFirstName("fname2");
        userToCreate.setLastName("lname2");
        long randomNumber = new Date().getTime();
        userToCreate.setLogin("login33" + randomNumber);
        userToCreate.setMail("email" + randomNumber + "@somedomain.com");
        userToCreate.setPassword("1234");
        try {
            User createdUser = mgr.createUser(userToCreate);
            Integer userId = createdUser.getId();
            Assert.assertNotNull("checking that a non-null project is returned", createdUser);

            String newFirstName = "fnameNEW";
            String newLastName = "lnameNEW";
            String newMail = "newmail" + randomNumber + "@asd.com";
            createdUser.setFirstName(newFirstName);
            createdUser.setLastName(newLastName);
            createdUser.setMail(newMail);

            mgr.update(createdUser);

            User updatedUser = mgr.getUserById(userId);

            Assert.assertEquals(newFirstName, updatedUser.getFirstName());
            Assert.assertEquals(newLastName, updatedUser.getLastName());
            Assert.assertEquals(newMail, updatedUser.getMail());
            Assert.assertEquals(userId, updatedUser.getId());

        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void userCanBeDeleted() throws RedmineException {
        User user = generateRandomUser();
        User createdUser = mgr.createUser(user);
        Integer newUserId = createdUser.getId();

        try {
            mgr.deleteUser(newUserId);
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
        try {
            mgr.getUserById(newUserId);
            fail("Must have failed with NotFoundException because we tried to delete the user");
        } catch (NotFoundException e) {
            // ignore: the user should not be found
        }
    }

    @Test(expected = NotFoundException.class)
    public void deletingNonExistingUserThrowsNFE() throws RedmineException {
        mgr.deleteUser(999999);
    }

    @Test
    public void testGetIssuesPaging() {
        try {
            // create 27 issues. default page size is 25.
            createIssues(27);
//			mgr.setObjectsPerPage(5); <-- does not work now
            List<Issue> issues = mgr.getIssues(projectKey, null);
            logger.debug("testGetIssuesPaging() loaded " + issues.size() + " issues");//using query #" + queryIdIssuesCreatedLast2Days);
            Assert.assertTrue(issues.size() > 26);

            Set<Issue> issueSet = new HashSet<Issue>(issues);
            Assert.assertEquals(issues.size(), issueSet.size());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
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
    public void testProjectsAllPagesLoaded() throws RedmineException {
        int NUM = 27; // must be larger than 25, which is a default page size in Redmine
        List<Project> projects = createProjects(NUM);

        List<Project> loadedProjects = mgr.getProjects();
        Assert.assertTrue(
                "Number of projects loaded from the server must be bigger than "
                        + NUM + ", but it's " + loadedProjects.size(),
                loadedProjects.size() > NUM);

        deleteProjects(projects);
    }

    private List<Project> createProjects(int num) throws RedmineException {
        List<Project> projects = new ArrayList<Project>(num);
        for (int i = 0; i < num; i++) {
            Project projectToCreate = generateRandomProject();
            Project p = mgr.createProject(projectToCreate);
            projects.add(p);
        }
        return projects;
    }

    private void deleteProjects(List<Project> projects) throws RedmineException {
        for (Project p : projects) {
            mgr.deleteProject(p.getIdentifier());
        }
    }

    @Test
    public void testGetTimeEntries() throws RedmineException {
        List<TimeEntry> list = mgr.getTimeEntries();
        Assert.assertNotNull(list);
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

        Assert.assertNotNull(createdEntry);
        logger.debug("Created time entry " + createdEntry);
        Assert.assertEquals(hours, createdEntry.getHours());

        Float newHours = 22f;
        createdEntry.setHours(newHours);

        mgr.update(createdEntry);

        TimeEntry updatedEntry = mgr.getTimeEntry(createdEntry.getId());
        Assert.assertEquals(newHours, updatedEntry.getHours());
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
        Assert.assertNotNull(createdEntry);

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
        Assert.assertNotNull(createdEntry1);
        Assert.assertNotNull(createdEntry2);

        List<TimeEntry> entries = mgr.getTimeEntriesForIssue(issueId);
        Assert.assertEquals(2, entries.size());
        Float totalTime = 0f;
        for (TimeEntry timeEntry : entries) {
            totalTime += timeEntry.getHours();
        }
        Assert.assertEquals(totalHoursExpected, totalTime);
    }

    private TimeEntry createTimeEntry(Integer issueId, float hours) throws RedmineException {
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
        Assert.assertEquals(issue, retrievedIssue);

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
        Assert.assertEquals(newSubject, updatedIssue.getSubject());
        Assert.assertEquals(newDescription, updatedIssue.getDescription());
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
        Assert.assertEquals(2, issue.getCustomFields().size());

        // TODO update this!
        int id1 = 1; // TODO this is pretty much a hack, we don't generally know these ids!
        String custom1FieldName = "my_custom_1";
        String custom1Value = "some value 123";

        int id2 = 2;
        String custom2FieldName = "custom_boolean_1";
        String custom2Value = "true";

        issue.setCustomFields(new ArrayList<CustomField>());

        issue.getCustomFields().add(new CustomField(id1, custom1FieldName, custom1Value));
        issue.getCustomFields().add(new CustomField(id2, custom2FieldName, custom2Value));
        mgr.update(issue);

        Issue updatedIssue = mgr.getIssueById(issue.getId());
        Assert.assertEquals(2, updatedIssue.getCustomFields().size());
        Assert.assertEquals(custom1Value, updatedIssue.getCustomField(custom1FieldName));
        Assert.assertEquals(custom2Value, updatedIssue.getCustomField(custom2FieldName));
    }

    @Test
    public void testUpdateIssueDoesNotChangeEstimatedTime() {
        try {
            Issue issue = new Issue();
            String originalSubject = "Issue " + new Date();
            issue.setSubject(originalSubject);

            Issue newIssue = mgr.createIssue(projectKey, issue);
            Assert.assertEquals("Estimated hours must be NULL", null, newIssue.getEstimatedHours());

            mgr.update(newIssue);

            Issue reloadedFromRedmineIssue = mgr.getIssueById(newIssue.getId());
            Assert.assertEquals("Estimated hours must be NULL", null, reloadedFromRedmineIssue.getEstimatedHours());
        } catch (Exception e) {
            Assert.fail();
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
    public void testSubProjectIsCreatedWithCorrectParentId() throws RedmineException {
        Project createdMainProject = null;
        try {
            createdMainProject = createProject();
            Project subProject = createSubProject(createdMainProject);
            Assert.assertEquals("Must have correct parent ID",
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
            Assert.assertEquals("Initial 'done ratio' must be 0", (Integer) 0, createdIssue.getDoneRatio());
            Integer doneRatio = 50;
            createdIssue.setDoneRatio(doneRatio);
            mgr.update(createdIssue);

            Integer issueId = createdIssue.getId();
            Issue reloadedFromRedmineIssue = mgr.getIssueById(issueId);
            Assert.assertEquals(
                    "Checking if 'update issue' operation changed 'done ratio' field",
                    doneRatio, reloadedFromRedmineIssue.getDoneRatio());

            Integer invalidDoneRatio = 130;
            reloadedFromRedmineIssue.setDoneRatio(invalidDoneRatio);
            try {
                mgr.update(reloadedFromRedmineIssue);
            } catch (RedmineProcessingException e) {
                Assert.assertEquals("Must be 1 error", 1, e.getErrors().size());
                Assert.assertEquals("Checking error text",
                        "% Done is not included in the list", e
                        .getErrors().get(0));
            }

            Issue reloadedFromRedmineIssueUnchanged = mgr.getIssueById(issueId);
            Assert.assertEquals(
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
            Assert.assertEquals("Checking description", descr, createdIssue.getDescription());

            createdIssue.setDescription(null);
            mgr.update(createdIssue);

            Integer issueId = createdIssue.getId();
            Issue reloadedFromRedmineIssue = mgr.getIssueById(issueId);
            Assert.assertEquals(
                    "Description must not be erased",
                    descr, reloadedFromRedmineIssue.getDescription());

            reloadedFromRedmineIssue.setDescription("");
            mgr.update(reloadedFromRedmineIssue);

            Issue reloadedFromRedmineIssueUnchanged = mgr.getIssueById(issueId);
            Assert.assertEquals(
                    "Description must be erased",
                    "", reloadedFromRedmineIssueUnchanged.getDescription());
        } catch (Exception e) {
            Assert.fail();
        }
    }

    @Test
    public void testIssueJournals() {
        try {
            // create at least 1 issue
            Issue issueToCreate = new Issue();
            issueToCreate.setSubject("testGetIssues: " + new Date());
            Issue newIssue = mgr.createIssue(projectKey, issueToCreate);

            Issue loadedIssueWithJournals = mgr.getIssueById(newIssue.getId(), INCLUDE.journals);
            Assert.assertTrue(loadedIssueWithJournals.getJournals().isEmpty());

            String commentDescribingTheUpdate = "some comment describing the issue update";
            loadedIssueWithJournals.setSubject("new subject");
            loadedIssueWithJournals.setNotes(commentDescribingTheUpdate);
            mgr.update(loadedIssueWithJournals);

            Issue loadedIssueWithJournals2 = mgr.getIssueById(newIssue.getId(), INCLUDE.journals);
            Assert.assertEquals(1, loadedIssueWithJournals2.getJournals().size());

            Journal journalItem = loadedIssueWithJournals2.getJournals().get(0);
            Assert.assertEquals(commentDescribingTheUpdate, journalItem.getNotes());
            User ourUser = getOurUser();
            // can't compare User objects because either of them is not completely filled
            Assert.assertEquals(ourUser.getId(), journalItem.getUser().getId());
            Assert.assertEquals(ourUser.getFirstName(), journalItem.getUser().getFirstName());
            Assert.assertEquals(ourUser.getLastName(), journalItem.getUser().getLastName());

            Issue loadedIssueWithoutJournals = mgr.getIssueById(newIssue.getId());
            Assert.assertTrue(loadedIssueWithoutJournals.getJournals().isEmpty());

        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testCreateRelation() {
        try {
            List<Issue> issues = createIssues(2);
            Issue src = issues.get(0);
            Issue target = issues.get(1);

            String relationText = IssueRelation.TYPE.precedes.toString();
            IssueRelation r = mgr.createRelation(src.getId(), target.getId(), relationText);
            assertEquals(src.getId(), r.getIssueId());
            Assert.assertEquals(target.getId(), r.getIssueToId());
            Assert.assertEquals(relationText, r.getType());
        } catch (Exception e) {
            Assert.fail(e.toString());
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
            Issue issue = mgr.getIssueById(relation.getIssueId(), INCLUDE.relations);
            Issue issueTarget = mgr.getIssueById(relation.getIssueToId(), INCLUDE.relations);

            Assert.assertEquals(1, issue.getRelations().size());
            Assert.assertEquals(1, issueTarget.getRelations().size());

            IssueRelation relation1 = issue.getRelations().get(0);
            assertEquals(issue.getId(), relation1.getIssueId());
            assertEquals(issueTarget.getId(), relation1.getIssueToId());
            assertEquals("precedes", relation1.getType());
            assertEquals((Integer) 0, relation1.getDelay());

            IssueRelation reverseRelation = issueTarget.getRelations().get(0);
            // both forward and reverse relations are the same!
            Assert.assertEquals(relation1, reverseRelation);
        } catch (Exception e) {
            Assert.fail(e.toString());
        }
    }

    @Test
    public void testIssureRelationDelete() throws RedmineException {
        IssueRelation relation = createTwoRelatedIssues();

        mgr.deleteRelation(relation.getId());
        Issue issue = mgr.getIssueById(relation.getIssueId(), INCLUDE.relations);
        Assert.assertEquals(0, issue.getRelations().size());
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
        Assert.assertEquals(0, issue.getRelations().size());
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

        Assert.assertNotNull(createdIssue.getTargetVersion());
        Assert.assertEquals(createdIssue.getTargetVersion().getName(), versionName);
    }

    // Redmine ignores this parameter for "get projects" request. see bug http://www.redmine.org/issues/8545
    @Ignore
    @Test
    public void testGetProjectsIncludesTrackers() {
        try {
            List<Project> projects = mgr.getProjects();
            Assert.assertTrue(projects.size() > 0);
            Project p1 = projects.get(0);
            Assert.assertNotNull(p1.getTrackers());
            for (Project p : projects)
                if (!p.getTrackers().isEmpty())
                    return;
            Assert.fail("No projects with trackers found");
            logger.debug("Created trackers " + p1.getTrackers());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }

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
            Assert.assertEquals((Float) spentHours, newIssue.getSpentHours());
        } catch (Exception e) {
            Assert.fail();
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
    public void testViolateTimeEntryConstraint_ProjectOrIssueID_issue66() throws RedmineException {
        TimeEntry timeEntry = createIncompleteTimeEntry();
        // Now can try to verify with project ID (only test with issue ID seems to be already covered)
        int projectId = mgr.getProjects().get(0).getId();
        timeEntry.setProjectId(projectId);
        try {
            TimeEntry created = mgr.createTimeEntry(timeEntry);
            logger.debug("Created time entry " + created);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Unexpected " + e.getClass().getSimpleName() + ": " + e.getMessage());
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
        // TODO we should create some statuses first, but the Redmine Java API does not support this presently
        List<IssueStatus> statuses = mgr.getStatuses();
        Assert.assertFalse("Expected list of statuses not to be empty", statuses.isEmpty());
        for (IssueStatus issueStatus : statuses) {
            // asserts on status
            assertNotNull("ID of status must not be null", issueStatus.getId());
            assertNotNull("Name of status must not be null", issueStatus.getName());
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
        Version version = new Version(null, "Invalid test version " + UUID.randomUUID().toString());
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
        Version version = new Version(null, "Invalid test version " + UUID.randomUUID().toString());
        version.setDescription("An invalid test version created by " + this.getClass());
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
        Project project = mgr.getProjectByKey(projectKey);
        // create new test version
        Version version = new Version(project, "Test version " + UUID.randomUUID().toString());
        version.setDescription("A test version created by " + this.getClass());
        version.setStatus("open");
        Version newVersion = mgr.createVersion(version);
        // assert new test version
        Assert.assertNotNull("Expected new version not to be null", newVersion);
        // now delete version
        mgr.deleteVersion(newVersion);
        // assert that the version is gone
        List<Version> versions = mgr.getVersions(project.getId());
        Assert.assertTrue("List of versions of test project must be empty now but is " + versions, versions.isEmpty());
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
        Project project = mgr.getProjectByKey(projectKey);
        // create some versions
        Version testVersion1 = mgr.createVersion(new Version(project, "Version" + UUID.randomUUID()));
        Version testVersion2 = mgr.createVersion(new Version(project, "Version" + UUID.randomUUID()));
        try {
            List<Version> versions = mgr.getVersions(project.getId());
            Assert.assertEquals("Wrong number of versions for project " + project.getName() + " delivered by Redmine Java API", 2, versions.size());
            for (Version version : versions) {
                // assert version
                Assert.assertNotNull("ID of version must not be null", version.getId());
                Assert.assertNotNull("Name of version must not be null", version.getName());
                Assert.assertNotNull("Project of version must not be null", version.getProject());
            }
        } finally {
            if (testVersion1 != null) {
                mgr.deleteVersion(testVersion1);
            }
            if (testVersion2 != null) {
                mgr.deleteVersion(testVersion2);
            }
        }
    }

    @Ignore
    // see Redmine bug http://www.redmine.org/issues/10241
    @Test
    public void versionIsRetrievedById() throws RedmineException {
        Project project = mgr.getProjectByKey(projectKey);
        Version createdVersion = mgr.createVersion(new Version(project, "Version_1_" + UUID.randomUUID()));
        Version versionById = mgr.getVersionById(createdVersion.getId());
        assertEquals(createdVersion, versionById);
    }

    @Ignore // see Redmine bug http://www.redmine.org/issues/10241
    @Test
    public void versionIsUpdated() throws RedmineException {
        Project project = mgr.getProjectByKey(projectKey);
        Version createdVersion = mgr.createVersion(new Version(project, "Version_1_" + UUID.randomUUID()));
        String description = "new description";
        createdVersion.setDescription(description);
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
        // create new test category
        IssueCategory category = new IssueCategory(project, "Category" + new Date().getTime());
        category.setAssignee(getOurUser());
        IssueCategory newIssueCategory = mgr.createCategory(category);
        // assert new test category
        Assert.assertNotNull("Expected new category not to be null", newIssueCategory);
        Assert.assertNotNull("Expected project of new category not to be null", newIssueCategory.getProject());
        Assert.assertNotNull("Expected assignee of new category not to be null", newIssueCategory.getAssignee());
        // now delete category
        mgr.deleteCategory(newIssueCategory);
        // assert that the category is gone
        List<IssueCategory> categories = mgr.getCategories(project.getId());
        Assert.assertTrue("List of categories of test project must be empty now but is " + categories, categories.isEmpty());
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
        IssueCategory testIssueCategory1 = new IssueCategory(project, "Category" + new Date().getTime());
        testIssueCategory1.setAssignee(getOurUser());
        IssueCategory newIssueCategory1 = mgr.createCategory(testIssueCategory1);
        IssueCategory testIssueCategory2 = new IssueCategory(project, "Category" + new Date().getTime());
        testIssueCategory2.setAssignee(getOurUser());
        IssueCategory newIssueCategory2 = mgr.createCategory(testIssueCategory2);
        try {
            List<IssueCategory> categories = mgr.getCategories(project.getId());
            Assert.assertEquals("Wrong number of categories for project " + project.getName() + " delivered by Redmine Java API", 2, categories.size());
            for (IssueCategory category : categories) {
                // assert category
                Assert.assertNotNull("ID of category must not be null", category.getId());
                Assert.assertNotNull("Name of category must not be null", category.getName());
                Assert.assertNotNull("Project of category must not be null", category.getProject());
                Assert.assertNotNull("Assignee of category must not be null", category.getAssignee());
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
        IssueCategory category = new IssueCategory(null, "InvalidCategory" + new Date().getTime());
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
        IssueCategory category = new IssueCategory(null, "InvalidCategory" + new Date().getTime());
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
        assertFalse("List of trackers returned should not be empty", trackers.isEmpty());
        for (Tracker tracker : trackers) {
            assertNotNull("Tracker returned should not be null", tracker);
            assertNotNull("ID of tracker returned should not be null", tracker.getId());
            assertNotNull("Name of tracker returned should not be null", tracker.getName());
        }
    }

    /**
     * Tests the retrieval of an {@link Issue}, inlcuding the {@link org.redmine.ta.beans.Attachment}s.
     *
     * @throws RedmineException               thrown in case something went wrong in Redmine
     * @throws IOException                    thrown in case something went wrong while performing I/O
     *                                        operations
     * @throws RedmineAuthenticationException thrown in case something went wrong while trying to login
     * @throws NotFoundException              thrown in case the objects requested for could not be found
     */
    @Test
    public void testGetIssueWithAttachments() throws RedmineException {
        Issue newIssue = null;
        try {
            // create at least 1 issue
            Issue issueToCreate = new Issue();
            issueToCreate.setSubject("testGetIssueAttachment_" + UUID.randomUUID());
            newIssue = mgr.createIssue(projectKey, issueToCreate);
            // TODO create test attachments for the issue once the Redmine REST API allows for it
            // retrieve issue attachments
            Issue retrievedIssue = mgr.getIssueById(newIssue.getId(), INCLUDE.attachments);
            Assert.assertNotNull("List of attachments retrieved for issue " + newIssue.getId() + " delivered by Redmine Java API should not be null", retrievedIssue.getAttachments());
            // TODO assert attachments once we actually receive ones for our test issue
        } finally {
            // scrub test issue
            if (newIssue != null) {
                mgr.deleteIssue(newIssue.getId());
            }
        }
    }

    /**
     * Tests the retrieval of an {@link org.redmine.ta.beans.Attachment} by its ID.
     * TODO reactivate once the Redmine REST API allows for creating attachments
     *
     * @throws RedmineException               thrown in case something went wrong in Redmine
     * @throws IOException                    thrown in case something went wrong while performing I/O
     *                                        operations
     * @throws RedmineAuthenticationException thrown in case something went wrong while trying to login
     * @throws NotFoundException              thrown in case the objects requested for could not be found
     */
    // @Test
    public void testGetAttachmentById() throws RedmineException {
        // TODO where do we get a valid attachment number from? We can't create an attachment by our own for the test as the Redmine REST API does not support that.
        int attachmentID = 1;
        Attachment attachment = mgr.getAttachmentById(attachmentID);
        Assert.assertNotNull("Attachment retrieved by ID " + attachmentID + " should not be null", attachment);
        Assert.assertNotNull("Content URL of attachment retrieved by ID " + attachmentID + " should not be null", attachment.getContentURL());
        // TODO more asserts on the attachment once this delivers an attachment
    }

    /**
     * Tests the download of the content of an {@link org.redmine.ta.beans.Attachment}.
     * TODO reactivate once the Redmine REST API allows for creating attachments
     *
     * @throws RedmineException               thrown in case something went wrong in Redmine
     * @throws IOException                    thrown in case something went wrong while performing I/O
     *                                        operations
     * @throws RedmineAuthenticationException thrown in case something went wrong while trying to login
     * @throws NotFoundException              thrown in case the objects requested for could not be found
     */
    // @Test
    public void testDownloadAttachmentContent() throws RedmineException {
        // TODO where do we get a valid attachment number from? We can't create an attachment by our own for the test as the Redmine REST API does not support that.
        int attachmentID = 1;
        // retrieve issue attachment
        Attachment attachment = mgr.getAttachmentById(attachmentID);
        // download  attachment content
        byte[] attachmentContent = mgr.downloadAttachmentContent(attachment);
        Assert.assertNotNull("Download of content of attachment with content URL " + attachment.getContentURL() + " should not be null", attachmentContent);
    }

    /**
     * Tests the creation and retrieval of an  {@link org.redmine.ta.beans.Issue} with a {@link IssueCategory}.
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
            IssueCategory category = new IssueCategory(project, "Category_" + new Date().getTime());
            category.setAssignee(getOurUser());
            newIssueCategory = mgr.createCategory(category);
            // create an issue
            Issue issueToCreate = new Issue();
            issueToCreate.setSubject("testGetIssueWithCategory_" + UUID.randomUUID());
            issueToCreate.setCategory(newIssueCategory);
            newIssue = mgr.createIssue(projectKey, issueToCreate);
            // retrieve issue 
            Issue retrievedIssue = mgr.getIssueById(newIssue.getId());
            // assert retrieved category of issue
            IssueCategory retrievedCategory = retrievedIssue.getCategory();
            Assert.assertNotNull("Category retrieved for issue " + newIssue.getId() + " should not be null", retrievedCategory);
            Assert.assertEquals("ID of category retrieved for issue " + newIssue.getId() + " is wrong", newIssueCategory.getId(), retrievedCategory.getId());
            Assert.assertEquals("Name of category retrieved for issue " + newIssue.getId() + " is wrong", newIssueCategory.getName(), retrievedCategory.getName());
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
    public void getSavedQueriesDoesNotFailForTempProject() throws RedmineException {
        mgr.getSavedQueries(projectKey);
    }

    @Test
    public void getSavedQueriesDoesNotFailForNULLProject() throws RedmineException {
        mgr.getSavedQueries(null);
    }

    @Test
    public void emptyDescriptionReturnedAsEmptyString() throws RedmineException {
        Issue issue = new Issue();
        String subject = "Issue " + new Date();
        issue.setSubject(subject);

        Issue createdIssue = mgr.createIssue(projectKey, issue);
        assertEquals("Description must be an empty string, not NULL", "", createdIssue.getDescription());
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
	public void tryUpdateProjectWithLongHomepage() throws RedmineException {
		final Project project = new Project();
		project.setIdentifier("issue7testproject");
		project.setName("issue 7 test project");
		project.setDescription("test");
		final String longHomepageName = "http://www.localhost.com/asdf?a=\"&b=\"&c=\"&d=\"&e=\"&f=\"&g=\"&h=\"&i=\"&j=\"&k=\"&l=\"&m=\"&n=\"&o=\"&p=\"&q=\"&r=\"&s=\"&t=\"&u=\"&v=\"&w=\"&x=\"&y=\"&zо=авфбвоафжывлдаофжывладоджлфоывадлфоываждфлоываждфлоываждлфоываждлфова&&\\&&&&&&&&&&&&&&&&&&\\&&&&&&&&&&&&&&&&&&&&&&&&&&&&<>>";
		project.setHomepage(longHomepageName);
		final Project created = mgr.createProject(project);
		created.setDescription("updated description");
		try {
			mgr.update(created);
			final Project updated = mgr
					.getProjectByKey(project.getIdentifier());
			Assert.assertEquals(longHomepageName, updated.getHomepage());
		} finally {
			mgr.deleteProject(created.getIdentifier());
		}
	}

	@Test
	public void testAttachementUploads() throws RedmineException, IOException {
		final byte[] content = new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
		final Attachment attach1 = mgr.uploadAttachment("test.bin",
				"application/ternary", content);
		final Issue testIssue = new Issue();
		testIssue.setSubject("This is upload ticket!");
		testIssue.getAttachments().add(attach1);
		final Issue createdIssue = mgr.createIssue(projectKey, testIssue);
		try {
			final List<Attachment> attachments = createdIssue.getAttachments();
			Assert.assertEquals(1, attachments.size());
			final Attachment added = attachments.get(0);
			Assert.assertEquals("test.bin", added.getFileName());
			Assert.assertEquals("application/ternary", added.getContentType());
			// FIXME: fix "download attachment content" and enable content check
			// final byte[] receivedContent =
			// mgr.downloadAttachmentContent(added);
			// Assert.assertArrayEquals(content, receivedContent);
		} finally {
			mgr.deleteIssue(createdIssue.getId());
		}
	}
}
