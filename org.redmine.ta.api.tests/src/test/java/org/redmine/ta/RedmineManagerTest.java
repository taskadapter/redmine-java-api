package org.redmine.ta;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.redmine.ta.RedmineManager.INCLUDE;
import org.redmine.ta.beans.CustomField;
import org.redmine.ta.beans.Issue;
import org.redmine.ta.beans.Journal;
import org.redmine.ta.beans.Project;
import org.redmine.ta.beans.TimeEntry;
import org.redmine.ta.beans.Tracker;
import org.redmine.ta.beans.User;

public class RedmineManagerTest {

	// TODO We don't know activities IDs!
	// see feature request http://www.redmine.org/issues/7506
	private static final Integer ACTIVITY_ID = 8;

	private static RedmineManager mgr;

	private static String projectKey;

	@BeforeClass
	public static void oneTimeSetUp() {
		System.out.println("Running redmine tests using: " + Config.getURI());
//		mgr = new RedmineManager(Config.getHost(), Config.getApiKey());
		mgr = new RedmineManager(Config.getURI(), Config.getLogin(), Config.getPassword());
		Project junitTestProject = new Project();
		junitTestProject.setName("test project");
		junitTestProject.setIdentifier("test"
				+ Calendar.getInstance().getTimeInMillis());
		try {
			Project createdProject = mgr.createProject(junitTestProject);
			projectKey = createdProject.getIdentifier();
		} catch (Exception e) {
			e.printStackTrace();
			fail("can't create a test project" + e.getMessage());
		}
	}

	@AfterClass
	public static void oneTimeTearDown() {
		try {
			if (mgr != null) {
				mgr.deleteProject(projectKey);
			}
		} catch (Exception e) {
			e.printStackTrace();
			fail("can't delete the test project '" + projectKey + ". reason: "
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
			assertEquals("Checking assignee id", assignee.getId(),
					actualAssignee.getId());
			
			// check AUTHOR
			Integer EXPECTED_AUTHOR_ID  = getOurUser().getId();
			assertEquals(EXPECTED_AUTHOR_ID, newIssue.getAuthor().getId());

			// check ESTIMATED TIME
			assertEquals((Float) estimatedHours, newIssue.getEstimatedHours());
			
			// check multi-line DESCRIPTION
			String regexpStripExtra = "\\r|\\n|\\s";
			description = description.replaceAll(regexpStripExtra, "");
			String actualDescription = newIssue.getDescription();
			actualDescription = actualDescription.replaceAll(regexpStripExtra, "");
			assertEquals(description, actualDescription);
			
			// PRIORITY
			assertNotNull(newIssue.getPriorityId());
			assertTrue(newIssue.getPriorityId()>0);
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void testCreateIssueWithParent() {
		try {
			Issue parentIssue = new Issue();
			parentIssue.setSubject("parent 1");
			Issue newParentIssue = mgr.createIssue(projectKey, parentIssue);
			System.out.println("created parent: " + newParentIssue);

			assertNotNull("Checking parent was created", newParentIssue);
			assertNotNull("Checking ID of parent issue is not null",
					newParentIssue.getId());

			// Integer parentId = 46;
			Integer parentId = newParentIssue.getId();

			Issue childIssue = new Issue();
			childIssue.setSubject("child 1");
			childIssue.setParentId(parentId);

			Issue newChildIssue = mgr.createIssue(projectKey, childIssue);
			System.out.println("created child: " + newChildIssue);

			assertEquals("Checking parent ID of the child issue", parentId,
					newChildIssue.getParentId());

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
			User assignee = getOurUser();
			issue.setAssignee(assignee);

			Issue newIssue = mgr.createIssue(projectKey, issue);
			System.out.println("created: " + newIssue);
			assertNotNull("Checking returned result", newIssue);
			assertNotNull("New issue must have some ID", newIssue.getId());

			// try to find the issue
			List<Issue> foundIssues = mgr.getIssuesBySummary(projectKey,
					summary);

			assertNotNull("Checking if search results is not NULL", foundIssues);
			assertTrue("Search results must be not empty",
					!(foundIssues.isEmpty()));
			
			Issue loadedIssue1 = RedmineTestUtils.findIssueInList(foundIssues, newIssue.getId());
			assertNotNull(loadedIssue1);
			assertEquals(summary, loadedIssue1.getSubject());
			
			// User actualAssignee = newIssue.getAssignee();

			// assertNotNull("Checking assignee not null", actualAssignee);
			// assertEquals("Checking assignee Name", assignee.getName(),
			// actualAssignee.getName());
			// assertEquals("Checking assignee Id", assignee.getId(),
			// actualAssignee.getId());

		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void testTryFindNonExistingIssue() {
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

	private static User getOurUser() {
		Integer userId = Integer
				.parseInt(Config.getParam("createissue.userid"));
		String login = Config.getLogin();
		String fName = Config.getParam("userFName");
		String lName = Config.getParam("userLName");
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
			fail("Must have failed with IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			System.out.println("Got expected IllegalArgumentException.");
		} catch (Exception e) {
			fail("Got unexpected exception : " + e);
		}
		
	}

	@Test
	public void testEmptyHostParameter() throws RuntimeException {
		try {
			new RedmineManager("", null); 
			fail("Must have failed with IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			System.out.println("Got expected IllegalArgumentException.");
		} catch (Exception e) {
			fail("Got unexpected exception : " + e);
		}
		
	}
	
	@Test
	public void testWrongCredentialsOnCreateIssue() throws RuntimeException {
		
		RedmineManager redmineMgrEmpty = new RedmineManager(Config.getURI(), null);

		// NO API access key set
		Issue issue = new Issue();
		issue.setSubject("test zzx");
		try {
			redmineMgrEmpty.createIssue(projectKey, issue);
			fail("Must have failed with '401 Not authorized'");
		} catch (AuthenticationException e) {
			System.out.println("Got expected AuthenticationException.");
		} catch (Exception e) {
			fail("Got unexpected exception : " + e);
		}
		
		// set invalid API access key
		RedmineManager redmineMgrInvalidKey = new RedmineManager(Config.getURI(), "wrong_key");
		try {
			redmineMgrInvalidKey.createIssue(projectKey, issue);
			fail("Must have failed with '401 Not authorized'");
		} catch (AuthenticationException e) {
			System.out.println("Got expected AuthenticationException.");
		} catch (Exception e) {
			fail("Got unexpected exception : " + e);
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

			assertEquals(
					"Checking if 'update issue' operation changed the 'subject' field",
					changedSubject, reloadedFromRedmineIssue.getSubject());
		} catch (Exception e) {
			e.printStackTrace();
			fail();
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

			assertEquals(
					"Checking if 'get issue by ID' operation returned issue with same 'subject' field",
					originalSubject, reloadedFromRedmineIssue.getSubject());

		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}
	
	@Test
	public void testGetProjects() {
		try {
			List<Project> projects = mgr.getProjects();
			assertTrue(projects.size()>0);
			boolean found = false;
			for (Project project : projects) {
				if (project.getIdentifier().equals(projectKey)) {
					found = true;
					break;
				}
			}
			if (!found) {
				fail("Our project with key '" + projectKey+"' is not found on the server");
			}
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
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
			assertTrue(issues.size()>0);
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
	
	@Test
	public void testGetIssuesInvalidQueryId() {
		try {
			Integer invalidQueryId = 9999999;
			mgr.getIssues(projectKey, invalidQueryId);
			fail("Must have failed with NotFoundException because query ID is invalid");
		} catch (NotFoundException e) {
			System.out.println("Got expected NotFoundException:" + e.getMessage());
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testCreateProject() throws IOException, AuthenticationException, NotFoundException, RedmineException {
		Project projectToCreate = generateRandomProject();
		String key = null;
		try {
			Project createdProject = mgr.createProject(projectToCreate);
			key = createdProject.getIdentifier();
			
			assertNotNull("checking that a non-null project is returned", createdProject);
			
			assertEquals(projectToCreate.getIdentifier(), createdProject.getIdentifier());
			assertEquals(projectToCreate.getName(), createdProject.getName());
			assertEquals(projectToCreate.getDescription(), createdProject.getDescription());
			assertEquals(projectToCreate.getHomepage(), createdProject.getHomepage());
			
			List<Tracker> trackers = createdProject.getTrackers();
			assertNotNull("checking that project has some trackers", trackers);
			assertTrue("checking that project has some trackers", !(trackers.isEmpty()));
		} catch (Exception e) {
			fail(e.getMessage());
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
			assertNotNull(updatedProject);
			
			assertEquals(createdProject.getIdentifier(), updatedProject.getIdentifier());
			assertEquals(newName, updatedProject.getName());
			assertEquals(newDescr, updatedProject.getDescription());
			List<Tracker> trackers = updatedProject.getTrackers();
			assertNotNull("checking that project has some trackers", trackers);
			assertTrue("checking that project has some trackers", !(trackers.isEmpty()));
			
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
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
			assertNotNull(e.getErrors());
			assertEquals(1, e.getErrors().size());
			assertEquals("Identifier is reserved", e.getErrors().get(0)); 
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
			String nonLatinSymbols = "Example with accents Ação";
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
			assertNotNull("New issue must have some ID", newIssue.getId());

			// check AUTHOR
			Integer EXPECTED_AUTHOR_ID  = getOurUser().getId();
			assertEquals(EXPECTED_AUTHOR_ID, newIssue.getAuthor().getId());

		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void testCreateIssueInvalidProjectKey() {
		try {
			Issue issueToCreate = new Issue();
			issueToCreate.setSubject("Summary line 100");
			String invalidProjectKey = "someNotExistingProjectKey";
			mgr.createIssue(invalidProjectKey, issueToCreate);
			
			fail("Must have failed with NotFoundException because we provided invalid project key.");
		} catch (NotFoundException e) {
			System.out.println("Got expected NotFoundException: " + e.getMessage());
		} catch (Exception e) {
			fail();
		}
	}

	@Test
	public void testGetProjectNonExistingId() {
		try {
			mgr.getProjectByKey("some-non-existing-key");
			fail("Must have failed with NotFoundException");
		} catch (NotFoundException e) {
			System.out.println("Got expected NotFoundException.");
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testDeleteNonExistingProject() {
		try {
			mgr.deleteProject("some-non-existing-key");
			fail("Must have failed with NotFoundException");
		} catch (NotFoundException e) {
			System.out.println("Got expected NotFoundException.");
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testGetIssueNonExistingId() {
		try {
			int someNonExistingID = 999999;
			mgr.getIssueById(someNonExistingID);
			fail("Must have failed with NotFoundException.");
		} catch (NotFoundException e) {
			System.out.println("Got expected NotFoundException.");
		} catch (Exception e) {
			fail();
		}
	}

	@Test
	public void testUpdateIssueNonExistingId() {
		try {
			int nonExistingId = 999999;
			Issue issue = new Issue();
			issue.setId(nonExistingId);
			mgr.updateIssue(issue);
			fail("Must have failed with NotFoundException because we provided invalid issue ID.");
		} catch (NotFoundException e) {
			System.out.println("Got expected NotFoundException.");
		} catch (Exception e) {
			fail();
		}
	}

	
	@Test
	public void testGetUsers() {
		try {
			List<User> users = mgr.getUsers();
			assertTrue(users.size()>0);
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
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testGetCurrentUser() throws IOException, AuthenticationException, RedmineException {
		User currentUser = mgr.getCurrentUser();
		assertEquals(getOurUser().getId(), currentUser.getId());
		assertEquals(getOurUser().getLogin(), currentUser.getLogin());
	}
	
	@Test
	public void testGetUserById() throws IOException, AuthenticationException, NotFoundException, RedmineException {
		User loadedUser = mgr.getUserById(getOurUser().getId());
		assertEquals(getOurUser().getId(), loadedUser.getId());
		assertEquals(getOurUser().getLogin(), loadedUser.getLogin());
	}
	
	@Test
	public void testGetUserNonExistingId() throws IOException, AuthenticationException, RedmineException {
		try {
			mgr.getUserById(999999);
			fail("Must have failed above");
		} catch (NotFoundException e) {
			System.out.println("Got expected NotFoundException");
		}
	}
	
	@Test
	public void testCreateUser() throws IOException, AuthenticationException, NotFoundException {
		try {
			User userToCreate = generateRandomUser();
			User createdUser = mgr.createUser(userToCreate );
			
			assertNotNull("checking that a non-null project is returned", createdUser);
			
			assertEquals(userToCreate.getLogin(), createdUser.getLogin());
			assertEquals(userToCreate.getFirstName(), createdUser.getFirstName());
			assertEquals(userToCreate.getLastName(), createdUser.getLastName());
			Integer id = createdUser.getId();
			assertNotNull(id);
			
		} catch (Exception e) {
			fail(e.getMessage());
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
			assertNotNull("checking that a non-null project is returned", createdUser);
			
			String newFirstName = "fnameNEW";
			String newLastName = "lnameNEW";
			String newMail = "newmail"+randomNumber+"@asd.com";
			createdUser.setFirstName(newFirstName);
			createdUser.setLastName(newLastName);
			createdUser.setMail(newMail);
			
			mgr.updateUser(createdUser);
			
			User updatedUser = mgr.getUserById(userId);
			
			assertEquals(newFirstName, updatedUser.getFirstName());
			assertEquals(newLastName, updatedUser.getLastName());
			assertEquals(newMail, updatedUser.getMail());
			assertEquals(userId, updatedUser.getId());
			
		} catch (Exception e) {
			fail(e.getMessage());
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
			assertTrue(issues.size()>26);
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
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

	@Test
	public void testProjectsAllPagesLoaded() throws IOException, AuthenticationException, NotFoundException, URISyntaxException, RedmineException{
		int NUM = 27; // must be larger than 25, which is a default page size in Redmine
		List<Project> projects = createProjects(NUM);
		
		List<Project> loadedProjects = mgr.getProjects();
		assertTrue(
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
			assertNotNull(list);
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

		assertNotNull(createdEntry);
		System.out.println(createdEntry);
		assertEquals(hours, createdEntry.getHours());
		
		Float newHours = 22f;
		createdEntry.setHours(newHours);
		
		mgr.updateTimeEntry(createdEntry);
		
		TimeEntry updatedEntry = mgr.getTimeEntry(createdEntry.getId());
		assertEquals(newHours, updatedEntry.getHours());
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
		assertNotNull(createdEntry);
		
		mgr.deleteTimeEntry(createdEntry.getId());
		try {
			mgr.getTimeEntry(createdEntry.getId());
			fail("Must have failed with NotFoundException");
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
		assertEquals(issue, retrievedIssue);

		mgr.deleteIssue(issue.getId());
		try {
			mgr.getIssueById(issue.getId());
			fail("Must have failed with NotFoundException");
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
		assertEquals(newSubject, updatedIssue.getSubject());
		assertEquals(newDescription, updatedIssue.getDescription());
	}

	/**
	 * The custom fields used here MUST ALREADY EXIST on the server and be 
	 * associated with the required task type (bug/feature/task/..).
	 */
	@Test
	public void testCustomFields() throws Exception {
		Issue issue = createIssues(1).get(0);
		// default empty values
		assertEquals(2, issue.getCustomFields().size());

		
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
		assertEquals(2, updatedIssue.getCustomFields().size());
		assertEquals(custom1Value, updatedIssue.getCustomField(custom1FieldName));
		assertEquals(custom2Value, updatedIssue.getCustomField(custom2FieldName));
	}

	@Test
	public void testUpdateIssueDoesNotChangeEstimatedTime() {
		try {
			Issue issue = new Issue();
			String originalSubject = "Issue " + new Date();
			issue.setSubject(originalSubject);

			Issue newIssue = mgr.createIssue(projectKey, issue);
			assertEquals("Estimated hours must be NULL", null, newIssue.getEstimatedHours());
			
			mgr.updateIssue(newIssue);

			Issue reloadedFromRedmineIssue = mgr.getIssueById(newIssue.getId());
			assertEquals("Estimated hours must be NULL", null, reloadedFromRedmineIssue.getEstimatedHours());
		} catch (Exception e) {
			fail();
		}
	}
	
	/* this test fails because Redmine's getproject() response does NOT
	 * include parent ID. see bug http://www.redmine.org/issues/8229 
	 */
	@Ignore
	@Test
	public void testCreateSubProject() {
		Project createdMainProject = null;
		Project createdSubProject = null;
		try {
			Project mainProject = new Project();
			long id = new Date().getTime();
			mainProject.setName("project" + id);
			mainProject.setIdentifier("project" + id);
			createdMainProject = mgr.createProject(mainProject);
			
			Project subProject = new Project();
			long subId = new Date().getTime()+1;
			subProject.setName("subproject" + subId);
			subProject.setIdentifier("subproject" + subId);
			createdSubProject = mgr.createProject(subProject);
			assertEquals(
					"update this code when http://www.redmine.org/issues/8229 is resolved. Must have correct parent ID",
					createdMainProject.getId(), createdSubProject.getParentId());
		} catch (Exception e) {
			fail();
		} finally {
			if (createdMainProject != null) {
				try {
					mgr.deleteProject(createdMainProject.getIdentifier());
				} catch (Exception e) {
					fail();
				}
			}
			if (createdSubProject != null) {
				try {
					mgr.deleteProject(createdSubProject.getIdentifier());
				} catch (Exception e) {
					fail();
				}
			}

		}

	}

	@Test
	public void testIssueDoneRatio() {
		try {
			Issue issue = new Issue();
			String subject = "Issue " + new Date();
			issue.setSubject(subject);

			Issue createdIssue = mgr.createIssue(projectKey, issue);
			assertEquals("Initial 'done ratio' must be 0", (Integer) 0, createdIssue.getDoneRatio());
			Integer doneRatio = 50;
			createdIssue.setDoneRatio(doneRatio);
			mgr.updateIssue(createdIssue);

			Integer issueId = createdIssue.getId();
			Issue reloadedFromRedmineIssue = mgr.getIssueById(issueId);
			assertEquals(
					"Checking if 'update issue' operation changed 'done ratio' field",
					doneRatio, reloadedFromRedmineIssue.getDoneRatio());

			Integer invalidDoneRatio = 130;
			reloadedFromRedmineIssue.setDoneRatio(invalidDoneRatio);
			try {
				mgr.updateIssue(reloadedFromRedmineIssue);
			} catch (RedmineException e) {
				assertEquals("Must be 1 error", 1, e.getErrors().size());
				assertEquals("Checking error text", "% Done is not included in the list", e.getErrors().get(0).toString());
			}

			Issue reloadedFromRedmineIssueUnchanged = mgr.getIssueById(issueId);
			assertEquals(
					"'done ratio' must have remained unchanged after invalid value",
					doneRatio, reloadedFromRedmineIssueUnchanged.getDoneRatio());
		} catch (Exception e) {
			fail();
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
			assertEquals("Checking description", descr, createdIssue.getDescription());
			
			createdIssue.setDescription(null);
			mgr.updateIssue(createdIssue);

			Integer issueId = createdIssue.getId();
			Issue reloadedFromRedmineIssue = mgr.getIssueById(issueId);
			assertEquals(
					"Description must not be erased",
					descr, reloadedFromRedmineIssue.getDescription());

			reloadedFromRedmineIssue.setDescription("");
			mgr.updateIssue(reloadedFromRedmineIssue);

			Issue reloadedFromRedmineIssueUnchanged = mgr.getIssueById(issueId);
			assertEquals(
					"Description must be erased",
					"", reloadedFromRedmineIssueUnchanged.getDescription());
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

			Issue loadedIssueWithJournals = mgr.getIssueById(newIssue.getId(), INCLUDE.journals);
			assertTrue(loadedIssueWithJournals.getJournals().isEmpty());
			
			String commentDescribingTheUpdate = "some comment describing the issue update"; 
			loadedIssueWithJournals.setSubject("new subject");
			loadedIssueWithJournals.setNotes(commentDescribingTheUpdate);
			mgr.updateIssue(loadedIssueWithJournals);
			
			Issue loadedIssueWithJournals2 = mgr.getIssueById(newIssue.getId(), INCLUDE.journals);
			assertEquals(1, loadedIssueWithJournals2.getJournals().size());
			
			Journal journalItem = loadedIssueWithJournals2.getJournals().get(0);
			assertEquals(commentDescribingTheUpdate, journalItem.getNotes());
			User ourUser = getOurUser();
			// can't compare User objects because either of them is not completely filled
			assertEquals(ourUser.getId(), journalItem.getUser().getId());
			assertEquals(ourUser.getFirstName(), journalItem.getUser().getFirstName());
			assertEquals(ourUser.getLastName(), journalItem.getUser().getLastName());

			Issue loadedIssueWithoutJournals = mgr.getIssueById(newIssue.getId());
			assertTrue(loadedIssueWithoutJournals.getJournals().isEmpty());

		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	// Redmine ignores this parameter for "get projects" request. see bug http://www.redmine.org/issues/8545
	@Ignore
	@Test
	public void testGetProjectsIncludesTrackers() {
		try {
			List<Project> projects = mgr.getProjects();
			assertTrue(projects.size()>0);
			Project p1 = projects.get(0);
			assertNotNull(p1.getTrackers());
			// XXX there could be a case when a project does not have any trackers
			// need to create a project with some trackers to make this test deterministic
			assertTrue(! p1.getTrackers().isEmpty());
			System.out.println(p1.getTrackers());
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
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
}
