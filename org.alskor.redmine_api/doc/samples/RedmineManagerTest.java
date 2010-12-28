package org.alskor.redmine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.alskor.httputils.AuthenticationException;
import org.alskor.httputils.NotFoundException;
import org.alskor.redmine.beans.Issue;
import org.alskor.redmine.beans.Project;
import org.alskor.redmine.beans.User;
import org.junit.Before;
import org.junit.Test;

public class RedmineManagerTest {

	private RedmineManager mgr;

	private final static String PROJECT_KEY = Config.getProjectKey();

	@Before
	// Is executed before each test method
	public void setup() throws Exception {
		mgr = new RedmineManager(Config.getHost(), Config.getApiKey());
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
			
			Issue newIssue = mgr.createIssue(PROJECT_KEY, issueToCreate);
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
			assertEquals("Checking assignee Name", assignee.getFullName(),
					actualAssignee.getFullName());
			assertEquals("Checking assignee Id", assignee.getId(),
					actualAssignee.getId());
			
			// check AUTHOR
			String EXPECTED_USER_FULL_NAME  = Config.getParam("createissue.userFirstAndLastName");
			assertEquals(EXPECTED_USER_FULL_NAME, newIssue.getAuthor().getFullName());

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
			Issue newParentIssue = mgr.createIssue(PROJECT_KEY, parentIssue);
			System.out.println("created parent: " + newParentIssue);

			assertNotNull("Checking parent was created", newParentIssue);
			assertNotNull("Checking ID of parent issue is not null",
					newParentIssue.getId());

			// Integer parentId = 46;
			Integer parentId = newParentIssue.getId();

			Issue childIssue = new Issue();
			childIssue.setSubject("child 1");
			childIssue.setParentId(parentId);

			Issue newChildIssue = mgr.createIssue(PROJECT_KEY, childIssue);
			System.out.println("created child: " + newChildIssue);

			assertEquals("Checking parent ID of the child issue", parentId,
					newChildIssue.getParentId());

		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void testFindIssuesBySummary() {
		String summary = "issue with subject ABC";
		try {
			Issue issue = new Issue();
			issue.setSubject(summary);
			User assignee = getOurUser();
			issue.setAssignee(assignee);

			Issue newIssue = mgr.createIssue(PROJECT_KEY, issue);
			System.out.println("created: " + newIssue);
			assertNotNull("Checking returned result", newIssue);
			assertNotNull("New issue must have some ID", newIssue.getId());

			// try to find the issue
			List<Issue> foundIssues = mgr.getIssuesBySummary(PROJECT_KEY,
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
			List<Issue> foundIssues = mgr.getIssuesBySummary(PROJECT_KEY,
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
		String userFullName = Config.getParam("createissue.userFirstAndLastName");

		User assignee = new User();
		assignee.setId(userId);
		assignee.setFullName(userFullName);
		return assignee;
	}

	@Test
	public void testWrongCredentialsOnCreateIssue() throws RuntimeException {
		
		RedmineManager redmineMgrEmpty = new RedmineManager(Config.getHost(), null);

		// NO API access key set
		Issue issue = new Issue();
		issue.setSubject("test zzx");
		try {
			redmineMgrEmpty.createIssue(PROJECT_KEY, issue);
			fail("Must have failed with '401 Not authorized'");
		} catch (AuthenticationException e) {
			System.out.println("Got expected AuthenticationException.");
		} catch (Exception e) {
			fail("Got unexpected exception : " + e);
		}
		
		// set invalid API access key
		RedmineManager redmineMgrInvalidKey = new RedmineManager(Config.getHost(), "wrong_key");
		try {
			redmineMgrInvalidKey.createIssue(PROJECT_KEY, issue);
			fail("Must have failed with '401 Not authorized'");
		} catch (AuthenticationException e) {
			System.out.println("Got expected AuthenticationException.");
		} catch (Exception e) {
			fail("Got unexpected exception : " + e);
		}

	}

/*	@Test
	public void testParseIssuesFromEmptyXMLUsingRedmineLoader() {
		// issues xml with no individual issues in the list should not break the
		// loader.

		InputStream is;
		RedmineLoader loader = new RedmineLoader();
		try {
			is = MyIOUtils.getResourceAsStream(FILE_EMPTY_ISSUES_XML);
			loader.startLoading(is);
		} catch (Exception e) {
			e.printStackTrace();
			fail("Error:" + e);
		}
		List<Issue> issuesList = loader.getIssues();
		HashMap<Integer, Issue> issuesMap = loader.getIssuesMap();

		assertTrue(issuesList.isEmpty());
		assertTrue(issuesMap.isEmpty());
	}*/

	@Test
	public void testUpdateIssue() {
		try {
			Issue issue = new Issue();
			String originalSubject = "Issue " + new Date();
			issue.setSubject(originalSubject);

			Issue newIssue = mgr.createIssue(PROJECT_KEY, issue);
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

			Issue newIssue = mgr.createIssue(PROJECT_KEY, issue);

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
				if (project.getIdentifier().equals(PROJECT_KEY)) {
					found = true;
					break;
				}
			}
			if (!found) {
				fail("Our project with key '" + PROJECT_KEY+"' is not found on the server");
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
			Issue newIssue = mgr.createIssue(PROJECT_KEY, issueToCreate);

			Integer queryIdIssuesCreatedLast2Days = Config.getQueryId();
			List<Issue> issues = mgr.getIssues(PROJECT_KEY, queryIdIssuesCreatedLast2Days);
			System.out.println("getIssues() loaded " + issues.size() + " issues using query #" + queryIdIssuesCreatedLast2Days);
			assertTrue(issues.size()>0);
			boolean found = false;
			for (Issue issue : issues) {
				if (issue.getId().equals(newIssue.getId())) {
					found = true;
					break;
				}
			}
			if (!found) {
				fail("getIssues() didn't return the issue we just created. query #"
						+ queryIdIssuesCreatedLast2Days + " must have returned all issues created during the last 2 days");
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
			mgr.getIssues(PROJECT_KEY, invalidQueryId);
			fail("Must have failed with NotFoundException because query ID is invalid");
		} catch (NotFoundException e) {
			System.out.println("Got expected NotFoundException:" + e.getMessage());
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testCreateProject() {
		Project projectToCreate = generateRandomProject();
		try {
			Project createdProject = mgr.createProject(projectToCreate);
			
			assertNotNull("checking that a non-null project is returned", createdProject);
			
			assertEquals(projectToCreate.getIdentifier(), createdProject.getIdentifier());
			assertEquals(projectToCreate.getName(), createdProject.getName());
			assertEquals(projectToCreate.getDescription(), createdProject.getDescription());
			
			/* 
			 * Redmine 1.0.5 and Trunk (pre-1.1 version) do not provide list of trackers with the project.
			 * see enhancement request http://www.redmine.org/issues/7184
			 */
//			List<Tracker> trackers = createdProject.getTrackers();
//			assertNotNull("checking that project has some trackers", trackers);
//			assertTrue("checking that project has some trackers", !(trackers.isEmpty()));
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	@Test
	public void testCreateGetUpdateDeleteProject() {
		Project projectToCreate = generateRandomProject();
		try {
			projectToCreate.setIdentifier("zzz");
			Project createdProject = mgr.createProject(projectToCreate);
			String key = createdProject.getIdentifier();
			String newDescr = "NEW123";
			String newName = "new name here";
			
			createdProject.setName(newName);
			createdProject.setDescription(newDescr);
			mgr.updateProject(createdProject);
			
			Project updatedProject = mgr.getProjectByIdentifier(key);
			assertNotNull(updatedProject);
			
			assertEquals(createdProject.getIdentifier(), updatedProject.getIdentifier());
			assertEquals(newName, updatedProject.getName());
			assertEquals(newDescr, updatedProject.getDescription());
			
			mgr.deleteProject(key);
			
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
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
	
	// XXX not completed. this test only works with hostedredmine.com. finish this test later
/*	@Test
	public void testPaging() {
		try {
			String queryIdAllIssues = "156";
			RedmineManager mgr104 = new RedmineManager(Config.getHost(), Config.getApiKey());
//			mgr104.setRedmineVersion(REDMINE_VERSION.V104);
			
			List<Issue> issues = mgr104.getIssues(PROJECT_KEY, queryIdAllIssues);
			System.out.println("getIssues() loaded " + issues.size() + " issues using query #" + queryIdAllIssues);
			assertTrue(issues.size()>25);
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
*/

	@Test
	public void testCreateIssueNonUnicodeSymbols() {
		try {
			String nonLatinSymbols = "Example with accents Ação";
			Issue toCreate = new Issue();
			toCreate.setSubject(nonLatinSymbols);
			Issue created = mgr.createIssue(PROJECT_KEY, toCreate);
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

			Issue newIssue = mgr.createIssue(PROJECT_KEY, issueToCreate);
			assertNotNull("Checking returned result", newIssue);
			assertNotNull("New issue must have some ID", newIssue.getId());

			// check AUTHOR
			String EXPECTED_USER_FULL_NAME  = Config.getParam("createissue.userFirstAndLastName");
			assertEquals(EXPECTED_USER_FULL_NAME, newIssue.getAuthor().getFullName());

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
			mgr.getProjectByIdentifier("some-non-existing-key");
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
	
}
