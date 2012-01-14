package org.redmine.ta.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.redmine.ta.MyIOUtils;
import org.redmine.ta.RedmineTestUtils;
import org.redmine.ta.beans.*;
import org.redmine.ta.internal.logging.Logger;
import org.redmine.ta.internal.logging.LoggerFactory;

public class RedmineXMLParserTest {
    private static final String REDMINE_1_1_ISSUES_XML = "redmine_1_1_issues.xml";
    private static final String REDMINE_1_2_2_DEV_ISSUES_XML = "redmine_1.2.2_dev_issues.xml";
    private static final String FILE_EMPTY_ISSUES_XML = "issues_empty_list.xml";
    private static final String REDMINE_1_3_0_ISSUE_STATUSES_XML = "redmine_1.3.0_issue_statuses.xml";

    private static Logger logger = LoggerFactory.getLogger(RedmineXMLParserTest.class);

    @Test
    public void testParseIssuesFromEmptyXML() {
        // issues xml with no individual issues in the list should not break the
        // loader.
        // see http://code.google.com/p/redmine-connect/issues/detail?id=57

        try {
            String str = MyIOUtils.getResourceAsString(FILE_EMPTY_ISSUES_XML);
            List<Issue> issues = RedmineXMLParser.parseObjectsFromXML(Issue.class, str);
            Assert.assertTrue(issues.isEmpty());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("Error:" + e);
        }

    }

    @Test
    public void testCountIssues() {
        try {
            List<Issue> issues = loadRedmine11IssuesXml();
            Assert.assertEquals(26, issues.size());
        } catch (Exception e) {
            Assert.fail(e.getMessage());
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
            Assert.assertEquals(expectedProjectID, project.getId());
            Assert.assertEquals(expectedName, project.getName());
            Assert.assertEquals(expectedKey, project.getIdentifier());

            /**
             * <tracker name="Feature" id="2"/> <tracker name="Support" id="3"/>
             * <tracker name="Bug" id="1"/> <tracker name="Task" id="4"/>
             */
            List<Tracker> trackers = project.getTrackers();
            Assert.assertNotNull("Trackers list must not be NULL", trackers);
            Assert.assertEquals(3, trackers.size());

            Tracker tracker = project.getTrackerByName("Support");
            Assert.assertNotNull("Tracker must be not null", tracker);
            Integer expectedTrackerId = 3;
            Assert.assertEquals("checking id of 'support' tracker", expectedTrackerId,
                    tracker.getId());

        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testParseProjectNoTrackerXML() {
        String xml;
        try {
            xml = MyIOUtils.getResourceAsString("redmine_1_1_project_no_trackers.xml");
            Project project = RedmineXMLParser.parseProjectFromXML(xml);
            List<Tracker> trackers = project.getTrackers();
            Assert.assertNull("Trackers list must be NULL", trackers);
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testParseIssuesTotalCount() {
        String tmp = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><issues type=\"array\" limit=\"25\" total_count=\"155\" offset=\"0\">....";
        int x = RedmineXMLParser.parseObjectsTotalCount(tmp);
        Assert.assertEquals(155, x);
    }

    @Test
    public void testParseProjectsTotalCount() {
        String tmp = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><projects type=\"array\" total_count=\"112\" limit=\"25\" offset=\"0\"><project><";
        int x = RedmineXMLParser.parseObjectsTotalCount(tmp);
        Assert.assertEquals(112, x);
    }

    @Test
    public void estimatedTimeIsNULL() {
        try {
            List<Issue> issues = loadRedmine11IssuesXml();
            Integer issueID = 52;
            Issue issue52 = RedmineTestUtils.findIssueInList(issues, issueID);
            Assert.assertNotNull(issue52);

            // must be NULL and not "0"
            Assert.assertNull("estimated time must be null",
                    issue52.getEstimatedHours());
        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testParseIssueNonUnicodeSymbols() throws IOException {
        String xml = MyIOUtils
                .getResourceAsString("issues_foreign_symbols.xml");
        String nonLatinAccentSymbols = "Accent symbols: Ação";
        String nonLatinRussianSymbols = "Russian symbols: Привет";
        List<Issue> issues = RedmineXMLParser.parseObjectsFromXML(Issue.class, xml);
        // must be 2 issues in the file
        Assert.assertTrue(issues.size() == 2);
        assertNotNull(RedmineTestUtils.findIssueInList(issues, nonLatinRussianSymbols));
        assertNotNull(RedmineTestUtils.findIssueInList(issues, nonLatinAccentSymbols));
    }

    @Test
    public void testParseInvalidPage() {
        try {
            String text = MyIOUtils
                    .getResourceAsString("invalid_page.txt");
            RedmineXMLParser.parseObjectsFromXML(Issue.class, text);
            Assert.fail("Must have failed with RuntimeException");
        } catch (IOException e) {
            Assert.fail(e.getMessage());
        } catch (RuntimeException e) {
            logger.debug("Got expected RuntimeException when parsing invalid xml");
        }
    }

    @Test
    public void testParseDescription() {
        try {
            List<Issue> issues = loadRedmine11IssuesXml();
            Issue issue65 = RedmineTestUtils.findIssueInList(issues, 65);
            Assert.assertTrue(issue65.getDescription().startsWith("This is the description for the new task."));
            Assert.assertTrue(issue65.getDescription().endsWith("This is the last line."));
        } catch (Exception e) {
            Assert.fail(e.getMessage());
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
        Assert.assertTrue("Admin user must be among all the users", found);
    }

    @Test
    public void testParseIssues() throws IOException {
        List<Issue> objects = loadRedmine11IssuesXml();
        Integer issueId = 68;
        Issue issue68 = RedmineTestUtils.findIssueInList(objects, issueId);
        Assert.assertNotNull(issue68);
        Assert.assertEquals(issueId, issue68.getId());
        Integer statusId = 1;
        Assert.assertEquals(statusId, issue68.getStatusId());
        Assert.assertEquals("New", issue68.getStatusName());

        User author = issue68.getAuthor();
        Assert.assertNotNull(author);
        Integer userId = 1;
        Assert.assertEquals(userId, author.getId());
    }

    @Test
    public void testParseTimeEntries() throws IOException {
        String xml = MyIOUtils.getResourceAsString("redmine_1_1_time_entries.xml");
        List<TimeEntry> objects = RedmineXMLParser.parseTimeEntries(xml);
        for (TimeEntry timeEntry : objects) {
            logger.debug("Parsed time entry " + timeEntry);
        }
        Integer objId = 2;
        TimeEntry obj2 = RedmineTestUtils.findTimeEntry(objects, objId);
        Assert.assertNotNull(obj2);

        Integer expectedIssueId = 44;
        String expectedProjectName = "Permanent test project for Task Adapter";
        Integer expectedProjectId = 1;
        String expectedUserName = "Redmine Admin";
        Integer expectedUserId = 1;
        String expectedActivityName = "Design";
        Integer expectedActivityId = 8;
        Float expectedHours = 2f;

        Assert.assertEquals(objId, obj2.getId());
        Assert.assertEquals(expectedIssueId, obj2.getIssueId());
        Assert.assertEquals(expectedProjectName, obj2.getProjectName());
        Assert.assertEquals(expectedProjectId, obj2.getProjectId());
        Assert.assertEquals(expectedUserName, obj2.getUserName());
        Assert.assertEquals(expectedUserId, obj2.getUserId());
        Assert.assertEquals(expectedActivityName, obj2.getActivityName());
        Assert.assertEquals(expectedActivityId, obj2.getActivityId());
        Assert.assertEquals(expectedHours, obj2.getHours());
        Assert.assertEquals("spent 2 hours working on ABC", obj2.getComment());

        MyIOUtils.testLongDate(2011, Calendar.JANUARY, 31, 11, 10, 40, "GMT-8", obj2.getCreatedOn());
        MyIOUtils.testLongDate(2011, Calendar.JANUARY, 31, 11, 12, 32, "GMT-8", obj2.getUpdatedOn());

        MyIOUtils.testShortDate(2011, Calendar.JANUARY, 30, obj2.getSpentOn());
    }

    @Test
    public void testMultilineIssueDescription() throws IOException {
        final String xml = MyIOUtils
                .getResourceAsString("chiliproject_2_0_0_issue_with_multiline_description.xml");
        final Issue issue = RedmineXMLParser.parseObjectFromXML(Issue.class, xml);
        Assert.assertEquals("This is a description \nwith more than \n\n\none line.",
                issue.getDescription());
    }

    @Test
    public void testCreatedOn() throws IOException {
        List<Issue> redmine11Issues = loadRedmine11IssuesXml();
        Issue issue = RedmineTestUtils.findIssueInList(redmine11Issues, 39);
        MyIOUtils.testLongDate(2011, Calendar.FEBRUARY, 12, 16, 00, 31, "GMT-8", issue.getCreatedOn());
    }

    @Test
    public void testUpdatedOn() throws IOException {
        List<Issue> redmine11Issues = loadRedmine11IssuesXml();
        Issue issue = RedmineTestUtils.findIssueInList(redmine11Issues, 39);
        MyIOUtils.testLongDate(2011, Calendar.SEPTEMBER, 17, 21, 28, 45, "GMT-8", issue.getUpdatedOn());
    }


    private List<Issue> loadRedmine11IssuesXml() throws IOException {
        String xml = MyIOUtils.getResourceAsString(REDMINE_1_1_ISSUES_XML);
        return RedmineXMLParser.parseObjectsFromXML(Issue.class, xml);
    }

    /**
     * regression test for http://code.google.com/p/redmine-java-api/issues/detail?id=91
     * with Redmine 1.3.0: NULL value returned by getIssues call is interpreted as 0.0
     */
    @Test
    public void nullEstimatedTimeProcessedCorrectlyWithRedmine122() {
        try {
            String str = MyIOUtils.getResourceAsString(REDMINE_1_2_2_DEV_ISSUES_XML);
            List<Issue> issues = RedmineXMLParser.parseObjectsFromXML(Issue.class, str);
            Issue issue = RedmineTestUtils.findIssueInList(issues, 4808);
            assertNull(issue.getEstimatedHours());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("Error:" + e);
        }
    }

    @Test
    public void issueStatusesCanBeParsed() {
        try {
            String str = MyIOUtils.getResourceAsString(REDMINE_1_3_0_ISSUE_STATUSES_XML);
            List<IssueStatus> statuses = RedmineXMLParser.parseObjectsFromXML(IssueStatus.class, str);
            assertEquals(6, statuses.size());
            IssueStatus status5 = statuses.get(4);
            assertEquals(new Integer(5), status5.getId());
            assertEquals("Closed", status5.getName());
            assertEquals(false, status5.isDefaultStatus());
            assertEquals(true, status5.isClosed());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("Error:" + e);
        }

    }

}
