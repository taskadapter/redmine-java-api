package org.alskor.redmine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.alskor.httputils.AuthenticationException;
import org.junit.Before;
import org.junit.Test;

public class RedmineManagerTest {

	private static final String FILE_EMPTY_ISSUES_XML = "issues_empty_list.xml";

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
			System.out.println("foundIssues: " + foundIssues);

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

		// running with NO API access key set
		Issue issue = new Issue();
		issue.setSubject("test zzx");
		try {
			redmineMgrEmpty.createIssue(PROJECT_KEY, issue);
			fail("Must have failed with '401 Not authorized'");
		} catch (IOException e) {
			fail("Some exception : " + e);
		} catch (AuthenticationException e) {
			System.out.println("Got expected AuthenticationException.");
		}

		// set invalid key
		RedmineManager redmineMgrInvalidKey = new RedmineManager(Config.getHost(), "wrong_key");
		try {
			redmineMgrInvalidKey.createIssue(PROJECT_KEY, issue);
			fail("Must have failed with '401 Not authorized'");
		} catch (IOException e) {
			fail("Some exception : " + e);
		} catch (AuthenticationException e) {
			System.out.println("Got expected AuthenticationException.");
		}

	}

/*	@Test
	public void testParseIssuesFromEmptyXMLUsingRedmineLoader() {
		// issues xml with no individual issues in the list should not break the
		// loader.
		// see http://code.google.com/p/redmine-connect/issues/detail?id=57

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
	public void testParseIssuesFromEmptyXML() {
		// issues xml with no individual issues in the list should not break the
		// loader.
		// see http://code.google.com/p/redmine-connect/issues/detail?id=57

		try {
			String str = MyIOUtils.getResourceAsString(FILE_EMPTY_ISSUES_XML);
			List<Issue> issues = RedmineManager.parseIssuesFromXML(str);
			assertTrue(issues.isEmpty());
		} catch (Exception e) {
			e.printStackTrace();
			fail("Error:" + e);
		}
		
	}
	@Test
	public void testUpdateIssue() {
		try {
			Issue issue = new Issue();
			String originalSubject = "Issue " + new Date();
			issue.setSubject(originalSubject);

			Issue newIssue = mgr.createIssue(PROJECT_KEY, issue);
			String changedSubject = "changed subject";
			newIssue.setSubject(changedSubject);

			mgr.updateIssue(PROJECT_KEY, newIssue);

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

			String queryIdIssuesCreatedLast2Days = Config.getQueryId();
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
	public void testCountIssues() {
		try {
			String xml = MyIOUtils.getResourceAsString("test_issues_big_file.xml");
			List<Issue> issues = RedmineManager.parseIssuesFromXML(xml);
			assertEquals(40, issues.size());
		} catch (Exception e) {
			fail(e.getMessage());
		}

	}
	
	@Test
	public void testCreateIssueNonUnicodeSymbols() {
		String xml;
		try {
			xml = MyIOUtils.getResourceAsString("issues_foreign_symbols.xml");
			List<Issue> issues = RedmineManager.parseIssuesFromXML(xml);
			// must be 1 issue in the file
			assertTrue(issues.size()==1);
			Issue toCreate = issues.get(0);
			Issue created = mgr.createIssue(PROJECT_KEY, toCreate);
			assertEquals("Example with accents Ação", created.getSubject());
			
		} catch (Exception e) {
			fail(e.getMessage());
		}

	}
	
	@Test
	public void testTrackerName() {
		fail("not implemented");
	}

	@Test
	public void testGetProject() {
		fail("not implemented");
	}

	@Test
	public void testParseProjectXML() {
		String xml;
		try {
			xml = MyIOUtils.getResourceAsString("project.xml");
			Project project = RedmineManager.parseProjectFromXML(xml);
			Integer expectedProjectID = 308;
			String expectedName = "taskconnector-test";
			String expectedKey = "taskconnector-test";
			assertEquals(expectedProjectID, project.getId());
			assertEquals(expectedName, project.getName());
			assertEquals(expectedKey, project.getIdentifier());
			
			/**
		    <tracker name="Feature" id="2"/>
		    <tracker name="Support" id="3"/>
		    <tracker name="Bug" id="1"/>
		    <tracker name="Task" id="4"/>
		    */
			List<Tracker> trackers = project.getTrackers();
			assertNotNull("Trackers list must not be NULL", trackers);
			assertEquals(4, trackers.size());
			
			Tracker tracker = project.getTrackerByName("Support");
			assertNotNull("Tracker must be not null", tracker);
			Integer expectedTrackerId = 3;
			assertEquals("checking id of 'support' tracker", expectedTrackerId, tracker.getId());
			
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

}
