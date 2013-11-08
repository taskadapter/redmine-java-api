package com.taskadapter.redmineapi;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;

import com.taskadapter.redmineapi.RedmineException;
import com.taskadapter.redmineapi.RedmineManager;
import com.taskadapter.redmineapi.bean.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ParentProjectTest {

    private final Logger logger = LoggerFactory.getLogger(ParentProjectTest.class);

    private static RedmineManager mgr;

  @BeforeClass
    public static void oneTimeSetUp() {
    TestConfig testConfig = new TestConfig();
        mgr = new RedmineManager(testConfig.getURI());
        mgr.setLogin(testConfig.getLogin());
        mgr.setPassword(testConfig.getPassword());
    }

    @Test
    public void testProjectParentId() throws RedmineException {
        String parentKey = "parent";
        String childKey = "child";

        try {
            Project parentProject = createProject(parentKey, "Parent Project", null);
            Project childProject =
                    createProject(childKey, "Child Project", parentProject.getId());

            Assert.assertEquals(childProject.getParentId(), parentProject.getId());
        } finally {
            try {
                mgr.deleteProject(parentKey);
                mgr.deleteProject(childKey);
            } catch (RedmineException e) {
                logger.debug("Unable to delete a project.", e);
            }
        }
    }

    /**
     * @param parentId id of the parent project or null
     */
    private Project createProject(String key, String name, Integer parentId) throws RedmineException {
        Project newProject = new Project();
        newProject.setIdentifier(key);
        newProject.setName(name);
        if (parentId != null)
            newProject.setParentId(parentId);
        mgr.createProject(newProject);
        return mgr.getProjectByKey(key);
    }

}