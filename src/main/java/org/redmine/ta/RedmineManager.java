/*
   Copyright 2010-2012 Alexey Skorokhodov.

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package org.redmine.ta;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.redmine.ta.beans.*;
import org.redmine.ta.internal.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;
import java.util.Map.Entry;

/**
 * <b>Entry point</b> for the API: use this class to communicate with Redmine
 * servers.
 * 
 * @author Alexey Skorokhodov
 */
public class RedmineManager {

	// TODO add tests for "relations" to RedmineManagerTest class
	public static enum INCLUDE {
		// these values MUST BE exactly as they are written here,
		// can't use capital letters or rename.
		// they are provided in "?include=..." HTTP request
		journals, relations, attachments
	}

	private final Transport transport;

	public RedmineManager(String uri) {
		this(uri, null, null);
	}

	public RedmineManager(String uri, String login, String password) {
		this.transport = new Transport(new URIConfigurator(uri, null));
		transport.setCredentials(login, password);
	}

	/**
	 * Creates an instance of RedmineManager class. Host and apiAccessKey are
	 * not checked at this moment.
	 * 
	 * @param host
	 *            complete Redmine server web URI, including protocol and port
	 *            number. Example: http://demo.redmine.org:8080
	 * @param apiAccessKey
	 *            Redmine API access key. It is shown on "My Account" /
	 *            "API access key" webpage (check
	 *            <i>http://redmine_server_url/my/account<i> URL). This
	 *            parameter is <b>optional</b> (can be set to NULL) for Redmine
	 *            projects, which are "public".
	 */
	public RedmineManager(String host, String apiAccessKey) {
		this.transport = new Transport(new URIConfigurator(host, apiAccessKey));
	}

	/**
	 * Sample usage:
	 * <p/>
	 * <p/>
	 * 
	 * <pre>
	 * {@code
	 *   Issue issueToCreate = new Issue();
	 *   issueToCreate.setSubject("This is the summary line 123");
	 *   Issue newIssue = mgr.createIssue(PROJECT_KEY, issueToCreate);
	 * }
	 * 
	 * @param projectKey The project "identifier". This is a string key like "project-ABC", NOT a database numeric ID.
	 * @param issue      the Issue object to create on the server.
	 * @return the newly created Issue.
	 * @throws RedmineAuthenticationException invalid or no API access key is used with the server, which
	 *                                 requires authorization. Check the constructor arguments.
	 * @throws NotFoundException       the project with the given projectKey is not found
	 * @throws RedmineException
	 */
	public Issue createIssue(String projectKey, Issue issue)
			throws RedmineException {
		final Project oldProject = issue.getProject();
		final Project newProject = new Project();
		newProject.setIdentifier(projectKey);
		issue.setProject(newProject);
		try {
			return transport.addObject(issue, new BasicNameValuePair("include",
					INCLUDE.attachments.toString()));
		} finally {
			issue.setProject(oldProject);
		}
	}

	/**
	 * Load the list of projects available to the user, which is represented by
	 * the API access key.
	 * 
	 * @return list of Project objects
	 * @throws RedmineAuthenticationException
	 *             invalid or no API access key is used with the server, which
	 *             requires authorization. Check the constructor arguments.
	 * @throws RedmineException
	 */
	public List<Project> getProjects() throws RedmineException {
		try {
			return transport.getObjectsList(Project.class,
					new BasicNameValuePair("include", "trackers"));
		} catch (NotFoundException e) {
			throw new RedmineInternalError(
					"NotFoundException received, which should never happen in this request");
		}
	}

	/**
	 * There could be several issues with the same summary, so the method
	 * returns List.
	 * 
	 * @return empty list if not issues with this summary field exist, never
	 *         NULL
	 * @throws RedmineAuthenticationException
	 *             invalid or no API access key is used with the server, which
	 *             requires authorization. Check the constructor arguments.
	 * @throws NotFoundException
	 * @throws RedmineException
	 */
	public List<Issue> getIssuesBySummary(String projectKey, String summaryField)
			throws RedmineException {
		if ((projectKey != null) && (projectKey.length() > 0)) {
			return transport.getObjectsList(Issue.class,
					new BasicNameValuePair("subject", summaryField),
					new BasicNameValuePair("project_id", projectKey));
		} else {
			return transport.getObjectsList(Issue.class,
					new BasicNameValuePair("subject", summaryField));
		}
	}

	/**
	 * Generic method to search for issues.
	 * 
	 * @param pParameters
	 *            the http parameters key/value pairs to append to the rest api
	 *            request
	 * @return empty list if not issues with this summary field exist, never
	 *         NULL
	 * @throws RedmineAuthenticationException
	 *             invalid or no API access key is used with the server, which
	 *             requires authorization. Check the constructor arguments.
	 * @throws NotFoundException
	 * @throws RedmineException
	 */
	public List<Issue> getIssues(Map<String, String> pParameters)
			throws RedmineException {
		Set<NameValuePair> params = new HashSet<NameValuePair>();

		for (final Entry<String, String> param : pParameters.entrySet()) {
			params.add(new BasicNameValuePair(param.getKey(), param.getValue()));
		}

		return transport.getObjectsList(Issue.class, params);
	}

	/**
	 * @param id
	 *            the Redmine issue ID
	 * @param include
	 *            list of "includes". e.g. "relations", "journals", ...
	 * @return Issue object
	 * @throws RedmineAuthenticationException
	 *             invalid or no API access key is used with the server, which
	 *             requires authorization. Check the constructor arguments.
	 * @throws NotFoundException
	 *             the issue with the given id is not found on the server
	 * @throws RedmineException
	 */
	public Issue getIssueById(Integer id, INCLUDE... include)
			throws RedmineException {
		String value = join(",", include);
		return transport.getObject(Issue.class, id, new BasicNameValuePair(
				"include", value));
	}

	// TODO move to a separate utility class or find a replacement in Google
	// Guava
	// TODO add unit tests
	private static String join(String delimToUse, INCLUDE... include) {
		String delim = "";
		StringBuilder sb = new StringBuilder();
		for (INCLUDE i : include) {
			sb.append(delim).append(i);
			delim = delimToUse;
		}
		return sb.toString();
	}

	/**
	 * @param projectKey
	 *            string key like "project-ABC", NOT a database numeric ID
	 * @return Redmine's project
	 * @throws RedmineAuthenticationException
	 *             invalid or no API access key is used with the server, which
	 *             requires authorization. Check the constructor arguments.
	 * @throws NotFoundException
	 *             the project with the given key is not found
	 * @throws RedmineException
	 */
	public Project getProjectByKey(String projectKey) throws RedmineException {
		return transport.getObject(Project.class, projectKey,
				new BasicNameValuePair("include", "trackers"));
	}

	/**
	 * @param projectKey
	 *            string key like "project-ABC", NOT a database numeric ID
	 * @throws RedmineAuthenticationException
	 *             invalid or no API access key is used with the server, which
	 *             requires authorization. Check the constructor arguments.
	 * @throws NotFoundException
	 *             if the project with the given key is not found
	 * @throws RedmineException
	 */
	public void deleteProject(String projectKey) throws RedmineException {
		transport.deleteObject(Project.class, projectKey);
	}

	public void deleteIssue(Integer id) throws RedmineException {
		transport.deleteObject(Issue.class, Integer.toString(id));
	}

	/**
	 * @param projectKey
	 *            ignored if NULL
	 * @param queryId
	 *            id of the saved query in Redmine. the query must be accessible
	 *            to the user represented by the API access key (if the Redmine
	 *            project requires authorization). This parameter is
	 *            <b>optional<b>, NULL can be provided to get all available
	 *            issues.
	 * @return list of Issue objects
	 * @throws RedmineAuthenticationException
	 *             invalid or no API access key is used with the server, which
	 *             requires authorization. Check the constructor arguments.
	 * @throws RedmineException
	 * @see Issue
	 */
	public List<Issue> getIssues(String projectKey, Integer queryId,
			INCLUDE... include) throws RedmineException {
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		if (queryId != null) {
			params.add(new BasicNameValuePair("query_id", String
					.valueOf(queryId)));
		}

		if ((projectKey != null) && (projectKey.length() > 0)) {
			params.add(new BasicNameValuePair("project_id", projectKey));
		}
		String includeStr = join(",", include);
		params.add(new BasicNameValuePair("include", includeStr));

		return transport.getObjectsList(Issue.class, params);
	}

	/*
	 * note: This method cannot return the updated object from Redmine because
	 * the server does not provide any XML in response.
	 * 
	 * @since 1.8.0
	 */
	public void update(Identifiable obj) throws RedmineException {
		validate(obj);
		transport.updateObject(obj);
	}

	private void validate(Identifiable obj) {
		// TODO this is a temporary step during refactoring. remove this class
		// check, make it generic.
		// maybe add validate() method to the objects themselves, although need
		// to remember that
		// there could be several "valid" states - e.g. "Valid to create"m
		// "valid to update".
		if (obj instanceof TimeEntry && !((TimeEntry) obj).isValid()) {
			throw createIllegalTimeEntryException();
		}
	}

	/**
	 * Sample usage:
	 * <p/>
	 * <p/>
	 * 
	 * <pre>
	 * {
	 * 	&#064;code
	 * 	Project project = new Project();
	 * 	Long timeStamp = Calendar.getInstance().getTimeInMillis();
	 * 	String key = &quot;projkey&quot; + timeStamp;
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
	 * @param project
	 *            project to create on the server
	 * @return the newly created Project object.
	 * @throws RedmineAuthenticationException
	 *             invalid or no API access key is used with the server, which
	 *             requires authorization. Check the constructor arguments.
	 * @throws RedmineException
	 */
	public Project createProject(Project project) throws RedmineException {
		return transport.addObject(project, new BasicNameValuePair("include",
				"trackers"));
	}

	/**
	 * This number of objects (tasks, projects, users) will be requested from
	 * Redmine server in 1 request.
	 */
	public int getObjectsPerPage() {
		return transport.getObjectsPerPage();
	}

	// TODO add test

	/**
	 * This number of objects (tasks, projects, users) will be requested from
	 * Redmine server in 1 request.
	 */
	public void setObjectsPerPage(int pageSize) {
		transport.setObjectsPerPage(pageSize);
	}

	/**
	 * Load the list of users on the server.
	 * <p>
	 * <b>This operation requires "Redmine Administrator" permission.</b>
	 * 
	 * @return list of User objects
	 * @throws RedmineAuthenticationException
	 *             invalid or no API access key is used with the server, which
	 *             requires authorization. Check the constructor arguments.
	 * @throws NotFoundException
	 * @throws RedmineException
	 */
	public List<User> getUsers() throws RedmineException {
		return transport.getObjectsList(User.class);
	}

	public User getUserById(Integer userId) throws RedmineException {
		return transport.getObject(User.class, userId);
	}

	/**
	 * @return the current user logged into Redmine
	 */
	public User getCurrentUser() throws RedmineException {
		return transport.getCurrentUser();
	}

	public User createUser(User user) throws RedmineException {
		return transport.addObject(user);
	}

	/**
	 * @param userId
	 *            user identifier (numeric ID)
	 * @throws RedmineAuthenticationException
	 *             invalid or no API access key is used with the server, which
	 *             requires authorization. Check the constructor arguments.
	 * @throws NotFoundException
	 *             if the user with the given id is not found
	 * @throws RedmineException
	 */
	public void deleteUser(Integer userId) throws RedmineException {
		transport.deleteObject(User.class, Integer.toString(userId));
	}

	public List<TimeEntry> getTimeEntries() throws RedmineException {
		return transport.getObjectsList(TimeEntry.class);
	}

	/**
	 * @param id
	 *            the database Id of the TimeEntry record
	 */
	public TimeEntry getTimeEntry(Integer id) throws RedmineException {
		return transport.getObject(TimeEntry.class, id);
	}

	public List<TimeEntry> getTimeEntriesForIssue(Integer issueId)
			throws RedmineException {
		return transport.getObjectsList(TimeEntry.class,
				new BasicNameValuePair("issue_id", Integer.toString(issueId)));
	}

	public TimeEntry createTimeEntry(TimeEntry obj) throws RedmineException {
		validate(obj);
		return transport.addObject(obj);
	}

	public void deleteTimeEntry(Integer id) throws RedmineException {
		transport.deleteObject(TimeEntry.class, Integer.toString(id));
	}

	private IllegalArgumentException createIllegalTimeEntryException() {
		return new IllegalArgumentException(
				"You have to either define a Project or Issue ID for a Time Entry. "
						+ "The given Time Entry object has neither defined.");
	}

	/**
	 * Get "saved queries" for the given project available to the current user.
	 * <p/>
	 * <p>
	 * This REST API feature was added in Redmine 1.3.0. See
	 * http://www.redmine.org/issues/5737
	 */
	public List<SavedQuery> getSavedQueries(String projectKey)
			throws RedmineException {
		Set<NameValuePair> params = new HashSet<NameValuePair>();

		if ((projectKey != null) && (projectKey.length() > 0)) {
			params.add(new BasicNameValuePair("project_id", projectKey));
		}

		return transport.getObjectsList(SavedQuery.class, params);
	}

	/**
	 * Get all "saved queries" available to the current user.
	 * <p/>
	 * <p>
	 * This REST API feature was added in Redmine 1.3.0. See
	 * http://www.redmine.org/issues/5737
	 */
	public List<SavedQuery> getSavedQueries() throws RedmineException {
		return transport.getObjectsList(SavedQuery.class);
	}

	public IssueRelation createRelation(Integer issueId, Integer issueToId,
			String type) throws RedmineException {
		IssueRelation toCreate = new IssueRelation();
		toCreate.setIssueId(issueId);
		toCreate.setIssueToId(issueToId);
		toCreate.setType(type);
		return transport.addChildEntry(Issue.class, issueId, toCreate);
	}

	/**
	 * Delete Issue Relation with the given ID.
	 */
	public void deleteRelation(Integer id) throws RedmineException {
		transport.deleteObject(IssueRelation.class, Integer.toString(id));
	}

	/**
	 * Delete all issue's relations
	 */
	public void deleteIssueRelations(Issue redmineIssue)
			throws RedmineException {
		for (IssueRelation relation : redmineIssue.getRelations()) {
			deleteRelation(relation.getId());
		}
	}

	/**
	 * Delete relations for the given issue ID.
	 * 
	 * @param id
	 *            issue ID
	 */
	public void deleteIssueRelationsByIssueId(Integer id)
			throws RedmineException {
		Issue issue = getIssueById(id, INCLUDE.relations);
		deleteIssueRelations(issue);
	}

	/**
	 * Delivers a list of existing {@link org.redmine.ta.beans.IssueStatus}es.
	 * 
	 * @return a list of existing {@link org.redmine.ta.beans.IssueStatus}es.
	 * @throws RedmineAuthenticationException
	 *             thrown in case something went wrong while trying to login
	 * @throws RedmineException
	 *             thrown in case something went wrong in Redmine
	 * @throws NotFoundException
	 *             thrown in case an object can not be found
	 */
	public List<IssueStatus> getStatuses() throws RedmineException {
		return transport.getObjectsList(IssueStatus.class);
	}

	/**
	 * creates a new {@link Version} for the {@link Project} contained. <br/>
	 * Pre-condition: the attribute {@link Project} for the {@link Version} must
	 * not be null!
	 * 
	 * @param version
	 *            the {@link Version}. Must contain a {@link Project}.
	 * @return the new {@link Version} created by Redmine
	 * @throws IllegalArgumentException
	 *             thrown in case the version does not contain a project.
	 * @throws RedmineAuthenticationException
	 *             thrown in case something went wrong while trying to login
	 * @throws RedmineException
	 *             thrown in case something went wrong in Redmine
	 * @throws NotFoundException
	 *             thrown in case an object can not be found
	 */
	public Version createVersion(Version version) throws RedmineException {
		// check project
		if (version.getProject() == null
				|| version.getProject().getId() == null) {
			throw new IllegalArgumentException(
					"Version must contain an existing project");
		}
		return transport.addChildEntry(Project.class, version.getProject()
				.getId(), version);
	}

	/**
	 * deletes a new {@link Version} from the {@link Project} contained. <br/>
	 * 
	 * @param version
	 *            the {@link Version}.
	 * @throws RedmineAuthenticationException
	 *             thrown in case something went wrong while trying to login
	 * @throws RedmineException
	 *             thrown in case something went wrong in Redmine
	 * @throws NotFoundException
	 *             thrown in case an object can not be found
	 */
	public void deleteVersion(Version version) throws RedmineException {
		transport
				.deleteObject(Version.class, Integer.toString(version.getId()));
	}

	/**
	 * delivers a list of {@link Version}s of a {@link Project}
	 * 
	 * @param projectID
	 *            the ID of the {@link Project}
	 * @return the list of {@link Version}s of the {@link Project}
	 * @throws RedmineAuthenticationException
	 *             thrown in case something went wrong while trying to login
	 * @throws RedmineException
	 *             thrown in case something went wrong in Redmine
	 * @throws NotFoundException
	 *             thrown in case an object can not be found
	 */
	public List<Version> getVersions(int projectID) throws RedmineException {
		return transport.getChildEntries(Project.class, projectID,
				Version.class);
	}

	// TODO add test
	public Version getVersionById(int versionId) throws RedmineException {
		return transport.getObject(Version.class, versionId);
	}

	/**
	 * delivers a list of {@link IssueCategory}s of a {@link Project}
	 * 
	 * @param projectID
	 *            the ID of the {@link Project}
	 * @return the list of {@link IssueCategory}s of the {@link Project}
	 * @throws RedmineAuthenticationException
	 *             thrown in case something went wrong while trying to login
	 * @throws RedmineException
	 *             thrown in case something went wrong in Redmine
	 * @throws NotFoundException
	 *             thrown in case an object can not be found
	 */
	public List<IssueCategory> getCategories(int projectID)
			throws RedmineException {
		return transport.getChildEntries(Project.class, projectID,
				IssueCategory.class);
	}

	/**
	 * creates a new {@link IssueCategory} for the {@link Project} contained. <br/>
	 * Pre-condition: the attribute {@link Project} for the
	 * {@link IssueCategory} must not be null!
	 * 
	 * @param category
	 *            the {@link IssueCategory}. Must contain a {@link Project}.
	 * @return the new {@link IssueCategory} created by Redmine
	 * @throws IllegalArgumentException
	 *             thrown in case the category does not contain a project.
	 * @throws RedmineAuthenticationException
	 *             thrown in case something went wrong while trying to login
	 * @throws RedmineException
	 *             thrown in case something went wrong in Redmine
	 * @throws NotFoundException
	 *             thrown in case an object can not be found
	 */
	public IssueCategory createCategory(IssueCategory category)
			throws RedmineException {
		if (category.getProject() == null
				|| category.getProject().getId() == null) {
			throw new IllegalArgumentException(
					"IssueCategory must contain an existing project");
		}

		return transport.addChildEntry(Project.class, category.getProject()
				.getId(), category);
	}

	/**
	 * deletes an {@link IssueCategory}. <br/>
	 * 
	 * @param category
	 *            the {@link IssueCategory}.
	 * @throws RedmineAuthenticationException
	 *             thrown in case something went wrong while trying to login
	 * @throws RedmineException
	 *             thrown in case something went wrong in Redmine
	 * @throws NotFoundException
	 *             thrown in case an object can not be found
	 */
	public void deleteCategory(IssueCategory category) throws RedmineException {
		transport.deleteObject(IssueCategory.class,
				Integer.toString(category.getId()));
	}

	/**
	 * @return a list of all {@link Tracker}s available
	 * @throws RedmineAuthenticationException
	 *             thrown in case something went wrong while trying to login
	 * @throws RedmineException
	 *             thrown in case something went wrong in Redmine
	 * @throws NotFoundException
	 *             thrown in case an object can not be found
	 */
	public List<Tracker> getTrackers() throws RedmineException {
		return transport.getObjectsList(Tracker.class);
	}

	/**
	 * Delivers an {@link org.redmine.ta.beans.Attachment} by its ID.
	 * 
	 * @param attachmentID
	 *            the ID
	 * @return the {@link org.redmine.ta.beans.Attachment}
	 * @throws RedmineAuthenticationException
	 *             thrown in case something went wrong while trying to login
	 * @throws RedmineException
	 *             thrown in case something went wrong in Redmine
	 * @throws NotFoundException
	 *             thrown in case an object can not be found
	 */
	public Attachment getAttachmentById(int attachmentID)
			throws RedmineException {
		return transport.getObject(Attachment.class, attachmentID);
	}

	/*
	 * It does not work at all. It requires authentication!
	 */
	/**
	 * Downloads the content of an {@link org.redmine.ta.beans.Attachment} from
	 * the Redmine server.
	 * 
	 * @param issueAttachment
	 *            the {@link org.redmine.ta.beans.Attachment}
	 * @return the content of the attachment as a byte[] array
	 * @throws RedmineCommunicationException
	 *             thrown in case the download fails
	 */
	public byte[] downloadAttachmentContent(Attachment issueAttachment)
			throws RedmineCommunicationException {
		try {
			final URL url = new URL(issueAttachment.getContentURL());
			final InputStream is = url.openStream();
			try {
				final ByteArrayOutputStream baos = new ByteArrayOutputStream();
				final byte[] buffer = new byte[65536];
				int read;
				while ((read = is.read(buffer)) != -1)
					baos.write(buffer, 0, read);
				return baos.toByteArray();
			} finally {
				is.close();
			}
		} catch (IOException e) {
			throw new RedmineTransportException(e);
		}
	}

	public void setLogin(String login) {
		transport.setLogin(login);
	}

	public void setPassword(String password) {
		transport.setPassword(password);
	}

	/**
	 * @param projectKey
	 *            ignored if NULL
	 * @return list of news objects
	 * @see News
	 */
	public List<News> getNews(String projectKey) throws RedmineException {
		Set<NameValuePair> params = new HashSet<NameValuePair>();
		if ((projectKey != null) && (projectKey.length() > 0)) {
			params.add(new BasicNameValuePair("project_id", projectKey));
		}
		return transport.getObjectsList(News.class, params);
	}

	/**
	 * Uploads an attachement.
	 * 
	 * @param fileName
	 *            file name of the attachement.
	 * @param contentType
	 *            content type of the attachement.
	 * @param content
	 *            attachement content stream.
	 * @return attachement content.
	 * @throws RedmineException
	 *             if something goes wrong.
	 * @throws IOException
	 *             if input cannot be read. This exception cannot be thrown yet
	 *             (I am not sure if http client can distinguish "network"
	 *             errors and local errors) but is will be good to distinguish
	 *             reading errors and transport errors.
	 */
	public Attachment uploadAttachment(String fileName, String contentType,
			InputStream content) throws RedmineException, IOException {
		final String token = transport.upload(content);
		final Attachment result = new Attachment();
		result.setToken(token);
		result.setContentType(contentType);
		result.setFileName(fileName);
		return result;
	}

	/**
	 * Uploads an attachement.
	 * 
	 * @param fileName
	 *            file name of the attachement.
	 * @param contentType
	 *            content type of the attachement.
	 * @param content
	 *            attachement content stream.
	 * @return attachement content.
	 * @throws RedmineException
	 *             if something goes wrong.
	 * @throws IOException
	 *             if input cannot be read.
	 */
	public Attachment uploadAttachment(String fileName, String contentType,
			byte[] content) throws RedmineException, IOException {
		final InputStream is = new ByteArrayInputStream(content);
		try {
			return uploadAttachment(fileName, contentType, is);
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				throw new RedmineInternalError("Unexpected exception", e);
			}
		}
	}

	/**
	 * Uploads an attachement.
	 * 
	 * @param contentType
	 *            content type of the attachement.
	 * @param content
	 *            attachement content stream.
	 * @return attachement content.
	 * @throws RedmineException
	 *             if something goes wrong.
	 * @throws IOException
	 *             if input cannot be read.
	 */
	public Attachment uploadAttachment(String contentType, File content)
			throws RedmineException, IOException {
		final InputStream is = new FileInputStream(content);
		try {
			return uploadAttachment(content.getName(), contentType, is);
		} finally {
			is.close();
		}
	}
}
