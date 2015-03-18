package com.taskadapter.redmineapi;

import com.taskadapter.redmineapi.bean.Project;
import com.taskadapter.redmineapi.bean.ProjectFactory;
import com.taskadapter.redmineapi.bean.Tracker;
import com.taskadapter.redmineapi.bean.Version;
import com.taskadapter.redmineapi.bean.VersionFactory;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ProjectIntegrationTest {
    private static RedmineManager mgr;
    private static ProjectManager projectManager;
    private static String projectKey;
    private static Project project;

    @BeforeClass
    public static void oneTimeSetup() {
        mgr = IntegrationTestHelper.createRedmineManager();
        projectManager = mgr.getProjectManager();
        try {
            project = IntegrationTestHelper.createProject(mgr);
            projectKey = project.getIdentifier();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @AfterClass
    public static void oneTimeTearDown() {
        IntegrationTestHelper.deleteProject(mgr, projectKey);
    }

    @Test(expected = NotFoundException.class)
    public void testDeleteNonExistingProject() throws RedmineException {
        projectManager.deleteProject("some-non-existing-key");
    }

    @Test
    public void projectIsLoadedById() throws RedmineException {
        final Project projectById = projectManager.getProjectById(project.getId());
        assertThat(projectById.getName()).isEqualTo(project.getName());
    }

    @Test(expected = NotFoundException.class)
    public void requestingPojectNonExistingIdGivesNFE() throws RedmineException {
        projectManager.getProjectById(999999999);
    }

    @Test(expected = NotFoundException.class)
    public void requestingPojectNonExistingStrignKeyGivesNFE() throws RedmineException {
        projectManager.getProjectByKey("some-non-existing-key");
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
        List<Project> projects = projectManager.getProjects();
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
            Project createdProject = projectManager.createProject(projectToCreate);
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

            Collection<Tracker> trackers = createdProject.getTrackers();
            assertNotNull("checking that project has some trackers",
                    trackers);
            assertTrue("checking that project has some trackers",
                    !(trackers.isEmpty()));
        } finally {
            if (key != null) {
                projectManager.deleteProject(key);
            }
        }
    }

    @Test
    public void testCreateGetUpdateDeleteProject() throws RedmineException {
        Project projectToCreate = generateRandomProject();
        String key = null;
        try {
            projectToCreate.setIdentifier("id" + new Date().getTime());
            Project createdProject = projectManager.createProject(projectToCreate);
            key = createdProject.getIdentifier();
            String newDescr = "NEW123";
            String newName = "new name here";

            createdProject.setName(newName);
            createdProject.setDescription(newDescr);
            projectManager.update(createdProject);

            Project updatedProject = projectManager.getProjectByKey(key);
            assertNotNull(updatedProject);

            assertEquals(createdProject.getIdentifier(),
                    updatedProject.getIdentifier());
            assertEquals(newName, updatedProject.getName());
            assertEquals(newDescr, updatedProject.getDescription());
            Collection<Tracker> trackers = updatedProject.getTrackers();
            assertNotNull("checking that project has some trackers",
                    trackers);
            assertTrue("checking that project has some trackers",
                    !(trackers.isEmpty()));
        } finally {
            if (key != null) {
                projectManager.deleteProject(key);
            }
        }
    }

    @Test
    public void createProjectFailsWithReservedIdentifier() throws Exception {
        Project projectToCreate = ProjectFactory.create("new", "new");
        String createdProjectKey = null;
        try {
            Project createdProject = projectManager.createProject(projectToCreate);
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
                projectManager.deleteProject(createdProjectKey);
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
        final Project created = projectManager.createProject(project);
        created.setDescription("updated description");
        try {
            projectManager.update(created);
            final Project updated = projectManager.getProjectByKey(project.getIdentifier());
            assertEquals(longHomepageName, updated.getHomepage());
        } finally {
            projectManager.deleteProject(created.getIdentifier());
        }
    }

    // Redmine ignores this parameter for "get projects" request. see bug
    // http://www.redmine.org/issues/8545
    // The field is already accessible for a specific project for a long time (GET /projects/:id)
    // but in the projects list (GET /projects) it's only on the svn trunk for now (Sep 8, 2014).
    // It will be included in Redmine 2.6.0 which isn't out yet.
    @Ignore
    @Test
    public void testGetProjectsIncludesTrackers() throws RedmineException {
        List<Project> projects = projectManager.getProjects();
        assertTrue(projects.size() > 0);
        Project p1 = projects.get(0);
        assertNotNull(p1.getTrackers());
        for (Project p : projects) {
            if (!p.getTrackers().isEmpty()) {
                return;
            }
        }
        fail("No projects with trackers found");
    }

    @Test
    public void testProjectsAllPagesLoaded() throws RedmineException {
        int NUM = 27; // must be larger than 25, which is a default page size in
        // Redmine
        List<Project> projects = createProjects(NUM);

        List<Project> loadedProjects = projectManager.getProjects();
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
            Project p = projectManager.createProject(projectToCreate);
            projects.add(p);
        }
        return projects;
    }

    private void deleteProjects(List<Project> projects) throws RedmineException {
        for (Project p : projects) {
            projectManager.deleteProject(p.getIdentifier());
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

    /**
     * Tests the correct retrieval of the parent id of sub {@link Project}.
     *
     * @throws RedmineProcessingException     thrown in case something went wrong in Redmine
     * @throws java.io.IOException                    thrown in case something went wrong while performing I/O
     *                                        operations
     * @throws RedmineAuthenticationException thrown in case something went wrong while trying to login
     * @throws NotFoundException              thrown in case the objects requested for could not be found
     */
    @Test
    public void testSubProjectIsCreatedWithCorrectParentId()
            throws RedmineException {
        Project createdMainProject = null;
        try {
            createdMainProject = createProject();
            Project subProject = createSubProject(createdMainProject);
            assertEquals("Must have correct parent ID",
                    createdMainProject.getId(), subProject.getParentId());
        } finally {
            if (createdMainProject != null) {
                projectManager.deleteProject(createdMainProject.getIdentifier());
            }
        }
    }

    /**
     * tests the deletion of a {@link com.taskadapter.redmineapi.bean.Version}.
     *
     * @throws RedmineException               thrown in case something went wrong in Redmine
     * @throws RedmineAuthenticationException thrown in case something went wrong while trying to login
     * @throws NotFoundException              thrown in case the objects requested for could not be found
     */
    @Test
    public void testDeleteVersion() throws RedmineException {
        Project project = createProject();
        try {
            String name = "Test version " + UUID.randomUUID().toString();
            Version version = VersionFactory.create(project, name);
            version.setDescription("A test version created by " + this.getClass());
            version.setStatus("open");
            Version newVersion = projectManager.createVersion(version);
            assertEquals("checking version name", name, newVersion.getName());

            projectManager.deleteVersion(newVersion);
            List<Version> versions = projectManager.getVersions(project.getId());
            assertTrue("List of versions of test project must be empty now but is "
                    + versions, versions.isEmpty());
        } finally {
            projectManager.deleteProject(project.getIdentifier());
        }
    }

    /**
     * tests the retrieval of {@link Version}s.
     *
     * @throws RedmineException               thrown in case something went wrong in Redmine
     * @throws RedmineAuthenticationException thrown in case something went wrong while trying to login
     * @throws NotFoundException              thrown in case the objects requested for could not be found
     */
    @Test
    public void testGetVersions() throws RedmineException {
        Project project = createProject();
        Version testVersion1 = null;
        Version testVersion2 = null;
        try {
            testVersion1 = projectManager.createVersion(VersionFactory.create(project, "Version" + UUID.randomUUID()));
            testVersion2 = projectManager.createVersion(VersionFactory.create(project, "Version" + UUID.randomUUID()));
            List<Version> versions = projectManager.getVersions(project.getId());
            assertEquals("Wrong number of versions for project "
                            + project.getName() + " delivered by Redmine Java API", 2,
                    versions.size());
            for (Version version : versions) {
                // assert version
                assertNotNull("ID of version must not be null", version.getId());
                assertNotNull("Name of version must not be null", version.getName());
                assertNotNull("Project of version must not be null", version.getProject());
            }
        } finally {
            if (testVersion1 != null) {
                projectManager.deleteVersion(testVersion1);
            }
            if (testVersion2 != null) {
                projectManager.deleteVersion(testVersion2);
            }
            projectManager.deleteProject(project.getIdentifier());
        }
    }

    @Test
    public void versionIsRetrievedById() throws RedmineException {
        Project project = projectManager.getProjectByKey(projectKey);
        Version createdVersion = projectManager.createVersion(VersionFactory.create(project,
                "Version_1_" + UUID.randomUUID()));
        Version versionById = projectManager.getVersionById(createdVersion.getId());
        assertEquals(createdVersion, versionById);
    }

    @Test
    public void versionIsUpdated() throws RedmineException {
        Project project = projectManager.getProjectByKey(projectKey);
        Version createdVersion = projectManager.createVersion(VersionFactory.create(project,
                "Version_1_" + UUID.randomUUID()));
        String description = "new description";
        createdVersion.setDescription(description);
        projectManager.update(createdVersion);
        Version versionById = projectManager.getVersionById(createdVersion.getId());
        assertEquals(description, versionById.getDescription());
    }

    @Test
    public void versionIsUpdatedIncludingDueDate() throws RedmineException {
        Project project = projectManager.getProjectByKey(projectKey);
        Version createdVersion = projectManager.createVersion(VersionFactory.create(project,
                "Version_1_" + UUID.randomUUID()));
        String description = "new description";
        createdVersion.setDescription(description);
        createdVersion.setDueDate(new Date());
        projectManager.update(createdVersion);
        Version versionById = projectManager.getVersionById(createdVersion.getId());
        assertEquals(description, versionById.getDescription());
    }

    @Test
    public void versionSharingParameterIsSaved() throws RedmineException {
        Project project = projectManager.getProjectByKey(projectKey);
        Version version = VersionFactory.create(project, "Version_1_" + UUID.randomUUID());
        version.setSharing(Version.SHARING_NONE);
        Version createdVersion = projectManager.createVersion(version);
        Version versionById = projectManager.getVersionById(createdVersion.getId());
        assertEquals(Version.SHARING_NONE, versionById.getSharing());

        Version versionShared = VersionFactory.create(project, "Version_2_" + UUID.randomUUID());
        versionShared.setSharing(Version.SHARING_HIERARCHY);
        Version createdVersion2 = projectManager.createVersion(versionShared);
        Version version2ById = projectManager.getVersionById(createdVersion2.getId());
        assertEquals(Version.SHARING_HIERARCHY, version2ById.getSharing());
    }

    private Project createProject() throws RedmineException {
        long id = new Date().getTime();
        Project mainProject = ProjectFactory.create("project" + id, "project" + id);
        return projectManager.createProject(mainProject);
    }

    private Project createSubProject(Project parent) throws RedmineException {
        long id = new Date().getTime();
        Project project = ProjectFactory.create("sub_pr" + id, "subpr" + id);
        project.setParentId(parent.getId());
        return projectManager.createProject(project);
    }

    /**
     * tests the creation of an invalid {@link Version}.
     *
     * @throws RedmineException               thrown in case something went wrong in Redmine
     * @throws RedmineAuthenticationException thrown in case something went wrong while trying to login
     * @throws NotFoundException              thrown in case the objects requested for could not be found
     */
    @Test(expected = IllegalArgumentException.class)
    public void testCreateInvalidVersion() throws RedmineException {
        Version version = VersionFactory.create(null, "Invalid version " + UUID.randomUUID().toString());
        projectManager.createVersion(version);
    }

    /**
     * tests the deletion of an invalid {@link Version}. Expects a
     * {@link NotFoundException} to be thrown.
     *
     * @throws RedmineException               thrown in case something went wrong in Redmine
     * @throws RedmineAuthenticationException thrown in case something went wrong while trying to login
     * @throws NotFoundException              thrown in case the objects requested for could not be found
     */
    @Test(expected = NotFoundException.class)
    public void testDeleteInvalidVersion() throws RedmineException {
        // create new test version with invalid id: -1.
        Version version = VersionFactory.create(-1);
        version.setName("name invalid version " + UUID.randomUUID().toString());
        version.setDescription("An invalid test version created by " + this.getClass());
        // now try to delete version
        projectManager.deleteVersion(version);
    }

    @Test
    public void getNewsDoesNotFailForNULLProject() throws RedmineException {
        projectManager.getNews(null);
    }

    @Test
    public void getNewsDoesNotFailForTempProject() throws RedmineException {
        projectManager.getNews(projectKey);
    }


}
