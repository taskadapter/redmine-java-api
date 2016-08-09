package com.taskadapter.redmineapi;

import com.taskadapter.redmineapi.bean.Issue;
import com.taskadapter.redmineapi.bean.IssueCategory;
import com.taskadapter.redmineapi.bean.IssueCategoryFactory;
import com.taskadapter.redmineapi.bean.IssueFactory;
import com.taskadapter.redmineapi.bean.IssueRelation;
import com.taskadapter.redmineapi.bean.Project;
import com.taskadapter.redmineapi.bean.ProjectFactory;
import com.taskadapter.redmineapi.bean.Version;
import com.taskadapter.redmineapi.bean.VersionFactory;
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

    @BeforeClass
	public static void oneTimeSetUp() {
        TestConfig testConfig = new TestConfig();
		logger.info("Running redmine tests using: " + testConfig.getURI());
        RedmineManager mgr = IntegrationTestHelper.createRedmineManager();
        issueManager = mgr.getIssueManager();
        projectManager = mgr.getProjectManager();

        Project junitTestProject = ProjectFactory.create("test project", "test" + Calendar.getInstance().getTimeInMillis());


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
		final Project template = ProjectFactory.create("Test name", "key" + Calendar.getInstance().getTimeInMillis());
		final Project result = projectManager.createProject(template);
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
		final Issue template = IssueFactory.create(projectId, "This is a subject");
		template.setStartDate(null);
		final Issue result = issueManager.createIssue(template);
		
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
            issueManager.deleteIssue(result.getId());
		}
	}

	@Test
	public void issueWithStartDateNotSetGetsDefaultValue() throws RedmineException {
		final Issue template = IssueFactory.create(projectId, "Issue with no start date set in code");
		final Issue result = issueManager.createIssue(template);
		try {
			Assert.assertNotNull(result.getStartDate());
		} finally {
			issueManager.deleteIssue(result.getId());
		}
	}

	@Test
	public void issueWithStartDateSetToNullDoesNotGetDefaultValueForStartDate() throws RedmineException {
		final Issue template = IssueFactory.create(projectId, "Issue with NULL start date");
		template.setStartDate(null);
		final Issue result = issueManager.createIssue(template);
		try {
			Assert.assertNull(result.getStartDate());
		} finally {
			issueManager.deleteIssue(result.getId());
		}
	}

	@Test
	public void testRelationDefaults() throws RedmineException {
		final Issue tmp = IssueFactory.create(projectId, "this is a test");
		// TODO why is not everything inside TRY? fix!
		final Issue issue1 = issueManager.createIssue(tmp);
		try {
			final Issue issue2 = issueManager.createIssue(tmp);
			try {
				final IssueRelation relation = issueManager.createRelation(
						issue1.getId(), issue2.getId(), "blocks");
				Assert.assertNotNull(relation.getId());
				Assert.assertEquals(issue1.getId(), relation.getIssueId());
				Assert.assertEquals(issue2.getId(), relation.getIssueToId());
				Assert.assertEquals("blocks", relation.getType());
				Assert.assertEquals(Integer.valueOf(0), relation.getDelay());
			} finally {
				issueManager.deleteIssue(issue2.getId());
			}
		} finally {
			issueManager.deleteIssue(issue1.getId());
		}
	}

	@Test
	public void testVersionDefaults() throws RedmineException {
		final Version template = VersionFactory.create();
		template.setProjectId(projectId);
		template.setName("2.3.4.5");
		final Version version = projectManager.createVersion(template);
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
			projectManager.deleteVersion(version);
		}
	}

	@Test
	public void testCategoryDefaults() throws RedmineException {
		final Project projectByKey = projectManager.getProjectByKey(projectKey);
		final IssueCategory template = IssueCategoryFactory.create(projectByKey.getId(), "test name");
		final IssueCategory category = issueManager.createCategory(template);
		try {
			Assert.assertNotNull(category.getId());
			Assert.assertEquals("test name", category.getName());
			Assert.assertNotNull(category.getProjectId());
			Assert.assertNull(category.getAssigneeId());
		} finally {
			issueManager.deleteCategory(category);
		}
	}
}
