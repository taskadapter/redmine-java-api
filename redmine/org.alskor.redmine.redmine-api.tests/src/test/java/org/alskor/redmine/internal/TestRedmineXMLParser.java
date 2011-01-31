package org.alskor.redmine.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;

import org.alskor.redmine.MyIOUtils;
import org.alskor.redmine.beans.Issue;
import org.alskor.redmine.beans.Project;
import org.alskor.redmine.beans.TimeEntry;
import org.alskor.redmine.beans.Tracker;
import org.alskor.redmine.beans.User;
import org.junit.Test;

public class TestRedmineXMLParser {
	private static final String REDMINE_1_1_ISSUES_XML = "redmine_1_1_issues.xml";
	private static final String FILE_EMPTY_ISSUES_XML = "issues_empty_list.xml";

	@Test
	public void testParseIssuesFromEmptyXML() {
		// issues xml with no individual issues in the list should not break the
		// loader.
		// see http://code.google.com/p/redmine-connect/issues/detail?id=57

		try {
			String str = MyIOUtils.getResourceAsString(FILE_EMPTY_ISSUES_XML);
			List<Issue> issues = RedmineXMLParser.parseIssuesFromXML(str);
			assertTrue(issues.isEmpty());
		} catch (Exception e) {
			e.printStackTrace();
			fail("Error:" + e);
		}

	}

	@Test
	public void testCountIssues() {
		try {
			String xml = MyIOUtils
					.getResourceAsString(REDMINE_1_1_ISSUES_XML);
			List<Issue> issues = RedmineXMLParser.parseIssuesFromXML(xml);
			assertEquals(26, issues.size());
		} catch (Exception e) {
			fail(e.getMessage());
		}

	}

/*	@Test
	public void testParseProjectXMLRedmine_1_0() {
		String xml;
		try {
			xml = MyIOUtils.getResourceAsString("project.xml");
			Project project = RedmineXMLParser.parseProjectFromXML(xml);
			Integer expectedProjectID = 308;
			String expectedName = "taskconnector-test";
			String expectedKey = "taskconnector-test";
			assertEquals(expectedProjectID, project.getId());
			assertEquals(expectedName, project.getName());
			assertEquals(expectedKey, project.getIdentifier());

			List<Tracker> trackers = project.getTrackers();
			assertNotNull("Trackers list must not be NULL", trackers);
			assertEquals(4, trackers.size());

			Tracker tracker = project.getTrackerByName("Support");
			assertNotNull("Tracker must be not null", tracker);
			Integer expectedTrackerId = 3;
			assertEquals("checking id of 'support' tracker", expectedTrackerId,
					tracker.getId());

		} catch (Exception e) {
			fail(e.getMessage());
		}
	}*/

	@Test
	public void testParseProjectXMLRedmine_1_1() {
		String xml;
		try {
			xml = MyIOUtils.getResourceAsString("redmine_1_1_project.xml");
			Project project = RedmineXMLParser.parseProjectFromXML(xml);
			Integer expectedProjectID = 23;
			String expectedName = "test project";
			String expectedKey = "test1295649781087";
			assertEquals(expectedProjectID, project.getId());
			assertEquals(expectedName, project.getName());
			assertEquals(expectedKey, project.getIdentifier());

			/**
			 * <tracker name="Feature" id="2"/> <tracker name="Support" id="3"/>
			 * <tracker name="Bug" id="1"/> <tracker name="Task" id="4"/>
			 */
			List<Tracker> trackers = project.getTrackers();
			assertNotNull("Trackers list must not be NULL", trackers);
			assertEquals(3, trackers.size());

			Tracker tracker = project.getTrackerByName("Support");
			assertNotNull("Tracker must be not null", tracker);
			Integer expectedTrackerId = 3;
			assertEquals("checking id of 'support' tracker", expectedTrackerId,
					tracker.getId());

		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void testParseProjectNoTrackerXML() {
		String xml;
		try {
			xml = MyIOUtils.getResourceAsString("redmine_1_1_project_no_trackers.xml");
			Project project = RedmineXMLParser.parseProjectFromXML(xml);
			List<Tracker> trackers = project.getTrackers();
			assertNull("Trackers list must be NULL", trackers);
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void testParseIssuesTotalCount() {
		String tmp = "...xml... <issues type=\"array\" limit=\"25\" total_count=\"155\" offset=\"0\">....";
		int x = RedmineXMLParser.parseIssuesTotalCount(tmp);
		assertEquals(155, x);
		System.out
				.println("success: parsed total_count attribute from issues xml. value= "
						+ x);
	}

	@Test
	public void testNullEstimatedTime() {
		String str;
		try {
			str = MyIOUtils.getResourceAsString(REDMINE_1_1_ISSUES_XML);
			List<Issue> issues = RedmineXMLParser.parseIssuesFromXML(str);
			Integer issueID = 52;
			Issue issue52 = findIssueInList(issues, issueID);
			assertNotNull(issue52);

			// must be NULL and not "0"
			assertNull("estimated time must be null",
					issue52.getEstimatedHours());
		} catch (IOException e) {
			fail(e.getMessage());
		}
	}

	private static Issue findIssueInList(List<Issue> list, Integer id) {
		Issue result = null;
		for (Issue issue : list) {
			if (issue.getId().equals(id)) {
				result = issue;
			}
		}
		return result;
	}

	private static TimeEntry findTimeEntry(List<TimeEntry> list, Integer id) {
		TimeEntry result = null;
		for (TimeEntry obj : list) {
			if (obj.getId().equals(id)) {
				result = obj;
			}
		}
		return result;
	}
	
	@Test
	public void testParseIssueNonUnicodeSymbols() {
		try {
			String xml = MyIOUtils
					.getResourceAsString("issues_foreign_symbols.xml");

			String nonLatinSymbols = "Example with accents Ação";
			List<Issue> issues = RedmineXMLParser.parseIssuesFromXML(xml);
			// must be 1 issue in the file
			assertTrue(issues.size() == 1);
			Issue issue = issues.get(0);
			assertEquals(nonLatinSymbols, issue.getSubject());

		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testParseInvalidPage() {
		try {
			String text = MyIOUtils
					.getResourceAsString("invalid_page.txt");
			RedmineXMLParser.parseIssuesFromXML(text);
			fail("Must have failed with RuntimeException");
		} catch (IOException e) {
			fail(e.getMessage());
		} catch (RuntimeException e) {
			System.out.println("Got expected RuntimeException when parsing invalid xml");
		}
	}
	
	@Test
	public void testParseDescription() {
		try {
			String xml = MyIOUtils
					.getResourceAsString(REDMINE_1_1_ISSUES_XML);
			List<Issue> issues = RedmineXMLParser.parseIssuesFromXML(xml);
			Issue issue65 = findIssueInList(issues, 65);
			assertTrue(issue65.getDescription().startsWith("This is the description for the new task."));
			assertTrue(issue65.getDescription().endsWith("This is the last line."));
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testParseUsersRedmine_1_1() throws IOException {
		String xml = MyIOUtils.getResourceAsString("redmine_1_1_users.xml");
		List<User> users = RedmineXMLParser.parseUsersFromXML(xml);
		boolean found = false;
		for (User u : users) {
			if (u.getLogin().equals("dina")) {
				found = true;
			}
		}
		assertTrue("Admin user must be among all the users", found);
	}

	@Test
	public void testParse123() throws IOException {
		String xml = MyIOUtils.getResourceAsString("redmine_1_1_issues.xml");
		List<Issue> objects = RedmineXMLParser.parseIssuesFromXML(xml);
		Integer issueId = 68;
		Issue issue68 = findIssueInList(objects, issueId);
		assertNotNull(issue68);
		assertEquals(issueId, issue68.getId());
		
		User author = issue68.getAuthor();
		assertNotNull(author);
		Integer userId = 1;
		assertEquals(userId, author.getId());
	}

	@Test
	public void testParseTimeEntries() throws IOException {
		String xml = MyIOUtils.getResourceAsString("redmine_1_1_time_entries.xml");
		List<TimeEntry> objects = RedmineXMLParser.parseTimeEntries(xml);
		for (TimeEntry timeEntry : objects) {
			System.out.println(timeEntry);
		}
		Integer objId = 2;
		TimeEntry obj2 = findTimeEntry(objects, objId);
		assertNotNull(obj2);
		
		Integer expectedIssueId = 44;
		String expectedProjectName = "Permanent test project for Task Adapter";
		Integer expectedProjectId = 1;
		String expectedUserName ="Redmine Admin";
		Integer expectedUserId = 1;
		String expectedActivityName = "Design";
		Integer expectedActivityId = 8;
		Float expectedHours = 2f;
		
		assertEquals(objId, obj2.getId());
		assertEquals(expectedIssueId, obj2.getIssueId());
		assertEquals(expectedProjectName, obj2.getProjectName());
		assertEquals(expectedProjectId, obj2.getProjectId());
		assertEquals(expectedUserName, obj2.getUserName());
		assertEquals(expectedUserId, obj2.getUserId());
		assertEquals(expectedActivityName, obj2.getActivityName());
		assertEquals(expectedActivityId, obj2.getActivityId());
		assertEquals(expectedHours, obj2.getHours());
		assertEquals("spent 2 hours working on ABC", obj2.getComment());
		
		MyIOUtils.testLongDate(obj2.getCreatedOn(), 2011, Calendar.JANUARY, 31, 11, 10, 40, "GMT-8");
		MyIOUtils.testLongDate(obj2.getUpdatedOn(), 2011, Calendar.JANUARY, 31, 11, 12, 32, "GMT-8");

		MyIOUtils.testShortDate(obj2.getSpentOn(), 2011, Calendar.JANUARY, 30);
	}
}
