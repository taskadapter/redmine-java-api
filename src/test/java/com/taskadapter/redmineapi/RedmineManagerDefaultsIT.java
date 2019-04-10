package com.taskadapter.redmineapi;

import com.taskadapter.redmineapi.bean.Issue;
import com.taskadapter.redmineapi.bean.IssueCategory;
import com.taskadapter.redmineapi.bean.IssueRelation;
import com.taskadapter.redmineapi.bean.Project;
import com.taskadapter.redmineapi.bean.Version;
import com.taskadapter.redmineapi.internal.Transport;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

/**
 * Tests default redmine manager values in a response. Tries to provides
 * behavior compatible with an XML version.
 */
public class RedmineManagerDefaultsIT {

	private static final Logger logger = LoggerFactory.getLogger(RedmineManagerIT.class);

    private static String projectKey;
    private static int projectId;
    private static IssueManager issueManager;
    private static ProjectManager projectManager;
	private static Transport transport;

	@BeforeClass
	public static void oneTimeSetUp() {
        TestConfig testConfig = new TestConfig();
		logger.info("Running redmine tests using: " + testConfig.getURI());
        RedmineManager mgr = IntegrationTestHelper.createRedmineManager();
        transport = mgr.getTransport();
        issueManager = mgr.getIssueManager();
        projectManager = mgr.getProjectManager();

        Project junitTestProject = new Project(transport, "test project", "test" + Calendar.getInstance().getTimeInMillis());


        try {
			Project createdProject = projectManager.createProject(junitTestProject);
			projectKey = createdProject.getIdentifier();
			projectId = createdProject.getId();
		} catch (Exception e) {
			logger.error("Exception while creating test project", e);
			Assert.fail("can't create a test project. " + e.getMessage());
		}
	}

	@AfterClass
	public static void oneTimeTearDown() {
		try {
			if (projectManager != null && projectKey != null) {
                projectManager.deleteProject(projectKey);
			}
		} catch (Exception e) {
			logger.error("Exception while deleting test project", e);
			Assert.fail("can't delete the test project '" + projectKey
					+ ". reason: " + e.getMessage());
		}
	}

	@Test
	public void testProjectDefaults() throws RedmineException {
		Project template = new Project(transport, "Test name", "key" + Calendar.getInstance().getTimeInMillis());
		Project result = template.create();
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
            projectManager.deleteProject(result.getIdentifier());
		}
	}

	@Test
	public void testIssueDefaults() throws RedmineException {
		Issue result = new Issue(transport, projectId, "This is a subject")
				.setStartDate(null)
				.create();
		
		try {
			Assert.assertNotNull(result.getId());
			Assert.assertEquals("This is a subject", result.getSubject());
			Assert.assertNull(result.getParentId());
			Assert.assertNull(result.getEstimatedHours());
			/* result.getSpentHours() is NULL for Redmine 3.0.0 and is equal to "0.0" for Redmine 2.6.2
			* so we can't really check this because we don't know the Redmine version.
			* Ideally we would want something like
			* if (redmine.getVersion()>=3) {
			*     assertThat()...
			* } else {
			*     assertThat()...
			* }
			*/
			Assert.assertNull(result.getAssigneeId());
			Assert.assertNotNull(result.getPriorityText());
			Assert.assertNotNull(result.getPriorityId());
			Assert.assertEquals(Integer.valueOf(0), result.getDoneRatio());
			Assert.assertNotNull(result.getProjectId());
			Assert.assertNotNull(result.getAuthorId());
			Assert.assertNotNull(result.getAuthorName());
			Assert.assertNull(result.getStartDate());
			Assert.assertNull(result.getDueDate());
			Assert.assertNotNull(result.getTracker());
			assertThat(result.getDescription()).isNull();
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
            result.delete();
		}
	}

	@Test
	public void issueWithStartDateNotSetGetsDefaultValue() throws RedmineException {
		Issue issue = new Issue(transport, projectId).setSubject("Issue with no start date set in code")
				.create();
		try {
			Assert.assertNotNull(issue.getStartDate());
		} finally {
			issue.delete();
		}
	}

	@Test
	public void issueWithStartDateSetToNullDoesNotGetDefaultValueForStartDate() throws RedmineException {
		Issue issue = new Issue(transport, projectId).setSubject("Issue with NULL start date")
				.setStartDate(null)
				.create();
		try {
			Assert.assertNull(issue.getStartDate());
		} finally {
			issue.delete();
		}
	}

	@Test
	public void testRelationDefaults() throws RedmineException {
		Issue issue1 = new Issue(transport, projectId, "this is a test")
				.create();
		// TODO why is not everything inside TRY? fix!
		try {
			Issue issue2 = new Issue(transport, projectId, "this is a test")
					.create();
			try {
				IssueRelation relation = new IssueRelation(transport, issue1.getId(), issue2.getId(), "blocks")
						.create();
				Assert.assertNotNull(relation.getId());
				Assert.assertEquals(issue1.getId(), relation.getIssueId());
				Assert.assertEquals(issue2.getId(), relation.getIssueToId());
				Assert.assertEquals("blocks", relation.getType());
				Assert.assertEquals(Integer.valueOf(0), relation.getDelay());
			} finally {
				issue2.delete();
			}
		} finally {
			issue1.delete();
		}
	}

	@Test
	public void testVersionDefaults() throws RedmineException {
		Version version = new Version(transport, projectId, "2.3.4.5").create();
		try {
			Assert.assertNotNull(version.getId());
			Assert.assertNotNull(version.getProjectId());
			Assert.assertEquals("2.3.4.5", version.getName());
			Assert.assertEquals("", version.getDescription());
			Assert.assertNotNull(version.getStatus());
			Assert.assertNull(version.getDueDate());
			Assert.assertNotNull(version.getCreatedOn());
			Assert.assertNotNull(version.getUpdatedOn());
		} finally {
			version.delete();
		}
	}

	@Test
	public void testCategoryDefaults() throws RedmineException {
		Project projectByKey = projectManager.getProjectByKey(projectKey);
		IssueCategory category = new IssueCategory(transport, projectByKey.getId(), "test name")
				.create();
		try {
			Assert.assertNotNull(category.getId());
			Assert.assertEquals("test name", category.getName());
			Assert.assertNotNull(category.getProjectId());
			Assert.assertNull(category.getAssigneeId());
		} finally {
			category.delete();
		}
	}
}
