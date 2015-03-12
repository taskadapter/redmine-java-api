package com.taskadapter.redmineapi;

import com.taskadapter.redmineapi.bean.Project;
import com.taskadapter.redmineapi.bean.ProjectFactory;
import com.taskadapter.redmineapi.bean.User;
import com.taskadapter.redmineapi.bean.UserFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;

public class IntegrationTestHelper {
    private static final Logger logger = LoggerFactory.getLogger(IntegrationTestHelper.class);

    // to make sure we all use the same config
    public static TestConfig getTestConfig() {
        return new TestConfig();
    }

    public static User getOurUser() {
        TestConfig testConfig = getTestConfig();
        Integer userId = Integer.parseInt(testConfig.getParam("createissue.userid"));
        String login = testConfig.getLogin();
        String fName = testConfig.getParam("userFName");
        String lName = testConfig.getParam("userLName");
        User user = UserFactory.create(userId);
        user.setLogin(login);
        user.setFirstName(fName);
        user.setLastName(lName);
        user.setApiKey(testConfig.getParam("apikey"));
        return user;
    }

    public static RedmineManager createRedmineManager() {
        TestConfig testConfig = getTestConfig();
        logger.info("Running Redmine integration tests using: " + testConfig.getURI());
        return RedmineManagerFactory.createWithUserAuth(testConfig.getURI(), testConfig.getLogin(), testConfig.getPassword());
    }

    public static Project createProject(RedmineManager mgr) {
        Project testProject = ProjectFactory.create("test project", "test" + Calendar.getInstance().getTimeInMillis());
        try {
            return mgr.getProjectManager().createProject(testProject);
        } catch (Exception e) {
            logger.error("Exception while configuring tests", e);
            throw new RuntimeException(e);
        }
    }

    public static Project createAndReturnProject(ProjectManager mgr) {
        Project testProject = ProjectFactory.create("test project", "test" + Calendar.getInstance().getTimeInMillis());
        try {
            return mgr.createProject(testProject);
        } catch (Exception e) {
            logger.error("Exception while configuring tests", e);
            throw new RuntimeException(e);
        }
    }
    /**
     * Delete the project if it exists. this method ignores NULL or empty projectKey parameter.
     */
    public static void deleteProject(RedmineManager mgr, String projectKey) {
        try {
            if (mgr != null && projectKey != null) {
                mgr.getProjectManager().deleteProject(projectKey);
            }
        } catch (Exception e) {
            logger.error("Exception while deleting test project", e);
            throw new RuntimeException("can't delete the test project '" + projectKey + ". reason: " + e.getMessage());
        }
    }
}
