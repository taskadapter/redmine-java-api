package com.taskadapter.redmineapi;

import com.taskadapter.redmineapi.bean.News;
import com.taskadapter.redmineapi.bean.Project;
import com.taskadapter.redmineapi.bean.Version;
import com.taskadapter.redmineapi.internal.Transport;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Works with Projects and their Versions.
 * <p>Obtain it via RedmineManager:
 * <pre>
 RedmineManager redmineManager = RedmineManagerFactory.createWithUserAuth(redmineURI, login, password);
 ProjectManager projectManager = redmineManager.getProjectManager();
 * </pre>
 *
 * <p>Sample usage:
 * <pre>
 projectManager.getProjectById(123);

 projects = projectManager.getProjects();

 // create project
 Project project = ProjectFactory.create();
 project.setName("MyProject");
 project.setDescription("description");
 project.setHomepage("www.mypage.com");
 createdProject = projectManager.createProject(project);
 * </pre>
 *
 * @see RedmineManager#getIssueManager()
 */
public class ProjectManager {
    private final Transport transport;

    ProjectManager(Transport transport) {
        this.transport = transport;
    }

    /**
     * Sample usage:
     * 
     * <pre>
     * {@code
     * 	Project project = new Project();
     * 	Long timeStamp = Calendar.getInstance().getTimeInMillis();
     * 	String key = "projkey" + timeStamp;
     * 	String name = &quot;project number &quot; + timeStamp;
     * 	String description = &quot;some description for the project&quot;;
     * 	project.setIdentifier(key);
     * 	project.setName(name);
     * 	project.setDescription(description);
     *
     * 	Project createdProject = mgr.createProject(project);
     * }
     * </pre>
     *
     * @param project project to create on the server
     * @return the newly created Project object.
     * @throws RedmineAuthenticationException invalid or no API access key is used with the server, which
     *                                 requires authorization. Check the constructor arguments.
     * @throws RedmineException
     */
    public Project createProject(Project project) throws RedmineException {
        return transport.addObject(project, new BasicNameValuePair("include",
                "trackers"));
    }

    /**
     * Load the list of projects available to the user, which is represented by the API access key.
     * <p>
     * Redmine ignores "get trackers info" parameter for "get projects" request. see bug
     * http://www.redmine.org/issues/8545
     * The field is already accessible for a specific project for a long time (GET /projects/:id)
     * but in the projects list (GET /projects) it's only on the svn trunk for now (Sep 8, 2014).
     * It will be included in Redmine 2.6.0 which isn't out yet.
     *
     * @return list of Project objects
     * @throws RedmineAuthenticationException invalid or no API access key is used with the server, which
     *                                 requires authorization. Check the constructor arguments.
     * @throws RedmineException
     */
    public List<Project> getProjects() throws RedmineException {
        try {
            return transport.getObjectsList(Project.class,
                    new BasicNameValuePair("include", "trackers"));
        } catch (NotFoundException e) {
            throw new RedmineInternalError("NotFoundException received, which should never happen in this request");
        }
    }

    /**
     * @param projectKey string key like "project-ABC", NOT a database numeric ID
     *
     * @return Redmine's project
     * @throws RedmineAuthenticationException invalid or no API access key is used with the server, which
     *                                 requires authorization. Check the constructor arguments.
     * @throws NotFoundException       the project with the given key is not found
     * @throws RedmineException
     */
    public Project getProjectByKey(String projectKey) throws RedmineException {
        return transport.getObject(Project.class, projectKey,
                new BasicNameValuePair("include", "trackers"));
    }

    /**
     * @param id project database Id, like 123. this is not a string "key" like "myproject".
     *
     * @return Redmine's project
     * @throws RedmineAuthenticationException invalid or no API access key is used with the server, which
     *                                 requires authorization.
     * @throws NotFoundException       the project with the given id is not found
     * @throws RedmineException
     */
    public Project getProjectById(int id) throws RedmineException {
        return transport.getObject(Project.class, id, new BasicNameValuePair("include", "trackers"));
    }

    /**
     * @param projectKey string key like "project-ABC", NOT a database numeric ID
     * @throws RedmineAuthenticationException invalid or no API access key is used with the server, which
     *                                 requires authorization. Check the constructor arguments.
     * @throws NotFoundException       if the project with the given key is not found
     * @throws RedmineException
     */
    public void deleteProject(String projectKey) throws RedmineException {
        transport.deleteObject(Project.class, projectKey);
    }

    /**
     * creates a new {@link com.taskadapter.redmineapi.bean.Version} for the {@link Project} contained. <br>
     * Pre-condition: the attribute {@link Project} for the {@link com.taskadapter.redmineapi.bean.Version} must
     * not be null!
     *
     * @param version the {@link com.taskadapter.redmineapi.bean.Version}. Must contain a {@link Project}.
     * @return the new {@link com.taskadapter.redmineapi.bean.Version} created by Redmine
     * @throws IllegalArgumentException thrown in case the version does not contain a project.
     * @throws RedmineAuthenticationException  thrown in case something went wrong while trying to login
     * @throws RedmineException         thrown in case something went wrong in Redmine
     * @throws NotFoundException        thrown in case an object can not be found
     */
    public Version createVersion(Version version) throws RedmineException {
        // check project
        if (version.getProject() == null
                || version.getProject().getId() == null) {
            throw new IllegalArgumentException(
                    "Version must contain an existing project");
        }
        return transport.addChildEntry(Project.class, version.getProject()
                .getId().toString(), version);
    }

    /**
     * deletes a new {@link Version} from the {@link Project} contained. <br>
     *
     * @param version the {@link Version}.
     * @throws RedmineAuthenticationException thrown in case something went wrong while trying to login
     * @throws RedmineException        thrown in case something went wrong in Redmine
     * @throws NotFoundException       thrown in case an object can not be found
     */
    public void deleteVersion(Version version) throws RedmineException {
        transport
                .deleteObject(Version.class, Integer.toString(version.getId()));
    }

    /**
     * delivers a list of {@link Version}s of a {@link Project}
     *
     * @param projectID the ID of the {@link Project}
     * @return the list of {@link Version}s of the {@link Project}
     * @throws RedmineAuthenticationException thrown in case something went wrong while trying to login
     * @throws RedmineException        thrown in case something went wrong in Redmine
     * @throws NotFoundException       thrown in case an object can not be found
     */
    public List<Version> getVersions(int projectID) throws RedmineException {
        return transport.getChildEntries(Project.class,
                Integer.toString(projectID), Version.class);
    }

    // TODO add test
    public Version getVersionById(int versionId) throws RedmineException {
        return transport.getObject(Version.class, versionId);
    }

    public void update(Project object) throws RedmineException {
        transport.updateObject(object);
    }

    public void update(Version object) throws RedmineException {
        transport.updateObject(object);
    }

    /**
     * @param projectKey ignored if NULL
     * @return list of news objects
     * @see com.taskadapter.redmineapi.bean.News
     */
    public List<News> getNews(String projectKey) throws RedmineException {
        Set<NameValuePair> params = new HashSet<NameValuePair>();
        if ((projectKey != null) && (projectKey.length() > 0)) {
            params.add(new BasicNameValuePair("project_id", projectKey));
        }
        return transport.getObjectsList(News.class, params);
    }
}
