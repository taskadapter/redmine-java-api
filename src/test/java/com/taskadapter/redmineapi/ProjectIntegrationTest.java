package com.taskadapter.redmineapi;

import com.taskadapter.redmineapi.bean.Project;
import com.taskadapter.redmineapi.bean.ProjectFactory;
import com.taskadapter.redmineapi.bean.Tracker;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ProjectIntegrationTest {
    private static RedmineManager mgr;
    private static String projectKey;

    @BeforeClass
    public static void oneTimeSetup() {
        mgr = IntegrationTestHelper.createRedmineManager();
        try {
            projectKey = IntegrationTestHelper.createProject(mgr);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @AfterClass
    public static void oneTimeTearDown() {
        IntegrationTestHelper.deleteProject(mgr, projectKey);
    }

    @Test(expected = NotFoundException.class)
    public void testGetProjectNonExistingId() throws RedmineException {
        mgr.getProjectByKey("some-non-existing-key");
    }

    @Test(expected = NotFoundException.class)
    public void testDeleteNonExistingProject() throws RedmineException {
        mgr.deleteProject("some-non-existing-key");
    }

    /**
     * Tests the retrieval of {@link com.taskadapter.redmineapi.bean.Project}s.
     *
     * @throws RedmineException               thrown in case something went wrong in Redmine
     * @throws java.io.IOException            thrown in case something went wrong while performing I/O
     *                                        operations
     * @throws RedmineAuthenticationException thrown in case something went wrong while trying to login
     * @throws com.taskadapter.redmineapi.NotFoundException
     *                                        thrown in case the objects requested for could not be found
     */
    @Test
    public void testGetProjects() throws RedmineException {
        // retrieve projects
        List<Project> projects = mgr.getProjects();
        // asserts
        assertTrue(projects.size() > 0);
        boolean found = false;
        for (Project project : projects) {
            if (project.getIdentifier().equals(projectKey)) {
                found = true;
                break;
            }
        }
        if (!found) {
            fail("Our project with key '" + projectKey + "' is not found on the server");
        }
    }

    @Test
    public void testCreateProject() throws RedmineException {
        Project projectToCreate = generateRandomProject();
        String key = null;
        try {
            Project createdProject = mgr.createProject(projectToCreate);
            key = createdProject.getIdentifier();

            assertNotNull(
                    "checking that a non-null project is returned",
                    createdProject);

            assertEquals(projectToCreate.getIdentifier(),
                    createdProject.getIdentifier());
            assertEquals(projectToCreate.getName(),
                    createdProject.getName());
            assertEquals(projectToCreate.getDescription(),
                    createdProject.getDescription());
            assertEquals(projectToCreate.getHomepage(),
                    createdProject.getHomepage());

            List<Tracker> trackers = createdProject.getTrackers();
            assertNotNull("checking that project has some trackers",
                    trackers);
            assertTrue("checking that project has some trackers",
                    !(trackers.isEmpty()));
        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            if (key != null) {
                mgr.deleteProject(key);
            }
        }
    }

    @Test
    public void testCreateGetUpdateDeleteProject() throws RedmineException {
        Project projectToCreate = generateRandomProject();
        String key = null;
        try {
            projectToCreate.setIdentifier("id" + new Date().getTime());
            Project createdProject = mgr.createProject(projectToCreate);
            key = createdProject.getIdentifier();
            String newDescr = "NEW123";
            String newName = "new name here";

            createdProject.setName(newName);
            createdProject.setDescription(newDescr);
            mgr.update(createdProject);

            Project updatedProject = mgr.getProjectByKey(key);
            assertNotNull(updatedProject);

            assertEquals(createdProject.getIdentifier(),
                    updatedProject.getIdentifier());
            assertEquals(newName, updatedProject.getName());
            assertEquals(newDescr, updatedProject.getDescription());
            List<Tracker> trackers = updatedProject.getTrackers();
            assertNotNull("checking that project has some trackers",
                    trackers);
            assertTrue("checking that project has some trackers",
                    !(trackers.isEmpty()));
        } finally {
            if (key != null) {
                mgr.deleteProject(key);
            }
        }
    }

    @Test
    public void createProjectFailsWithReservedIdentifier() throws Exception {
        Project projectToCreate = ProjectFactory.create("new", "new");
        String createdProjectKey = null;
        try {
            Project createdProject = mgr.createProject(projectToCreate);
            // in case if the creation haven't failed (although it should have
            // had!),
            // need to cleanup - delete this project
            createdProjectKey = createdProject.getIdentifier();

        } catch (RedmineProcessingException e) {
            assertNotNull(e.getErrors());
            assertEquals(1, e.getErrors().size());
            assertEquals("Identifier is reserved", e.getErrors().get(0));
        } finally {
            if (createdProjectKey != null) {
                mgr.deleteProject(createdProjectKey);
            }
        }
    }

    @Test
    public void tryUpdateProjectWithLongHomepage() throws RedmineException {
        final Project project = generateRandomProject();
        project.setName("issue 7 test project");
        project.setDescription("test");
        final String longHomepageName = "http://www.localhost.com/asdf?a=\"&b=\"&c=\"&d=\"&e=\"&f=\"&g=\"&h=\"&i=\"&j=\"&k=\"&l=\"&m=\"&n=\"&o=\"&p=\"&q=\"&r=\"&s=\"&t=\"&u=\"&v=\"&w=\"&x=\"&y=\"&zо=авфбвоафжывлдаофжывладоджлфоывадлфоываждфлоываждфлоываждлфоываждлфова&&\\&&&&&&&&&&&&&&&&&&\\&&&&&&&&&&&&&&&&&&&&&&&&&&&&<>>";
        project.setHomepage(longHomepageName);
        final Project created = mgr.createProject(project);
        created.setDescription("updated description");
        try {
            mgr.update(created);
            final Project updated = mgr
                    .getProjectByKey(project.getIdentifier());
            assertEquals(longHomepageName, updated.getHomepage());
        } finally {
            mgr.deleteProject(created.getIdentifier());
        }
    }

    // Redmine ignores this parameter for "get projects" request. see bug
    // http://www.redmine.org/issues/8545
    @Ignore
    @Test
    public void testGetProjectsIncludesTrackers() {
        try {
            List<Project> projects = mgr.getProjects();
            assertTrue(projects.size() > 0);
            Project p1 = projects.get(0);
            assertNotNull(p1.getTrackers());
            for (Project p : projects) {
                if (!p.getTrackers().isEmpty()) {
                    return;
                }
            }
            fail("No projects with trackers found");
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testProjectsAllPagesLoaded() throws RedmineException {
        int NUM = 27; // must be larger than 25, which is a default page size in
        // Redmine
        List<Project> projects = createProjects(NUM);

        List<Project> loadedProjects = mgr.getProjects();
        assertTrue(
                "Number of projects loaded from the server must be bigger than "
                        + NUM + ", but it's " + loadedProjects.size(),
                loadedProjects.size() > NUM);

        deleteProjects(projects);
    }

    private List<Project> createProjects(int num) throws RedmineException {
        List<Project> projects = new ArrayList<Project>(num);
        for (int i = 0; i < num; i++) {
            Project projectToCreate = generateRandomProject();
            Project p = mgr.createProject(projectToCreate);
            projects.add(p);
        }
        return projects;
    }

    private void deleteProjects(List<Project> projects) throws RedmineException {
        for (Project p : projects) {
            mgr.deleteProject(p.getIdentifier());
        }
    }

    private static Project generateRandomProject() {
        Long timeStamp = Calendar.getInstance().getTimeInMillis();
        String key = "projkey" + timeStamp;
        String name = "project number " + timeStamp;
        String description = "some description for the project";

        Project project = ProjectFactory.create(name, key);
        project.setDescription(description);
        project.setHomepage("www.randompage" + timeStamp + ".com");
        return project;
    }

}
