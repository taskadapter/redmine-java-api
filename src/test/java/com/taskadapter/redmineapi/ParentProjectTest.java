package com.taskadapter.redmineapi;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;

import com.taskadapter.redmineapi.RedmineException;
import com.taskadapter.redmineapi.RedmineManager;
import com.taskadapter.redmineapi.bean.Project;

public class ParentProjectTest {

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

        Project parentProject = createProject(parentKey, "Parent Project", null);
        Project childProject = 
                createProject(childKey, "Child Project", parentProject.getId());

        try {
            Assert.assertEquals(childProject.getParentId(), parentProject.getId());
        } finally {
            mgr.deleteProject(childKey);
            mgr.deleteProject(parentKey);
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