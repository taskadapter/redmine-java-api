package com.taskadapter.redmineapi.internal;

import com.taskadapter.redmineapi.DateComparator;
import com.taskadapter.redmineapi.MyIOUtils;
import com.taskadapter.redmineapi.RedmineTestUtils;
import com.taskadapter.redmineapi.bean.Issue;
import com.taskadapter.redmineapi.bean.IssueCategory;
import com.taskadapter.redmineapi.bean.IssueStatus;
import com.taskadapter.redmineapi.bean.News;
import com.taskadapter.redmineapi.bean.Project;
import com.taskadapter.redmineapi.bean.TimeEntry;
import com.taskadapter.redmineapi.bean.TimeEntryActivity;
import com.taskadapter.redmineapi.bean.Tracker;
import com.taskadapter.redmineapi.bean.User;
import com.taskadapter.redmineapi.internal.json.JsonInput;
import com.taskadapter.redmineapi.internal.json.JsonObjectParser;
import org.json.JSONException;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

/**
 * Redmine JSON parser tests.
 */
public class RedmineJSONParserTest {
	private static final String FILE_EMPTY_ISSUES = "issue/issues_empty_list.json";
	private static final String REDMINE_ISSUES = "issue/redmine_issues.json";

	@Test
	public void testParseProject1() throws ParseException, JSONException {
		final String projectString = "{\"project\":{\"created_on\":\"2012/05/11 06:53:21 -0700\",\"updated_on\":\"2012/05/11 06:53:20 -0700\",\"homepage\":\"\",\"trackers\":[{\"name\":\"Bug\",\"id\":1},{\"name\":\"Feature\",\"id\":2},{\"name\":\"Support\",\"id\":3}],\"identifier\":\"test1336744548920\",\"name\":\"test project\",\"id\":6143}}";

		final JsonObjectParser<Project> parser = RedmineJSONParser::parseProject;
		final Project project = parser.parse(RedmineJSONParser.getResponseSingleObject(projectString,"project"));

		Project template = new Project(null).setId(6143);
		template.setIdentifier("test1336744548920");
		template.setName("test project");
		template.setHomepage("");
		template.setCreatedOn(new SimpleDateFormat("dd.MM.yyyy HH:mm:ss Z")
				.parse("11.05.2012 06:53:21 -0700"));
		template.setUpdatedOn(new SimpleDateFormat("dd.MM.yyyy HH:mm:ss Z")
				.parse("11.05.2012 06:53:20 -0700"));
		template.addTrackers(Arrays.asList(
				new Tracker().setId(1).setName("Bug"),
				new Tracker().setId(2).setName("Feature"),
				new Tracker().setId(3).setName("Support")));
		template.setDescription("");
		Assert.assertEquals(template, project);
	}

	@Test
	public void testParseIssuesFromEmptyList() {
		/*
		 * issues result with no individual issues in the list should not break
		 * the loader.
		 */

		try {
			String str = MyIOUtils.getResourceAsString(FILE_EMPTY_ISSUES);
			List<Issue> issues = JsonInput.getListOrEmpty(
					RedmineJSONParser.getResponse(str), "issues",
					RedmineJSONParser::parseIssue);
			Assert.assertTrue(issues.isEmpty());
		} catch (Exception e) {
			e.printStackTrace();
			fail("Error:" + e);
		}
	}

	@Test
	public void testCountIssues() {
		try {
			List<Issue> issues = loadRedmine11Issues();
			Assert.assertEquals(26, issues.size());
		} catch (Exception e) {
			fail(e.getMessage());
		}

	}

	private List<Issue> loadRedmine11Issues() throws IOException, JSONException {
		String json = MyIOUtils.getResourceAsString(REDMINE_ISSUES);
		return JsonInput.getListOrEmpty(RedmineJSONParser.getResponse(json),
				"issues", RedmineJSONParser::parseIssue);
	}

	/* Gson parser is bad at detecting errors :( */
	@Ignore
	@Test
	public void testMailformedProject() throws IOException, JSONException {
		/* Check parser correctness */
		try {
			String json = MyIOUtils
					.getResourceAsString("project/mailformed_redmine_project.json");
			RedmineJSONParser.parseProject(RedmineJSONParser
					.getResponseSingleObject(json, "project"));
		} catch (JSONException e) {
			Assert.assertNotSame("Empty input", e.getMessage());
		}
	}

	@Test
	public void testParseProjectRedmine() throws IOException, JSONException {
		String json = MyIOUtils.getResourceAsString("project/redmine_project.json");
		Project project = RedmineJSONParser.parseProject(RedmineJSONParser
				.getResponseSingleObject(json, "project"));
		Integer expectedProjectID = 23;
		String expectedName = "test project";
		String expectedKey = "test1295649781087";
		Assert.assertEquals(expectedProjectID, project.getId());
		Assert.assertEquals(expectedName, project.getName());
		Assert.assertEquals(expectedKey, project.getIdentifier());

		Collection<Tracker> trackers = project.getTrackers();
		Assert.assertNotNull("Trackers list must not be NULL", trackers);
		Assert.assertEquals(3, trackers.size());

		Tracker tracker = project.getTrackerByName("Support");
		Assert.assertNotNull("Tracker must be not null", tracker);
		Integer expectedTrackerId = 3;
		Assert.assertEquals("checking id of 'support' tracker",
				expectedTrackerId, tracker.getId());
	}

    @Test
    public void testProjectWithCustomField() throws IOException, JSONException {
        String json = MyIOUtils.getResourceAsString("project/redmine_projectWithCustomField.json");
        Project project = RedmineJSONParser.parseProject(RedmineJSONParser
                .getResponseSingleObject(json, "project"));

        Assert.assertEquals(project.getCustomFields().size(), 2);
        String expectedCustomeFieldValue = "Should have a value";
        Assert.assertEquals(expectedCustomeFieldValue, project.getCustomFieldById(1).getValue());
        Assert.assertEquals("", project.getCustomFieldById(6).getValue());
    }

	@Test
	public void testParseProjectNoTracker() throws IOException, JSONException {
		String json = MyIOUtils
				.getResourceAsString("project/redmine_project_no_trackers.json");
		Project project = RedmineJSONParser.parseProject(RedmineJSONParser
				.getResponseSingleObject(json, "project"));
		Collection<Tracker> trackers = project.getTrackers();
		assertThat(trackers).isEmpty();
	}

	@Test
	public void estimatedTimeIsNULL() throws JSONException {
		try {
			List<Issue> issues = loadRedmine11Issues();
			Integer issueID = 52;
			Issue issue52 = RedmineTestUtils.findIssueInList(issues, issueID);
			Assert.assertNotNull(issue52);

			// must be NULL and not "0"
			assertNull("estimated time must be null",
                    issue52.getEstimatedHours());
		} catch (IOException e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void testParseIssueNonUnicodeSymbols() throws IOException,
			JSONException {
		String json = MyIOUtils
				.getResourceAsString("issue/issues_foreign_symbols.json");
		String nonLatinAccentSymbols = "Accent symbols: Ação";
		String nonLatinRussianSymbols = "Russian symbols: Привет";
		List<Issue> issues = JsonInput.getListOrEmpty(
				RedmineJSONParser.getResponse(json), "issues",
				RedmineJSONParser::parseIssue);
		// must be 2 issues in the file
		Assert.assertTrue(issues.size() == 2);
		assertNotNull(RedmineTestUtils.findIssueInList(issues,
				nonLatinRussianSymbols));
		assertNotNull(RedmineTestUtils.findIssueInList(issues,
				nonLatinAccentSymbols));
	}

	@Test
	public void testParseInvalidPage() {
		try {
			String text = MyIOUtils.getResourceAsString("invalid_page.txt");
			RedmineJSONParser.getResponse(text);
			fail("Must have failed with RuntimeException");
		} catch (IOException e) {
			fail(e.getMessage());
		} catch (JSONException e) {
			// success
		}
	}

	@Test
	public void testParseDescription() {
		try {
			List<Issue> issues = loadRedmine11Issues();
			Issue issue65 = RedmineTestUtils.findIssueInList(issues, 65);
			Assert.assertTrue(issue65.getDescription().startsWith(
					"This is the description for the new task."));
			Assert.assertTrue(issue65.getDescription().endsWith(
					"This is the last line."));
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void testParseUsers() throws IOException, JSONException {
		String json = MyIOUtils.getResourceAsString("redmine_users.json");
		List<User> users = JsonInput.getListOrEmpty(
				RedmineJSONParser.getResponse(json), "users",
				RedmineJSONParser::parseUser);
		boolean found = false;
		for (User u : users) {
			if (u.getLogin().equals("dina")) {
				found = true;
			}
		}
		Assert.assertTrue("Admin user must be among all the users", found);
	}

	@Test
	public void testParseIssues() throws IOException, JSONException {
		List<Issue> objects = loadRedmine11Issues();
		Integer issueId = 68;
		Issue issue68 = RedmineTestUtils.findIssueInList(objects, issueId);
		Assert.assertNotNull(issue68);
		Assert.assertEquals(issueId, issue68.getId());
		Integer statusId = 1;
		Assert.assertEquals(statusId, issue68.getStatusId());
		Assert.assertEquals("New", issue68.getStatusName());

		assertThat(issue68.getAuthorId()).isEqualTo(1);
	}

	@Test
	public void testParseTimeEntries() throws IOException, JSONException {
		String xml = MyIOUtils.getResourceAsString("redmine_time_entries.json");
		List<TimeEntry> objects = JsonInput.getListOrEmpty(
				RedmineJSONParser.getResponse(xml), "time_entries",
				RedmineJSONParser::parseTimeEntry);
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

		DateComparator.testLongDate(2011, Calendar.JANUARY, 31, 11, 10, 40,
				"GMT-8", obj2.getCreatedOn());
		DateComparator.testLongDate(2011, Calendar.JANUARY, 31, 11, 12, 32,
				"GMT-8", obj2.getUpdatedOn());

		DateComparator.testShortDate(2011, Calendar.JANUARY, 30,
				obj2.getSpentOn());
	}

	@Test
	public void testMultilineIssueDescription() throws IOException,
			JSONException {
		final String json = MyIOUtils
				.getResourceAsString("issue/issue_with_multiline_description.json");
		final Issue issue = RedmineJSONParser.parseIssue(RedmineJSONParser
				.getResponseSingleObject(json, "issue"));
		Assert.assertEquals(
				"This is a description \nwith more than \n\n\none line.",
				issue.getDescription());
	}

	@Test
	public void testCreatedOn() throws IOException, JSONException {
		List<Issue> redmine11Issues = loadRedmine11Issues();
		Issue issue = RedmineTestUtils.findIssueInList(redmine11Issues, 39);
		DateComparator.testLongDate(2011, Calendar.FEBRUARY, 12, 16, 0, 31,
				"GMT-8", issue.getCreatedOn());
	}

	@Test
	public void testUpdatedOn() throws IOException, JSONException {
		List<Issue> redmine11Issues = loadRedmine11Issues();
		Issue issue = RedmineTestUtils.findIssueInList(redmine11Issues, 39);
		DateComparator.testLongDate(2011, Calendar.SEPTEMBER, 17, 21, 28, 45,
				"GMT-8", issue.getUpdatedOn());
	}

	/**
	 * regression test for
	 * http://code.google.com/p/redmine-java-api/issues/detail?id=91 with
	 * Redmine 1.3.0: NULL value returned by getIssues call is interpreted as
	 * 0.0
	 */
	@Test
	public void nullEstimatedTimeProcessedCorrectlyWithRedmine122() {
		try {
			String str = MyIOUtils
					.getResourceAsString("issue/redmine_1.2.2_dev_issues.json");
			List<Issue> issues = JsonInput.getListOrEmpty(
					RedmineJSONParser.getResponse(str), "issues",
					RedmineJSONParser::parseIssue);
			Issue issue = RedmineTestUtils.findIssueInList(issues, 4808);
			assertNull(issue.getEstimatedHours());
			Issue issue1 = RedmineTestUtils.findIssueInList(issues, 4809);
			assertNull(issue1.getEstimatedHours());
		} catch (Exception e) {
			e.printStackTrace();
			fail("Error:" + e);
		}
	}

	@Test
	public void issueStatusesCanBeParsed() {
		try {
			String str = MyIOUtils
					.getResourceAsString("redmine_issue_statuses.json");
			List<IssueStatus> statuses = JsonInput.getListOrEmpty(
					RedmineJSONParser.getResponse(str), "statuses",
					RedmineJSONParser::parseStatus);
			assertEquals(6, statuses.size());
			IssueStatus status5 = statuses.get(4);
			assertEquals(new Integer(5), status5.getId());
			assertEquals("Closed", status5.getName());
			assertEquals(false, status5.isDefaultStatus());
			assertEquals(true, status5.isClosed());
		} catch (Exception e) {
			e.printStackTrace();
			fail("Error:" + e);
		}

	}

	@Test
	public void doesNotFailWithNoNews() {
		// "news" xml with no items in the list should not break the loader.
		try {
			String str = MyIOUtils
					.getResourceAsString("news/redmine_news_empty.json");
			List<News> news = JsonInput.getListOrEmpty(
					RedmineJSONParser.getResponse(str), "news",
					RedmineJSONParser::parseNews);
			Assert.assertTrue(news.isEmpty());
		} catch (Exception e) {
			fail("Error:" + e);
		}
	}

	@Test
	public void parses2NewsItems() {
		// "news" xml with no items in the list should not break the loader.
		try {
			String str = MyIOUtils
					.getResourceAsString("news/redmine_news_2_items.json");
			List<News> news = JsonInput.getListOrEmpty(
					RedmineJSONParser.getResponse(str), "news",
					RedmineJSONParser::parseNews);
			assertEquals(2, news.size());

			News anyItem = news.get(0);
			assertThat(anyItem.getUser().getFirstName()).isEqualTo("rest");
			assertThat(anyItem.getUser().getLastName()).isEqualTo("last");
		} catch (Exception e) {
			fail("Error:" + e);
		}
	}

	@Test
	public void parsesTimeEntryActivities() {
		try {
			String str = MyIOUtils
					.getResourceAsString("redmine_time_entry_activities.json");
			List<TimeEntryActivity> activities = JsonInput.getListOrEmpty(
					RedmineJSONParser.getResponse(str),
					"time_entry_activities",
					RedmineJSONParser::parseTimeEntryActivity);
			assertEquals(2, activities.size());

			assertEquals(8, (long) activities.get(0).getId());
			assertEquals("Design", activities.get(0).getName());

		} catch (Exception e) {
			fail("Error:" + e);
		}
	}

    @Test
    public void testVersionWithCustomField() throws IOException, JSONException {
		String json = MyIOUtils
			.getResourceAsString("redmine_versionWithCustomField.json");
		Project version = RedmineJSONParser
			.parseProject(RedmineJSONParser.getResponseSingleObject(json,
				"version"));
	
		Assert.assertEquals(version.getCustomFields().size(), 2);
		String expectedCustomeFieldValue = "Should have a value";
		Assert.assertEquals(expectedCustomeFieldValue, version
			.getCustomFieldById(1).getValue());
		Assert.assertEquals("", version.getCustomFieldById(6).getValue());
    }
    
    @Test
    public void testAssigneeParserIssue() throws IOException, JSONException {
        String json = MyIOUtils
			.getResourceAsString("issue/issue_with_assignee.json");
        Issue issue = RedmineJSONParser.parseIssue(RedmineJSONParser.getResponse(json));
        assertThat(issue.getAssigneeId()).isEqualTo(3);
        assertThat(issue.getAssigneeName()).isEqualTo("Dina TheDog");
    }
    
    @Test
    public void testAssigneeParserCategories() throws IOException, JSONException {
        String json = MyIOUtils.getResourceAsString("issue_categories.json");
        List<IssueCategory> categories = JsonInput.getListOrEmpty(
                                RedmineJSONParser.getResponse(json),
				"issue_categories",
                                RedmineJSONParser::parseCategory);
        
        // Basic category structure
        assertThat(categories).hasSize(2);
        assertThat(categories.get(0).getId()).isEqualTo(112);
        assertThat(categories.get(0).getName()).isEqualTo("test");
        assertThat(categories.get(1).getId()).isEqualTo(113);
        assertThat(categories.get(1).getName()).isEqualTo("test2");
        
        // First category has assigne with id 1 and name "Redmine Admin"
        assertThat(categories.get(0).getAssigneeId()).isEqualTo(1);
        assertThat(categories.get(0).getAssigneeName()).isEqualTo("Redmine Admin");
        
        // Second category has no assignee
        assertNull(categories.get(1).getAssigneeId());
    }
}
