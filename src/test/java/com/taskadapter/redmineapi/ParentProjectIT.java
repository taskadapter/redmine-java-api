package com.taskadapter.redmineapi;

import com.taskadapter.redmineapi.bean.Project;
import com.taskadapter.redmineapi.internal.Transport;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ParentProjectIT {

    private static ProjectManager projectManager;
    private static Transport transport;

    @BeforeClass
    public static void oneTimeSetUp() {
        RedmineManager redmineManager = IntegrationTestHelper.createRedmineManager();
        projectManager = redmineManager.getProjectManager();
        transport = redmineManager.getTransport();
    }

    @Test
    public void childProjectGetsCorrectParentId() throws RedmineException {
        String parentKey = "parent" + System.currentTimeMillis();
        String childKey = "child" + System.currentTimeMillis();

        Project parentProject = createProject(parentKey, "Parent Project", null);
        Project childProject =
                createProject(childKey, "Child Project", parentProject.getId());

        try {
            assertThat(childProject.getParentId()).isEqualTo(parentProject.getId());
        } finally {
            // Alexey: I verified that deleting the parent project deletes the child one as well
            // (at least on Redmine 2.3.3)
            // Thus, there's no need in deleting the child one separately
            parentProject.delete();
        }
    }

    /**
     * @param parentId id of the parent project or null
     */
    private Project createProject(String key, String name, Integer parentId) throws RedmineException {
        Project newProject = new Project(transport, name, key);
        if (parentId != null) {
            newProject.setParentId(parentId);
        }
        newProject.create();
        return projectManager.getProjectByKey(key);
    }

}
