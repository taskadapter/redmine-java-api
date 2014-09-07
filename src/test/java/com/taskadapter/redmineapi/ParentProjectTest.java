package com.taskadapter.redmineapi;

import com.taskadapter.redmineapi.bean.Project;
import com.taskadapter.redmineapi.bean.ProjectFactory;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

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
    public void childProjectGetsCorrectParentId() throws RedmineException {
        String parentKey = "parent" + System.currentTimeMillis();
        String childKey = "child" + System.currentTimeMillis();

        Project parentProject = createProject(parentKey, "Parent Project", null);
        Project childProject =
                createProject(childKey, "Child Project", parentProject.getId());

        try {
            assertEquals(childProject.getParentId(), parentProject.getId());
        } finally {
            // Alexey: I verified that deleting the parent project deletes the child one as well
            // (at least on Redmine 2.3.3)
            // Thus, there's no need in deleting the child one separately
            mgr.deleteProject(parentKey);
        }
    }

    /**
     * @param parentId id of the parent project or null
     */
    private Project createProject(String key, String name, Integer parentId) throws RedmineException {
        Project newProject = ProjectFactory.create(name, key);
        if (parentId != null) {
            newProject.setParentId(parentId);
        }
        mgr.createProject(newProject);
        return mgr.getProjectByKey(key);
    }

}
