package org.redmine.ta;

import org.junit.*;
import org.redmine.ta.RedmineManager.INCLUDE;
import org.redmine.ta.beans.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;

/**
 * This class and its dependencies are located in org.redmine.ta.api project.
 */
public class RedmineManagerTest {

	// TODO We don't know activities IDs!
	// see feature request http://www.redmine.org/issues/7506
	private static final Integer ACTIVITY_ID = 8;

	private static RedmineManager mgr;

	private static String projectKey;
    private static TestConfig testConfig;

	@BeforeClass
	public static void oneTimeSetUp() {
        testConfig = new TestConfig();
		System.out.println("Running redmine tests using: " + testConfig.getURI());
//		mgr = new RedmineManager(TestConfig.getURI(), TestConfig.getApiKey());
		mgr = new RedmineManager(testConfig.getURI(), testConfig.getLogin(), testConfig.getPassword());
		Project junitTestProject = new Project();
		junitTestProject.setName("test project");
		junitTestProject.setIdentifier("test"
				+ Calendar.getInstance().getTimeInMillis());
		try {
			Project createdProject = mgr.createProject(junitTestProject);
			projectKey = createdProject.getIdentifier();
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail("can't create a test project" + e.getMessage());
		}
	}

	@AfterClass
	public static void oneTimeTearDown() {
		try {
			if (mgr != null && projectKey != null) {
				mgr.deleteProject(projectKey);
			}
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail("can't delete the test project '" + projectKey + ". reason: "
                    + e.getMessage());
		}
	}
      
	@Before
	// Is executed before each test method
	public void setup() throws Exception {
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
			Integer EXPECTED_AUTHOR_ID  = getOurUser().getId();
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
			System.out.println("created parent: " + newParentIssue);

			Assert.assertNotNull("Checking parent was created", newParentIssue);
			Assert.assertNotNull("Checking ID of parent issue is not null",
                    newParentIssue.getId());

			// Integer parentId = 46;
			Integer parentId = newParentIssue.getId();

			Issue childIssue = new Issue();
			childIssue.setSubject("child 1");
			childIssue.setParentId(parentId);

			Issue newChildIssue = mgr.createIssue(projectKey, childIssue);
			System.out.println("created child: " + newChildIssue);

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
			System.out.println("created: " + newIssue);
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

	@Test
	public void testNULLHostParameter() throws RuntimeException {
		try {
			new RedmineManager(null, null); 
			Assert.fail("Must have failed with IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			System.out.println("Got expected IllegalArgumentException.");
		} catch (Exception e) {
			Assert.fail("Got unexpected exception : " + e);
		}
		
	}

	@Test
	public void testEmptyHostParameter() throws RuntimeException {
		try {
			new RedmineManager("", null); 
			Assert.fail("Must have failed with IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			System.out.println("Got expected IllegalArgumentException.");
		} catch (Exception e) {
			Assert.fail("Got unexpected exception : " + e);
		}
		
	}
	
	@Test
	public void testWrongCredentialsOnCreateIssue() throws RuntimeException {
		
		RedmineManager redmineMgrEmpty = new RedmineManager(testConfig.getURI(), null);

		// NO API access key set
		Issue issue = new Issue();
		issue.setSubject("test zzx");
		try {
			redmineMgrEmpty.createIssue(projectKey, issue);
			Assert.fail("Must have failed with '401 Not authorized'");
		} catch (AuthenticationException e) {
			System.out.println("Got expected AuthenticationException.");
		} catch (Exception e) {
			Assert.fail("Got unexpected exception : " + e);
		}
		
		// set invalid API access key
		RedmineManager redmineMgrInvalidKey = new RedmineManager(testConfig.getURI(), "wrong_key");
		try {
			redmineMgrInvalidKey.createIssue(projectKey, issue);
			Assert.fail("Must have failed with '401 Not authorized'");
		} catch (AuthenticationException e) {
			System.out.println("Got expected AuthenticationException.");
		} catch (Exception e) {
			Assert.fail("Got unexpected exception : " + e);
		}

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

			mgr.updateIssue(newIssue);

			Issue reloadedFromRedmineIssue = mgr.getIssueById(newIssue.getId());

			Assert.assertEquals(
                    "Checking if 'update issue' operation changed the 'subject' field",
                    changedSubject, reloadedFromRedmineIssue.getSubject());
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail();
		}
	}

	@Test
	public void testGetIssueById() {
		try {
			Issue issue = new Issue();
			String originalSubject = "Issue " + new Date();
			issue.setSubject(originalSubject);

			Issue newIssue = mgr.createIssue(projectKey, issue);

			Issue reloadedFromRedmineIssue = mgr.getIssueById(newIssue.getId());

			Assert.assertEquals(
                    "Checking if 'get issue by ID' operation returned issue with same 'subject' field",
                    originalSubject, reloadedFromRedmineIssue.getSubject());

		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail();
		}
	}
	
	@Test
	public void testGetProjects() {
		try {
			List<Project> projects = mgr.getProjects();
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
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
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
			System.out.println("getIssues() loaded " + issues.size() + " issues");//using query #" + queryIdIssuesCreatedLast2Days);
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
	
	@Test
	public void testGetIssuesInvalidQueryId() {
		try {
			Integer invalidQueryId = 9999999;
			mgr.getIssues(projectKey, invalidQueryId);
			Assert.fail("Must have failed with NotFoundException because query ID is invalid");
		} catch (NotFoundException e) {
			System.out.println("Got expected NotFoundException:" + e.getMessage());
		} catch (Exception e) {
			Assert.fail(e.getMessage());
		}
	}
	
	@Test
	public void testCreateProject() throws IOException, AuthenticationException, NotFoundException, RedmineException {
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
	public void testCreateGetUpdateDeleteProject() throws IOException, AuthenticationException, NotFoundException, RedmineException {
		Project projectToCreate = generateRandomProject();
		String key = null;
		try {
			projectToCreate.setIdentifier("id" + new Date().getTime());
			System.out.println("trying to create a project with id " + projectToCreate.getIdentifier());
			Project createdProject = mgr.createProject(projectToCreate);
			key = createdProject.getIdentifier();
			String newDescr = "NEW123";
			String newName = "new name here";
			
			createdProject.setName(newName);
			createdProject.setDescription(newDescr);
			mgr.updateProject(createdProject);
			
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
	public void testCreateProjectFailsWithReservedIdentifier() throws Exception {
		Project projectToCreate = new Project();
		projectToCreate.setName("new");
		projectToCreate.setIdentifier("new");
		String key = null;
		try {
			Project createdProject = mgr.createProject(projectToCreate);
			// in case if the creation haven't failed (although it should have had!),
			// need to cleanup - delete this project 
			key = createdProject.getIdentifier();
			
		} catch (RedmineException e) {
			Assert.assertNotNull(e.getErrors());
			Assert.assertEquals(1, e.getErrors().size());
			Assert.assertEquals("Identifier is reserved", e.getErrors().get(0));
		} finally {
			if (key != null) {
				mgr.deleteProject(key);
			}
		}
	}

	private static Project generateRandomProject() {
		Project project = new Project();
		Long timeStamp = Calendar.getInstance().getTimeInMillis();
		String key = "projkey" + timeStamp;
		String name = "project number "+ timeStamp;
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
			Integer EXPECTED_AUTHOR_ID  = getOurUser().getId();
			Assert.assertEquals(EXPECTED_AUTHOR_ID, newIssue.getAuthor().getId());

		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail();
		}
	}

	@Test
	public void testCreateIssueInvalidProjectKey() {
		try {
			Issue issueToCreate = new Issue();
			issueToCreate.setSubject("Summary line 100");
			String invalidProjectKey = "someNotExistingProjectKey";
			mgr.createIssue(invalidProjectKey, issueToCreate);
			
			Assert.fail("Must have failed with NotFoundException because we provided invalid project key.");
		} catch (NotFoundException e) {
			System.out.println("Got expected NotFoundException: " + e.getMessage());
		} catch (Exception e) {
			Assert.fail();
		}
	}

	@Test
	public void testGetProjectNonExistingId() {
		try {
			mgr.getProjectByKey("some-non-existing-key");
			Assert.fail("Must have failed with NotFoundException");
		} catch (NotFoundException e) {
			System.out.println("Got expected NotFoundException.");
		} catch (Exception e) {
			Assert.fail(e.getMessage());
		}
	}
	
	@Test
	public void testDeleteNonExistingProject() {
		try {
			mgr.deleteProject("some-non-existing-key");
			Assert.fail("Must have failed with NotFoundException");
		} catch (NotFoundException e) {
			System.out.println("Got expected NotFoundException.");
		} catch (Exception e) {
			Assert.fail(e.getMessage());
		}
	}
	
	@Test
	public void testGetIssueNonExistingId() {
		try {
			int someNonExistingID = 999999;
			mgr.getIssueById(someNonExistingID);
			Assert.fail("Must have failed with NotFoundException.");
		} catch (NotFoundException e) {
			System.out.println("Got expected NotFoundException.");
		} catch (Exception e) {
			Assert.fail();
		}
	}

	@Test
	public void testUpdateIssueNonExistingId() {
		try {
			int nonExistingId = 999999;
			Issue issue = new Issue();
			issue.setId(nonExistingId);
			mgr.updateIssue(issue);
			Assert.fail("Must have failed with NotFoundException because we provided invalid issue ID.");
		} catch (NotFoundException e) {
			System.out.println("Got expected NotFoundException.");
		} catch (Exception e) {
			Assert.fail();
		}
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
	public void testGetCurrentUser() throws IOException, AuthenticationException, RedmineException {
		User currentUser = mgr.getCurrentUser();
		Assert.assertEquals(getOurUser().getId(), currentUser.getId());
		Assert.assertEquals(getOurUser().getLogin(), currentUser.getLogin());
	}
	
	@Test
	public void testGetUserById() throws IOException, AuthenticationException, NotFoundException, RedmineException {
		User loadedUser = mgr.getUserById(getOurUser().getId());
		Assert.assertEquals(getOurUser().getId(), loadedUser.getId());
		Assert.assertEquals(getOurUser().getLogin(), loadedUser.getLogin());
	}
	
	@Test
	public void testGetUserNonExistingId() throws IOException, AuthenticationException, RedmineException {
		try {
			mgr.getUserById(999999);
			Assert.fail("Must have failed above");
		} catch (NotFoundException e) {
			System.out.println("Got expected NotFoundException");
		}
	}
	
	@Test
	public void testCreateUser() throws IOException, AuthenticationException, NotFoundException {
		try {
			User userToCreate = generateRandomUser();
			User createdUser = mgr.createUser(userToCreate );
			
			Assert.assertNotNull("checking that a non-null project is returned", createdUser);
			
			Assert.assertEquals(userToCreate.getLogin(), createdUser.getLogin());
			Assert.assertEquals(userToCreate.getFirstName(), createdUser.getFirstName());
			Assert.assertEquals(userToCreate.getLastName(), createdUser.getLastName());
			Integer id = createdUser.getId();
			Assert.assertNotNull(id);
			
		} catch (Exception e) {
			Assert.fail(e.getMessage());
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
	public void testUpdateUser() throws IOException, AuthenticationException, NotFoundException {
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
			String newMail = "newmail"+randomNumber+"@asd.com";
			createdUser.setFirstName(newFirstName);
			createdUser.setLastName(newLastName);
			createdUser.setMail(newMail);
			
			mgr.updateUser(createdUser);
			
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
	public void testGetIssuesPaging() {
		try {
			// create 27 issues. default page size is 25.
			createIssues(27);
//			mgr.setObjectsPerPage(5); <-- does not work now
			List<Issue> issues = mgr.getIssues(projectKey, null);
			System.out.println("testGetIssuesPaging() loaded " + issues.size() + " issues");//using query #" + queryIdIssuesCreatedLast2Days);
			Assert.assertTrue(issues.size() > 26);

            Set<Issue> issueSet = new HashSet<Issue>(issues);
            Assert.assertEquals(issues.size(), issueSet.size());
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
	}

	private List<Issue> createIssues(int issuesNumber) throws IOException, AuthenticationException, NotFoundException, RedmineException {
		List<Issue> issues = new ArrayList<Issue>(issuesNumber);
		for (int i=0; i<issuesNumber; i++){
			Issue issueToCreate = new Issue();
			issueToCreate.setSubject("some issue " + i + " " + new Date());
			Issue issue = mgr.createIssue(projectKey, issueToCreate);
			issues.add(issue);
		}
		return issues;
	}

	private Issue createIssue() throws IOException, AuthenticationException, NotFoundException, RedmineException {
		List<Issue> createIssues = createIssues(1);
		return createIssues.get(0);
	}
	
	private Issue generateRandomIssue() {
		Random r = new Random();
		Issue issue = new Issue();
		issue.setSubject("some issue " + r.nextInt() + " " + new Date());
		return issue;
	}

	@Test
	public void testProjectsAllPagesLoaded() throws IOException, AuthenticationException, NotFoundException, URISyntaxException, RedmineException{
		int NUM = 27; // must be larger than 25, which is a default page size in Redmine
		List<Project> projects = createProjects(NUM);
		
		List<Project> loadedProjects = mgr.getProjects();
		Assert.assertTrue(
                "Number of projects loaded from the server must be bigger than "
                        + NUM + ", but it's " + loadedProjects.size(),
                loadedProjects.size() > NUM);
		
		deleteProjects(projects);
	}
	
	private List<Project> createProjects(int num) throws IOException, AuthenticationException, NotFoundException, RedmineException {
		List<Project> projects = new ArrayList<Project>(num);
		for (int i=0; i<num; i++){
			Project projectToCreate = generateRandomProject();
			Project p = mgr.createProject(projectToCreate);
			projects.add(p);
		}
		return projects;
	}

	private void deleteProjects(List<Project> projects) throws IOException, AuthenticationException, NotFoundException, RedmineException {
		for(Project p : projects) {
			mgr.deleteProject(p.getIdentifier());
		}
	}
	
	@Test
	public void testGetTimeEntries() throws IOException, AuthenticationException, NotFoundException, RedmineException {
			List<TimeEntry> list = mgr.getTimeEntries();
			Assert.assertNotNull(list);
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
	}

	@Test
	public void testCreateGetTimeEntry() throws IOException, AuthenticationException, NotFoundException, RedmineException {
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
		System.out.println(createdEntry);
		Assert.assertEquals(hours, createdEntry.getHours());
		
		Float newHours = 22f;
		createdEntry.setHours(newHours);
		
		mgr.updateTimeEntry(createdEntry);
		
		TimeEntry updatedEntry = mgr.getTimeEntry(createdEntry.getId());
		Assert.assertEquals(newHours, updatedEntry.getHours());
	}
	
	@Test
	public void testCreateDeleteTimeEntry() throws IOException, AuthenticationException, NotFoundException, RedmineException {
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
		try {
			mgr.getTimeEntry(createdEntry.getId());
			Assert.fail("Must have failed with NotFoundException");
		} catch (NotFoundException e) {
			System.out.println("Got expected NotFoundException when loading TimeEntry with non-existing id");
		}
	}
	
	@Test
	public void testGetTimeEntriesForIssue() throws IOException, AuthenticationException, NotFoundException, RedmineException {
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

	private TimeEntry createTimeEntry(Integer issueId, float hours) throws IOException,
			AuthenticationException, NotFoundException, RedmineException {
		TimeEntry entry = new TimeEntry();
		entry.setHours(hours);
		entry.setIssueId(issueId);
		entry.setActivityId(ACTIVITY_ID);
		TimeEntry createdEntry = mgr.createTimeEntry(entry);
		return createdEntry;
	}
	
	@Test
	public void testDeleteIssue() throws IOException, AuthenticationException,
			NotFoundException, RedmineException {
		Issue issue = createIssues(1).get(0);
		Issue retrievedIssue = mgr.getIssueById(issue.getId());
		Assert.assertEquals(issue, retrievedIssue);

		mgr.deleteIssue(issue.getId());
		try {
			mgr.getIssueById(issue.getId());
			Assert.fail("Must have failed with NotFoundException");
		} catch (NotFoundException ignore) {
			System.out.println("Got expected NotFoundException for deleted Issue");
		}
	}
	
	@Test
	public void testUpdateIssueSpecialXMLtags() throws Exception {
		Issue issue = createIssues(1).get(0);
		String newSubject = "\"text in quotes\" and <xml> tags";
		String newDescription="<teghere>\"abc\"</here>";
		issue.setSubject(newSubject);
		issue.setDescription(newDescription);
		mgr.updateIssue(issue);

		Issue updatedIssue = mgr.getIssueById(issue.getId());
		Assert.assertEquals(newSubject, updatedIssue.getSubject());
		Assert.assertEquals(newDescription, updatedIssue.getDescription());
	}

	/**
	 * The custom fields used here MUST ALREADY EXIST on the server and be 
	 * associated with the required task type (bug/feature/task/..).
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
		mgr.updateIssue(issue);

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
			
			mgr.updateIssue(newIssue);

			Issue reloadedFromRedmineIssue = mgr.getIssueById(newIssue.getId());
			Assert.assertEquals("Estimated hours must be NULL", null, reloadedFromRedmineIssue.getEstimatedHours());
		} catch (Exception e) {
			Assert.fail();
		}
	}
	
	/* This tests finally PASSES after Redmine bug http://www.redmine.org/issues/8229
	 * was fixed  
	 */
	@Test
	public void testCreateSubProject() {
		Project createdMainProject = null;
		try {
			createdMainProject = createProject();			
			Project subProject = createSubProject(createdMainProject);
			
			Assert.assertEquals("Must have correct parent ID",
                    createdMainProject.getId(), subProject.getParentId());
		} catch (Exception e) {
			Assert.fail();
		} finally {
			if (createdMainProject != null) {
				try {
					mgr.deleteProject(createdMainProject.getIdentifier());
				} catch (Exception e) {
					Assert.fail();
				}
			}
		}

	}

	private Project createProject() throws IOException, AuthenticationException, RedmineException {
		Project mainProject = new Project();
		long id = new Date().getTime();
		mainProject.setName("project" + id);
		mainProject.setIdentifier("project" + id);
		return mgr.createProject(mainProject);
	}
	
	private Project createSubProject(Project parent) throws IOException, AuthenticationException, RedmineException {
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
			mgr.updateIssue(createdIssue);

			Integer issueId = createdIssue.getId();
			Issue reloadedFromRedmineIssue = mgr.getIssueById(issueId);
			Assert.assertEquals(
                    "Checking if 'update issue' operation changed 'done ratio' field",
                    doneRatio, reloadedFromRedmineIssue.getDoneRatio());

			Integer invalidDoneRatio = 130;
			reloadedFromRedmineIssue.setDoneRatio(invalidDoneRatio);
			try {
				mgr.updateIssue(reloadedFromRedmineIssue);
			} catch (RedmineException e) {
				Assert.assertEquals("Must be 1 error", 1, e.getErrors().size());
				Assert.assertEquals("Checking error text", "% Done is not included in the list", e.getErrors().get(0).toString());
			}

			Issue reloadedFromRedmineIssueUnchanged = mgr.getIssueById(issueId);
			Assert.assertEquals(
                    "'done ratio' must have remained unchanged after invalid value",
                    doneRatio, reloadedFromRedmineIssueUnchanged.getDoneRatio());
		} catch (Exception e) {
			Assert.fail();
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
			mgr.updateIssue(createdIssue);

			Integer issueId = createdIssue.getId();
			Issue reloadedFromRedmineIssue = mgr.getIssueById(issueId);
			Assert.assertEquals(
                    "Description must not be erased",
                    descr, reloadedFromRedmineIssue.getDescription());

			reloadedFromRedmineIssue.setDescription("");
			mgr.updateIssue(reloadedFromRedmineIssue);

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
			mgr.updateIssue(loadedIssueWithJournals);
			
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
			IssueRelation r = mgr.createRelation(projectKey, src.getId(), target.getId(), relationText);
//			assertEquals(src.getId(), r.getIssueId());
			Assert.assertEquals(target.getId(), r.getIssueToId());
			Assert.assertEquals(relationText, r.getType());
		} catch (Exception e) {
			Assert.fail(e.toString());
		}
	}

	private IssueRelation createTwoRelatedIssues() throws IOException, AuthenticationException, NotFoundException, RedmineException {
		List<Issue> issues = createIssues(2);
		Issue src = issues.get(0);
		Issue target = issues.get(1);
		
		String relationText = IssueRelation.TYPE.precedes.toString();
		IssueRelation r = mgr.createRelation(projectKey, src.getId(), target.getId(), relationText);
		return r;
	}
	
	@Test
	public void testLoadRelation() {
		try {
			IssueRelation relation = createTwoRelatedIssues();
			Issue issue = mgr.getIssueById(relation.getIssueId(), INCLUDE.relations);
			Issue issueTarget = mgr.getIssueById(relation.getIssueToId(), INCLUDE.relations);
			
			Assert.assertEquals(1, issue.getRelations().size());
			Assert.assertEquals(1, issueTarget.getRelations().size());
			
			IssueRelation relation1 = issue.getRelations().get(0);
//			assertEquals(issue.getId(), relation1.getIssueId());
			Assert.assertEquals(issue.getId(), relation1.getIssueToId());
			Assert.assertEquals("precedes", relation1.getType());
			Assert.assertEquals((Integer) 0, relation1.getDelay());

			IssueRelation reverseRelation = issueTarget.getRelations().get(0);
			// both forward and reverse relations are the same!
			Assert.assertEquals(relation1, reverseRelation);
		} catch (Exception e) {
			Assert.fail(e.toString());
		}
	}

	/** this test is ignored because:
	 * 1) we can't create Versions. see http://www.redmine.org/issues/9088
	 * 2) we don't currently set versions when creating issues.   
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
			// XXX there could be a case when a project does not have any trackers
			// need to create a project with some trackers to make this test deterministic
			Assert.assertTrue(!p1.getTrackers().isEmpty());
			System.out.println(p1.getTrackers());
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
	}

	@Ignore
	@Test
	public void testSpentTime() {
		// TODO need to use "Time Entries" 
//		float spentHours = 12.5f;
//		issueToCreate.setSpentHours(spentHours);
		// check SPENT TIME
//		assertEquals((Float) spentHours, newIssue.getSpentHours());
	}

    @Test
	public void testViolateTimeEntryConstraint_ProjectOrIssueID_issue66() throws IOException, AuthenticationException, RedmineException {
		TimeEntry timeEntry = new TimeEntry();
		timeEntry.setActivityId(ACTIVITY_ID);
		timeEntry.setSpentOn(new Date());
		timeEntry.setHours(1.5f);
		try {
			mgr.createTimeEntry(timeEntry);
		} catch (IllegalArgumentException e) {
			System.out.println("create: Got expected IllegalArgumentException for invalid Time Entry (issue #66).");
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail("Got unexpected " + e.getClass().getSimpleName() + ": " + e.getMessage());
		}
		try {
			mgr.updateTimeEntry(timeEntry);
		} catch (IllegalArgumentException e) {
			System.out.println("update: Got expected IllegalArgumentException for invalid Time Entry (issue #66).");
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail("Got unexpected " + e.getClass().getSimpleName() + ": " + e.getMessage());
		}
		// Now can try to verify with project ID (only test with issue ID seems to be already covered)
		int projectId = mgr.getProjects().get(0).getId();
		timeEntry.setProjectId(projectId);
		try {
			TimeEntry created = mgr.createTimeEntry(timeEntry);
			System.out.println(created);
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail("Unexpected " + e.getClass().getSimpleName() + ": " + e.getMessage());
		}
	}
}
