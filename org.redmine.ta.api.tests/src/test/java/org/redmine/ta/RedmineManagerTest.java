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

import org.redmine.ta.beans.CustomField;
import org.redmine.ta.beans.Issue;
import org.redmine.ta.beans.Project;
import org.redmine.ta.beans.TimeEntry;
import org.redmine.ta.beans.Tracker;
import org.redmine.ta.beans.User;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class RedmineManagerTest {

	// TODO We don't know activities IDs!
	// see feature request http://www.redmine.org/issues/7506
	private static final Integer ACTIVITY_ID = 8;

	private static RedmineManager mgr;

	private static String projectKey;

	@BeforeClass
	public static void oneTimeSetUp() {
		System.out.println("Running redmine tests using: " + Config.getHost());
		mgr = new RedmineManager(Config.getHost(), Config.getApiKey());
		Project junitTestPRoject = new Project();
		junitTestPRoject.setName("test project");
		junitTestPRoject.setIdentifier("test"
				+ Calendar.getInstance().getTimeInMillis());
		try {
			Project createdProject = mgr.createProject(junitTestPRoject);
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
//			System.out.println("foundIssues: " + foundIssues);

			assertNotNull("Checking if search results is not NULL", foundIssues);
			assertTrue("Search results must be not empty",
					!(foundIssues.isEmpty()));
			// assertNotNull("New issue must have some ID", newIssue.getId());

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
		String login = Config.getUserLogin();

		User assignee = new User();
		assignee.setId(userId);
		assignee.setLogin(login);
		return assignee;
	}

	@Test
	public void testWrongCredentialsOnCreateIssue() throws RuntimeException {
		
		RedmineManager redmineMgrEmpty = new RedmineManager(Config.getHost(), null);

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
		RedmineManager redmineMgrInvalidKey = new RedmineManager(Config.getHost(), "wrong_key");
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
	
	private static Project generateRandomProject() {
		Project project = new Project();
		Long timeStamp = Calendar.getInstance().getTimeInMillis();
		String key = "projkey" + timeStamp;
		String name = "project number "+ timeStamp;
		String description = "some description for the project";
		project.setIdentifier(key);
		project.setName(name);
		project.setDescription(description);
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
		User userToCreate = new User();
		userToCreate.setFirstName("fname");
		userToCreate.setLastName("lname");
		long randomNumber = new Date().getTime();
		userToCreate.setLogin("login" + randomNumber);
		userToCreate.setMail("somemail" + randomNumber + "@somedomain.com");
		userToCreate.setPassword("zzzz");
		try {
			User createdUser = mgr.createUser(userToCreate);
			
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
			List<Issue> issues = mgr.getIssues("test1296256368758", null);//projectKey, null);
			System.out.println("testGetIssuesNEWMETHOD() loaded " + issues.size() + " issues");//using query #" + queryIdIssuesCreatedLast2Days);
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
		assertTrue(loadedProjects.size()>NUM);
		
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

}
