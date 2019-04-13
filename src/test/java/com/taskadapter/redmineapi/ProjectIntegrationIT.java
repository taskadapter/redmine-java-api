package com.taskadapter.redmineapi;

import com.taskadapter.redmineapi.bean.CustomField;
import com.taskadapter.redmineapi.bean.CustomFieldDefinition;
import com.taskadapter.redmineapi.bean.CustomFieldFactory;
import com.taskadapter.redmineapi.bean.Project;
import com.taskadapter.redmineapi.bean.Tracker;
import com.taskadapter.redmineapi.bean.Version;
import com.taskadapter.redmineapi.internal.Transport;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import static com.taskadapter.redmineapi.CustomFieldResolver.getCustomFieldByName;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ProjectIntegrationIT {
    private static RedmineManager mgr;
    private static ProjectManager projectManager;
    private static String projectKey;
    private static Project project;
    private static Integer projectId;
    private static Transport transport;

    @BeforeClass
    public static void oneTimeSetup() {
        mgr = IntegrationTestHelper.createRedmineManager();
        projectManager = mgr.getProjectManager();
        transport = mgr.getTransport();
        try {
            project = IntegrationTestHelper.createProject(transport);
            projectKey = project.getIdentifier();
            projectId = project.getId();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @AfterClass
    public static void oneTimeTearDown() {
        IntegrationTestHelper.deleteProject(transport, projectKey);
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
    	final Integer statusActive = 1;
        Project projectToCreate = generateRandomProject();
        String key = null;
        try {
            Project createdProject = projectToCreate.create();
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
            assertEquals(statusActive, 
            		createdProject.getStatus());

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
    	final Integer statusActive = 1;
    	final Integer statusClosed = 5;
        Project projectToCreate = generateRandomProject();
        Project createdProject = null;
        try {
            createdProject = projectToCreate.setIdentifier("id" + new Date().getTime())
                    .create();
            String newDescr = "NEW123";
            String newName = "new name here";

            createdProject.setName(newName)
                    .setDescription(newDescr)
                    .setStatus(statusClosed)
                    .update();

            Project updatedProject = projectManager.getProjectByKey(createdProject.getIdentifier());
            assertNotNull(updatedProject);

            assertEquals(createdProject.getIdentifier(),
                    updatedProject.getIdentifier());
            assertEquals(newName, updatedProject.getName());
            assertEquals(newDescr, updatedProject.getDescription());
            //status should not change to closed as currently redmine api does not allow reopen/close/archive projects
            assertEquals(statusActive, updatedProject.getStatus());
            Collection<Tracker> trackers = updatedProject.getTrackers();
            assertNotNull("checking that project has some trackers",
                    trackers);
            assertTrue("checking that project has some trackers",
                    !(trackers.isEmpty()));
        } finally {
            if (createdProject != null) {
                createdProject.delete();
            }
        }
    }

    @Test
    public void createProjectFailsWithReservedIdentifier() throws Exception {
        Project projectToCreate = new Project(transport, "new", "new");
        Project createdProject = null;
        try {
            createdProject = projectToCreate.create();
        } catch (RedmineProcessingException e) {
            assertNotNull(e.getErrors());
            assertEquals(1, e.getErrors().size());
            assertEquals("Identifier is reserved", e.getErrors().get(0));
        } finally {
            if (createdProject != null) {
                createdProject.delete();
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
        Project created = projectManager.createProject(project);
        created.setDescription("updated description");
        try {
            created.update();

            Project updated = projectManager.getProjectByKey(project.getIdentifier());
            assertEquals(longHomepageName, updated.getHomepage());
        } finally {
            created.delete();
        }
    }

    @Test
    public void testUpdateTrackersBeforeRedmineUpdate() throws RedmineException {
        //Trackers are undefined for new project beans, updating trackers on the project object should be allowed
        Project projectToCreate = generateRandomProject();
        assertEquals(0, projectToCreate.getTrackers().size());
        
        Collection<Tracker> trackers=new HashSet<>(Arrays.asList(new Tracker().setId(1).setName("Bug")));
        projectToCreate.addTrackers(trackers);
        assertEquals(1, projectToCreate.getTrackers().size());
        
        projectToCreate.clearTrackers();
        assertEquals(0, projectToCreate.getTrackers().size());
    }
    
    @Test
    public void testUpdateTrackersAfterRedimineUpdate() throws RedmineException {
        //Prerequisite: verify that redmine server has at least 3 trackers (e.g. default configuration)
        List<Tracker> availableTrackers=mgr.getIssueManager().getTrackers();
        assertTrue("a minumum of 3 trackers should be configured in redmine", availableTrackers.size()>=3);

        Project projectToCreate = generateRandomProject();
        String createdProjectKey = null;
        try {
            //project created with default trackers has been tested by testCreateProject
            //test override of default trackers on project creation
            Project createdProject = projectToCreate.clearTrackers().create();
            createdProjectKey = createdProject.getIdentifier();
            assertEquals(0, createdProject.getTrackers().size());
           
            //add single tracker
            Collection<Tracker> trackersToSet=new HashSet<Tracker>(Arrays.asList(availableTrackers.get(0)));
            createdProject.addTrackers(trackersToSet).update();
            createdProject=projectManager.getProjectByKey(createdProjectKey);
            assertEquals(1, createdProject.getTrackers().size());

            //add more than one tracker, it does not replace previous trackers
            Collection<Tracker> trackersToAdd=new HashSet<Tracker>(Arrays.asList(availableTrackers.get(1), availableTrackers.get(2)));
            createdProject.addTrackers(trackersToAdd).update();
            createdProject=projectManager.getProjectByKey(createdProjectKey);
            assertEquals(3, createdProject.getTrackers().size());
            
            //all trackers can be removed
            createdProject.clearTrackers().update();
            createdProject=projectManager.getProjectByKey(createdProjectKey);
            assertEquals(0, createdProject.getTrackers().size());
        } finally {
            if (createdProjectKey != null) {
            	projectManager.getProjectByKey(createdProjectKey).delete();
            }
        }
    }

    @Test(expected = NotFoundException.class)
    public void testUpdateTrackersInvalidGivesException() throws RedmineException {
        int nonExistingTrackerId = 99999999;
        Collection<Tracker> trackers=new HashSet<>(Arrays.asList(new Tracker().setId(nonExistingTrackerId).setName("NonExisting")));
        Project projectToCreate = generateRandomProject();
        projectToCreate.addTrackers(trackers);
        projectToCreate.create();
    }

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
        List<Project> projects = new ArrayList<>(num);
        for (int i = 0; i < num; i++) {
            Project projectToCreate = generateRandomProject();
            Project p = projectManager.createProject(projectToCreate);
            projects.add(p);
        }
        return projects;
    }

    private static void deleteProjects(List<Project> projects) throws RedmineException {
        for (Project p : projects) {
            p.delete();
        }
    }

    private static Project generateRandomProject() {
        Long timeStamp = Calendar.getInstance().getTimeInMillis();
        String key = "projkey" + timeStamp;
        String name = "project number " + timeStamp;
        String description = "some description for the project";

        Project project = new Project(transport, name, key);
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
                createdMainProject.delete();
            }
        }
    }

    /**
     * This test requires a project custom field called "custom_project_field_1" to already exist on the server.
     * cannot create custom fields in Redmine programmatically - no support in its REST API.
     * See feature request http://www.redmine.org/issues/9664
     */
    @Ignore
    @Test
    public void projectIsCreatedWithCustomField() throws RedmineException {
        List<CustomFieldDefinition> customFieldDefinitions = mgr.getCustomFieldManager().getCustomFieldDefinitions();
        CustomFieldDefinition customFieldDefinition = getCustomFieldByName(customFieldDefinitions, "custom_project_field_1");
        Integer fieldId = customFieldDefinition.getId();
        CustomField customField = new CustomField().setId(fieldId);
        customField.setValue("value1");
        final Project project = generateRandomProject();
        project.setName("project-with-custom-field");
        Project createdProject = null;
        try {
            project.addCustomFields(Arrays.asList(customField));
            createdProject = projectManager.createProject(project);
            assertThat(createdProject.getCustomFieldById(fieldId).getValue()).isEqualTo("value1");
        } finally {
            if (createdProject != null) {
                createdProject.delete();
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
            Version version = new Version(transport, project.getId(), name)
                    .setDescription("A test version created by " + this.getClass())
                    .setStatus("open")
                    .create();
            assertEquals("checking version name", name, version.getName());

            version.delete();
            List<Version> versions = projectManager.getVersions(project.getId());
            assertTrue("List of versions of test project must be empty now but is "
                    + versions, versions.isEmpty());
        } finally {
            project.delete();
        }
    }

    /**
     * tests the retrieval of {@link Version}s.
     */
    @Test
    public void testGetVersions() throws RedmineException {
        Project project = createProject();
        Version testVersion1 = null;
        Version testVersion2 = null;
        try {
            testVersion1 = new Version(transport, project.getId(), "Version" + UUID.randomUUID())
                    .create();
            testVersion2 = new Version(transport, project.getId(), "Version" + UUID.randomUUID())
                    .create();
            List<Version> versions = projectManager.getVersions(project.getId());
            assertEquals("Wrong number of versions for project "
                            + project.getName() + " delivered by Redmine Java API", 2,
                    versions.size());
            for (Version version : versions) {
                // assert version
                assertNotNull("ID of version must not be null", version.getId());
                assertNotNull("Name of version must not be null", version.getName());
                assertNotNull("ProjectId of version must not be null", version.getProjectId());
            }
        } finally {
            if (testVersion1 != null) {
                testVersion1.delete();
            }
            if (testVersion2 != null) {
                testVersion2.delete();
            }
            project.delete();
        }
    }

    @Test
    public void versionIsRetrievedById() throws RedmineException {
        Version createdVersion = new Version(transport, projectId, "Version_1_" + UUID.randomUUID())
                .create();
        Version versionById = projectManager.getVersionById(createdVersion.getId());
        assertEquals(createdVersion, versionById);
    }

    @Test
    public void versionIsUpdated() throws RedmineException {
        Version createdVersion = new Version(transport, projectId, "Version_1_" + UUID.randomUUID())
                .create();
        String description = "new description";

        createdVersion.setDescription(description)
                .update();
        Version versionById = projectManager.getVersionById(createdVersion.getId());
        assertEquals(description, versionById.getDescription());
    }

    @Test
    public void versionIsUpdatedIncludingDueDate() throws RedmineException {
        Version createdVersion = new Version(transport, projectId, "Version_1_" + UUID.randomUUID())
                .create();
        String description = "new description";

        createdVersion.setDescription(description)
                .setDueDate(new Date())
                .update();
        Version versionById = projectManager.getVersionById(createdVersion.getId());
        assertEquals(description, versionById.getDescription());
    }

    @Test
    public void versionSharingParameterIsSaved() throws RedmineException {
        Version createdVersion = new Version(transport, projectId, "Version_1_" + UUID.randomUUID())
                .setSharing(Version.SHARING_NONE)
                .create();
        Version versionById = projectManager.getVersionById(createdVersion.getId());
        assertEquals(Version.SHARING_NONE, versionById.getSharing());

        Version createdVersion2 = new Version(transport, projectId, "Version_2_" + UUID.randomUUID())
        .setSharing(Version.SHARING_HIERARCHY)
                .create();
        Version version2ById = projectManager.getVersionById(createdVersion2.getId());
        assertEquals(Version.SHARING_HIERARCHY, version2ById.getSharing());
    }

    private Project createProject() throws RedmineException {
        long id = new Date().getTime();
        return new Project(transport, "project" + id, "project" + id)
                .create();
    }

    private Project createSubProject(Project parent) throws RedmineException {
        long id = new Date().getTime();
        return new Project(transport, "sub_pr" + id, "subpr" + id)
                .setParentId(parent.getId())
                .create();
    }

    @Test(expected = NotFoundException.class)
    public void versionWithNonExistingProjectIdGivesNotFoundException() throws RedmineException {
        new Version(transport, -1, "Invalid version " + UUID.randomUUID().toString())
                .create();
    }

    /**
     * tests the deletion of an invalid {@link Version}. Expects a
     * {@link NotFoundException} to be thrown.
     */
    @Test(expected = NotFoundException.class)
    public void testDeleteInvalidVersion() throws RedmineException {
        // version with invalid id: -1.
        Version version = new Version(transport, projectId, "123").setId(-1)
                .setName("name invalid version " + UUID.randomUUID().toString())
                .setDescription("An invalid test version created by " + this.getClass());

        version.delete();
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
