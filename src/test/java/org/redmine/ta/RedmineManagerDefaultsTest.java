package org.redmine.ta;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.redmine.ta.beans.Project;
import org.redmine.ta.beans.Tracker;
import org.redmine.ta.internal.logging.Logger;
import org.redmine.ta.internal.logging.LoggerFactory;

/**
 * Tests "default" redmine manager values in a responce. Tries to provides
 * behavior compatible with an XML version, see issue 29,
 * https://github.com/redminedev/redmine-java-api/issues/29
 * 
 * @author maxkar
 * 
 */
public class RedmineManagerDefaultsTest {

	private static final Logger logger = LoggerFactory
			.getLogger(RedmineManagerTest.class);

	private static RedmineManager mgr;

	private static String projectKey;
	private static TestConfig testConfig;

	@BeforeClass
	public static void oneTimeSetUp() {
		testConfig = new TestConfig();
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
			logger.error(e, "Exception while creating test project");
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
			logger.error(e, "Exception while deleting test project");
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
}
