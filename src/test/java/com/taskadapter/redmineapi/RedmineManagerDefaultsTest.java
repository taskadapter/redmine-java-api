package com.taskadapter.redmineapi;

import java.util.Calendar;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import com.taskadapter.redmineapi.bean.Issue;
import com.taskadapter.redmineapi.bean.IssueCategory;
import com.taskadapter.redmineapi.bean.IssueRelation;
import com.taskadapter.redmineapi.bean.Project;
import com.taskadapter.redmineapi.bean.TimeEntry;
import com.taskadapter.redmineapi.bean.User;
import com.taskadapter.redmineapi.bean.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests "default" redmine manager values in a response. Tries to provides
 * behavior compatible with an XML version, see issue 29,
 * https://github.com/redminedev/redmine-java-api/issues/29
 * 
 */
public class RedmineManagerDefaultsTest {
	private static final Integer ACTIVITY_ID = 8;

	private static final Logger logger = LoggerFactory.getLogger(RedmineManagerTest.class);

	private static RedmineManager mgr;

	private static String projectKey;

  @BeforeClass
	public static void oneTimeSetUp() {
    TestConfig testConfig = new TestConfig();
		logger.info("Running redmine tests using: " + testConfig.getURI());
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
			logger.error("Exception while creating test project", e);
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
			logger.error("Exception while deleting test project", e);
			Assert.fail("can't delete the test project '" + projectKey
					+ ". reason: " + e.getMessage());
		}
	}

	@Test
	public void testProjectDefaults() throws RedmineException {
		final Project template = new Project();
		template.setName("Test name");
		template.setIdentifier("test"
				+ Calendar.getInstance().getTimeInMillis());
		final Project result = mgr.createProject(template);
		try {
			Assert.assertNotNull(result.getId());
			Assert.assertEquals(template.getIdentifier(),
					result.getIdentifier());
			Assert.assertEquals("Test name", result.getName());
			Assert.assertEquals("", result.getDescription());
			Assert.assertEquals("", result.getHomepage());
			Assert.assertNotNull(result.getCreatedOn());
			Assert.assertNotNull(result.getUpdatedOn());
			Assert.assertNotNull(result.getTrackers());
			Assert.assertNull(result.getParentId());
		} finally {
			mgr.deleteProject(result.getIdentifier());
		}
	}

	@Test
	public void testIssueDefaults() throws RedmineException {
		final Issue template = new Issue();
		template.setSubject("This is a subject");
		final Issue result = mgr.createIssue(projectKey, template);
		
		try {
			Assert.assertNotNull(result.getId());
			Assert.assertEquals("This is a subject", result.getSubject());
			Assert.assertNull(result.getParentId());
			Assert.assertNull(result.getEstimatedHours());
			Assert.assertEquals(Float.valueOf(0), result.getSpentHours());
			Assert.assertNull(result.getAssignee());
			Assert.assertNotNull(result.getPriorityText());
			Assert.assertNotNull(result.getPriorityId());
			Assert.assertEquals(Integer.valueOf(0), result.getDoneRatio());
			Assert.assertNotNull(result.getProject());
			Assert.assertNotNull(result.getAuthor());
			Assert.assertNull(result.getStartDate());
			Assert.assertNull(result.getDueDate());
			Assert.assertNotNull(result.getTracker());
			Assert.assertEquals("", result.getDescription());
			Assert.assertNotNull(result.getCreatedOn());
			Assert.assertNotNull(result.getUpdatedOn());
			Assert.assertNotNull(result.getStatusId());
			Assert.assertNotNull(result.getStatusName());
			Assert.assertNull(result.getTargetVersion());
			Assert.assertNull(result.getCategory());
			Assert.assertNull(result.getNotes());
			Assert.assertNotNull(result.getCustomFields());
			Assert.assertNotNull(result.getJournals());
			Assert.assertNotNull(result.getRelations());
			Assert.assertNotNull(result.getAttachments());
		} finally {
			mgr.deleteIssue(result.getId());
		}
	}

	@Test
	public void testUserDefaults() throws RedmineException {
		final User template = new User();
		template.setFirstName("first name");
		template.setLastName("last name");
		template.setMail("root@globalhost.ru");
		template.setPassword("aslkdj32jnrfds7asdfn23()[]:kajsdf");
		template.setLogin("asdNnadnNasd");
		final User result = mgr.createUser(template);
		try {
			Assert.assertNotNull(result.getId());
			Assert.assertEquals("asdNnadnNasd", result.getLogin());
			Assert.assertNull(result.getPassword());
			Assert.assertEquals("first name", result.getFirstName());
			Assert.assertEquals("last name", result.getLastName());
			Assert.assertEquals("root@globalhost.ru", result.getMail());
			Assert.assertNotNull(result.getCreatedOn());
			Assert.assertNull(result.getLastLoginOn());
			Assert.assertNotNull(result.getCustomFields());
		} finally {
			mgr.deleteUser(result.getId());
		}
	}

	@Test
	public void testTimeEntryDefaults() throws RedmineException {
		final TimeEntry template = new TimeEntry();

		final Issue tmp = new Issue();
		tmp.setSubject("aaabbbccc");
		final Issue tmpIssue = mgr.createIssue(projectKey, tmp);
		try {
			template.setHours(123.f);
			template.setActivityId(ACTIVITY_ID);
			template.setIssueId(tmpIssue.getId());
			final TimeEntry result = mgr.createTimeEntry(template);
			try {
				Assert.assertNotNull(result.getId());
				Assert.assertNotNull(result.getIssueId());
				Assert.assertNotNull(result.getProjectId());
				Assert.assertNotNull(result.getProjectName());
				Assert.assertNotNull(result.getUserName());
				Assert.assertNotNull(result.getUserId());
				Assert.assertNotNull(result.getActivityName());
				Assert.assertNotNull(result.getActivityId());
				Assert.assertEquals(Float.valueOf(123.0f), result.getHours());
				Assert.assertEquals("", result.getComment());
				Assert.assertNotNull(result.getSpentOn());
				Assert.assertNotNull(result.getCreatedOn());
				Assert.assertNotNull(result.getUpdatedOn());
			} finally {
				mgr.deleteTimeEntry(result.getId());
			}
		} finally {
			mgr.deleteIssue(tmpIssue.getId());
		}
	}

	@Test
	public void testRelationDefaults() throws RedmineException {
		final Issue tmp = new Issue();
		tmp.setSubject("this is a test");
		final Issue issue1 = mgr.createIssue(projectKey, tmp);
		try {
			final Issue issue2 = mgr.createIssue(projectKey, tmp);
			try {
				final IssueRelation relation = mgr.createRelation(
						issue1.getId(), issue2.getId(), "blocks");
				Assert.assertNotNull(relation.getId());
				Assert.assertEquals(issue1.getId(), relation.getIssueId());
				Assert.assertEquals(issue2.getId(), relation.getIssueToId());
				Assert.assertEquals("blocks", relation.getType());
				Assert.assertEquals(Integer.valueOf(0), relation.getDelay());
			} finally {
				mgr.deleteIssue(issue2.getId());
			}
		} finally {
			mgr.deleteIssue(issue1.getId());
		}
	}

	@Test
	public void testVersionDefaults() throws RedmineException {
		final Version template = new Version();
		template.setProject(mgr.getProjectByKey(projectKey));
		template.setName("2.3.4.5");
		final Version version = mgr.createVersion(template);
		try {
			Assert.assertNotNull(version.getId());
			Assert.assertNotNull(version.getProject());
			Assert.assertEquals("2.3.4.5", version.getName());
			Assert.assertEquals("", version.getDescription());
			Assert.assertNotNull(version.getStatus());
			Assert.assertNull(version.getDueDate());
			Assert.assertNotNull(version.getCreatedOn());
			Assert.assertNotNull(version.getUpdatedOn());
		} finally {
			mgr.deleteVersion(version);
		}
	}

	@Test
	public void testCategoryDefaults() throws RedmineException {
		final IssueCategory template = new IssueCategory();
		template.setProject(mgr.getProjectByKey(projectKey));
		template.setName("test name");
		final IssueCategory category = mgr.createCategory(template);
		try {
			Assert.assertNotNull(category.getId());
			Assert.assertEquals("test name", category.getName());
			Assert.assertNotNull(category.getProject());
			Assert.assertNull(category.getAssignee());
		} finally {
			mgr.deleteCategory(category);
		}
	}
}
