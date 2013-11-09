package com.taskadapter.redmineapi;

import com.taskadapter.redmineapi.bean.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;

public class IntegrationTestHelper {
    private static final Logger logger = LoggerFactory.getLogger(IntegrationTestHelper.class);

    public static RedmineManager createRedmineManager() {
        TestConfig testConfig = new TestConfig();
        logger.info("Running Redmine integration tests using: " + testConfig.getURI());
        RedmineManager manager = new RedmineManager(testConfig.getURI());
        manager.setLogin(testConfig.getLogin());
        manager.setPassword(testConfig.getPassword());
        return manager;
    }

    /**
     * @return project key
     */
    public static String createProject(RedmineManager mgr) {
        Project testProject = new Project();
        testProject.setName("test project");
        testProject.setIdentifier("test" + Calendar.getInstance().getTimeInMillis());

        String projectKey = null;
        try {
            Project createdProject = mgr.createProject(testProject);
            projectKey = createdProject.getIdentifier();
        } catch (Exception e) {
            logger.error("Exception while configuring tests", e);
            throw new RuntimeException(e);
        }
        return projectKey;
    }

    public static void deleteProject(RedmineManager mgr, String projectKey) {
        try {
            if (mgr != null && projectKey != null) {
                mgr.deleteProject(projectKey);
            }
        } catch (Exception e) {
            logger.error("Exception while deleting test project", e);
            throw new RuntimeException("can't delete the test project '" + projectKey + ". reason: " + e.getMessage());
        }
    }
}
