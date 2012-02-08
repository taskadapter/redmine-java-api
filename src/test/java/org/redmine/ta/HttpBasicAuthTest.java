package org.redmine.ta;

import org.junit.BeforeClass;
import org.junit.Test;
import org.redmine.ta.beans.Project;
import org.redmine.ta.internal.logging.Logger;
import org.redmine.ta.internal.logging.LoggerFactory;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Check the RedmineManager returns the same amount of projects using HTTP basic authentication and
 * API key.
 * <p/>
 * It's better to use the redmine account with access to at least 1 public and 1 private project.
 *
 * @author JÃ©rÃ©mie Huchet
 */
public final class HttpBasicAuthTest {

    private final Logger logger = LoggerFactory.getLogger(HttpBasicAuthTest.class);

    private static RedmineManager mgrKey;
    private static RedmineManager mgrHttpBasicAuth;

    @BeforeClass
    public static void setup() {
        TestConfig testConfig = new TestConfig();
        mgrKey = new RedmineManager(testConfig.getURI(), testConfig.getApiKey());
        mgrHttpBasicAuth = new RedmineManager(testConfig.getURI());
        mgrHttpBasicAuth.setLogin(testConfig.getLogin());
        mgrHttpBasicAuth.setPassword(testConfig.getPassword());
    }

    @Test
    public void testGetProjectList() throws IOException, AuthenticationException, RedmineException {

        final List<Project> projectsWithApiKey = mgrKey.getProjects();
        final List<Project> projectsWithHttpBasicAuth = mgrHttpBasicAuth.getProjects();

        logger.debug(String.format("RedmineManager using API key auth returned %s projects",
                projectsWithApiKey.size()));
        logger.debug(String.format(
                "RedmineManager using HTTP basic auth returned %s projects",
                projectsWithHttpBasicAuth.size()));

        assertEquals("Number of projects returned for API access key is different from the one for Login and Password",
                projectsWithApiKey, projectsWithHttpBasicAuth);

        // check the result received with the manager using HTTP basic authentication received all
        // the projects
        for (final Project expectedProject : projectsWithApiKey) {
            assertTrue("Check the redmine mgr using basic auth returned project id "
                    + expectedProject.getId(), projectsWithHttpBasicAuth.contains(expectedProject));
        }
    }
}

