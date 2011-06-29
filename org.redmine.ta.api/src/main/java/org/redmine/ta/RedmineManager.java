/*
   Copyright 2010-2011 Alexey Skorokhodov.

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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.redmine.ta.beans.Identifiable;
import org.redmine.ta.beans.Issue;
import org.redmine.ta.beans.Project;
import org.redmine.ta.beans.TimeEntry;
import org.redmine.ta.beans.User;
import org.redmine.ta.internal.HttpUtil;
import org.redmine.ta.internal.RedmineXMLGenerator;
import org.redmine.ta.internal.RedmineXMLParser;


/**
 * <b>Entry point</b> for the API: use this class to communicate with Redmine servers.
 * 
 * @author Alexey Skorokhodov
 */
public class RedmineManager {
	private static final String CONTENT_TYPE = "text/xml; charset=utf-8";
	private static final String CHARSET = "UTF-8";

	private static final int DEFAULT_OBJECTS_PER_PAGE = 25;
	
	private static enum MODE {
		REDMINE_1_0, REDMINE_1_1_OR_CHILIPROJECT_1_2, 
	}

	private String host;
	private String apiAccessKey;
	private String login;
	private String password;
	private boolean useBasicAuth = false;
	
	private int objectsPerPage = DEFAULT_OBJECTS_PER_PAGE;
	
	private MODE currentMode = MODE.REDMINE_1_1_OR_CHILIPROJECT_1_2;

	private static final Map<Class, String> urls = new HashMap<Class, String>() {
		private static final long serialVersionUID = 1L;
		{
			put(User.class, "users");
			put(Issue.class, "issues");
			put(Project.class, "projects");
			put(TimeEntry.class, "time_entries");
		}
	};
	private static final String URL_POSTFIX = ".xml";

	public RedmineManager(String host) {
		if (host == null || host.isEmpty()) {
			throw new IllegalArgumentException("The host parameter is NULL or empty");
		}
		this.host = host;
	}
	
	/**
	 * Creates an instance of RedmineManager class. Host and apiAccessKey are not checked at this moment.
	 * 
	 * @param host complete Redmine server web URI, including protocol and port number. Example: http://demo.redmine.org:8080
	 * @param apiAccessKey Redmine API access key. It is shown on "My Account" / "API access key" webpage 
	 *   (check  <i>http://redmine_server_url/my/account<i> URL).
	 *   This parameter is <b>optional</b> (can be set to NULL) for Redmine projects, which are "public". 
	 */
	public RedmineManager(String host, String apiAccessKey) {
		this(host);
		this.apiAccessKey = apiAccessKey;
	}

	public RedmineManager(String host, String login, String password) {
		this(host);
		this.login = login;
		this.password = password;
		this.useBasicAuth = true;
	}
	
	/**
	 * Sample usage:
	 * <p>
	 * 
	 * <pre>
	 * {@code
	 *   Issue issueToCreate = new Issue();
	 *   issueToCreate.setSubject("This is the summary line 123");
	 *   Issue newIssue = mgr.createIssue(PROJECT_KEY, issueToCreate);
	 * }
	 * <p>
	 * 
	 * @param projectKey The project "identifier". This is a string key like "project-ABC", NOT a database numeric ID.
	 * @param issue the Issue object to create on the server.
	 * 
	 * @return the newly created Issue.
	 * 
	 * @throws IOException
	 * @throws AuthenticationException
	 *             invalid or no API access key is used with the server, which
	 *             requires authorization. Check the constructor arguments.
	 * @throws NotFoundException
	 * the project with the given projectKey is not found
	 * @throws RedmineException 
	 */
	public Issue createIssue(String projectKey, Issue issue) throws IOException,AuthenticationException, NotFoundException, RedmineException {
        URI uri = createURI("/issues.xml");
		HttpPost http = new HttpPost(uri);
		String xmlBody = RedmineXMLGenerator.toXML(projectKey, issue);
		
		setEntity(http, xmlBody);
		Response response = sendRequest(http);
		if (response.getCode() == HttpStatus.SC_NOT_FOUND) {
			throw new NotFoundException("Project with key '" + projectKey + "' is not found.");
		}
		Issue newIssue = RedmineXMLParser.parseIssueFromXML(response.getBody());
		return newIssue;
	}
	
	private URI createURI(String query) {
		return createURI(query, new ArrayList<NameValuePair>());
	}

	private URI createURI(String query, NameValuePair... param) {
		List<NameValuePair> list =  new ArrayList<NameValuePair>();
		for (NameValuePair p : param) {
			list.add(p);
		}
		return createURI(query, list);
	}

	/**
	 * @param query e.g. "/issues.xml"
	 * @return URI with auth parameter "key" if not in "basic auth mode.
	 */
	private URI createURI(String query, List<NameValuePair> params) {
		if (!useBasicAuth) {
			params.add(new BasicNameValuePair("key", apiAccessKey));
		}
		URI uri;
		try {
			URL url = new URL(host);
			uri = URIUtils.createURI(url.getProtocol(), url.getHost(), url.getPort(), query, 
				    URLEncodedUtils.format(params, "UTF-8"), null);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return uri;
	}
	
	/**
	 * Note: This method cannot return the updated Issue from Redmine 
	 * because the server does not provide any XML in response.
	 * 
	 * @param issue the Issue to update on the server. issue.getId() is used for identification.
	 *  
	 * @throws IOException
	 * @throws AuthenticationException
	 *             invalid or no API access key is used with the server, which
	 *             requires authorization. Check the constructor arguments.
	 * @throws NotFoundException the issue with the required ID is not found
	 * @throws RedmineException 
	 */
	public void updateIssue(Issue issue) throws IOException, AuthenticationException, NotFoundException, RedmineException {
		URI uri = createURI("/issues/" + issue.getId() + ".xml");
		HttpPut httpRequest = new HttpPut(uri);

		// XXX add "notes" xml node. see http://www.redmine.org/wiki/redmine/Rest_Issues
		String NO_PROJECT_KEY = null;
		String xmlBody = RedmineXMLGenerator.toXML(NO_PROJECT_KEY, issue);
//		System.out.println(xmlBody);
		setEntity(httpRequest, xmlBody);
		Response response = sendRequest(httpRequest);
		if (response.getCode() == HttpStatus.SC_NOT_FOUND) {
			throw new NotFoundException("Issue with id="+ issue.getId() + " is not found.");
		}
	}
	
	private void setEntity(HttpEntityEnclosingRequest request, String xmlBody) throws UnsupportedEncodingException {
		StringEntity entity = new StringEntity(xmlBody, CHARSET);
		entity.setContentType(CONTENT_TYPE);
		request.setEntity(entity);
	}
	
	private Response sendRequest(HttpRequest request) throws ClientProtocolException, IOException, AuthenticationException, RedmineException {
//		System.out.println(request.getRequestLine());
		DefaultHttpClient httpclient = HttpUtil.getNewHttpClient();
		
		if (useBasicAuth) {
			httpclient.getCredentialsProvider().setCredentials(
                new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT),
                new UsernamePasswordCredentials(login, password));
		}
		
		HttpResponse httpResponse = httpclient.execute((HttpUriRequest)request);
		
//		System.out.println(httpResponse.getStatusLine());
		int responseCode = httpResponse.getStatusLine().getStatusCode();
		if (responseCode ==	HttpStatus.SC_UNAUTHORIZED) {
			throw new AuthenticationException("Authorization error. Please check if you provided a valid API access key or Login and Password.");
		}
		if (responseCode ==	HttpStatus.SC_FORBIDDEN) {
			throw new AuthenticationException("Forbidden. The API access key you used does not allow this operation. Please check the user has proper permissions.");
		}
		
		HttpEntity responseEntity = httpResponse.getEntity();
		String responseBody = EntityUtils.toString(responseEntity);

		if (responseCode ==	HttpStatus.SC_UNPROCESSABLE_ENTITY) {
			List<String> errors = RedmineXMLParser.parseErrors(responseBody);   
			throw new RedmineException(errors);
		}
		/* 422 "invalid"
		<?xml version="1.0" encoding="UTF-8"?>
		<errors>
		  <error>Name can't be blank</error>
		  <error>Identifier has already been taken</error>
		</errors>
		 */
		

		// have to fill our own object, otherwise there's no guarantee 
		// that the request body can be retrieved later ("socket closed" exception can occur) 
		Response r = new Response(responseCode, responseBody);
		httpclient.getConnectionManager().shutdown();
//		String responseBody = EntityUtils.toString(responseEntity);
		return r;
	}
	
	class Response {
		private int code;
		private String body;
		public Response(int code, String body) {
			super();
			this.code = code;
			this.body = body;
		}
		public int getCode() {
			return code;
		}
		public String getBody() {
			return body;
		}
		
	}
	

	
	/**
	 * Load the list of projects available to the user, which is represented by the API access key.
	 * 
	 * @return list of Project objects
	 * 
	 * @throws AuthenticationException
	 *             invalid or no API access key is used with the server, which
	 *             requires authorization. Check the constructor arguments.
	 * @throws RedmineException 
	 * @throws NotFoundException 
	 */
	public List<Project> getProjects() throws IOException,AuthenticationException, RedmineException {
		Map<String, NameValuePair> params = new HashMap<String, NameValuePair>();
		params.put("include", new BasicNameValuePair("include", "trackers"));
		try {
			return getObjectsList(Project.class, params);
		} catch (NotFoundException e) {
			throw new RuntimeException("NotFoundException received, which should never happen in this request");
		}
	}
	
	/**
	 * There could be several issues with the same summary, so the method returns List. 
	 * 
	 * @param summaryField
	 * 
	 * @return empty list if not issues with this summary field exist, never NULL
	 * 
	 * @throws AuthenticationException
	 *             invalid or no API access key is used with the server, which
	 *             requires authorization. Check the constructor arguments.
	 * @throws NotFoundException 
	 * @throws RedmineException 
	 */
	public List<Issue> getIssuesBySummary(String projectKey, String summaryField) throws IOException, AuthenticationException, NotFoundException, RedmineException {
		Map<String, NameValuePair> params = new HashMap<String, NameValuePair>();
		params.put("subject", new BasicNameValuePair("subject", summaryField));

		if ((projectKey != null) && (projectKey.length()>0)) {
			params.put("project_id", new BasicNameValuePair("project_id", projectKey));
		}

		return getObjectsList(Issue.class, params);
	}
	
	/**
	 * Does not include "journal" entries. Equivalent to calling getIssueById(id, false). 
	 * 
	 * @param id the Redmine issue ID
	 * @return Issue object
	 * @throws IOException
	 * @throws AuthenticationException
	 *             invalid or no API access key is used with the server, which
	 *             requires authorization. Check the constructor arguments.
	 * @throws NotFoundException the issue with the given id is not found on the server
	 * @throws RedmineException 
	 */
	public Issue getIssueById(Integer id) throws IOException, AuthenticationException, NotFoundException, RedmineException {
		return getIssueById(id, false);
	}

	/**
	 * 
	 * @param id  the Redmine issue ID
	 * @param includeJournals get issue journals
	 * @return Issue object
	 * @throws IOException
	 * @throws AuthenticationException
	 *             invalid or no API access key is used with the server, which
	 *             requires authorization. Check the constructor arguments.
	 * @throws NotFoundException
	 *             the issue with the given id is not found on the server
	 * @throws RedmineException
	 */
	public Issue getIssueById(Integer id, boolean includeJournals)
			throws IOException, AuthenticationException, NotFoundException,
			RedmineException {
		return getObject(Issue.class, id, new BasicNameValuePair("include", "journals"));
	}
	
	/**
	 * 
	 * @param projectKey string key like "project-ABC", NOT a database numeric ID
	 * 
	 * @return Redmine's project
	 * 
	 * @throws AuthenticationException
	 *             invalid or no API access key is used with the server, which
	 *             requires authorization. Check the constructor arguments.
	 * @throws NotFoundException the project with the given key is not found
	 * @throws RedmineException 
	 */
	public Project getProjectByKey(String projectKey) throws IOException, AuthenticationException, NotFoundException, RedmineException {
        URI uri = createURI("/projects/" + projectKey + ".xml", new BasicNameValuePair("include", "trackers"));
		HttpGet http = new HttpGet(uri);
		Response response = sendRequest(http);
		if (response.getCode() == HttpStatus.SC_NOT_FOUND) {
			throw new NotFoundException("Project with key '" + projectKey + "' is not found.");
		}
		Project projectFromServer = RedmineXMLParser.parseProjectFromXML(response.getBody());
		return projectFromServer;
	}

	/**
	 * @param projectKey string key like "project-ABC", NOT a database numeric ID
	 *
	 * @throws AuthenticationException
	 *             invalid or no API access key is used with the server, which
	 *             requires authorization. Check the constructor arguments.
	 * @throws NotFoundException if the project with the given key is not found
	 * @throws RedmineException 
	 */
	public void deleteProject(String projectKey) throws IOException, AuthenticationException, NotFoundException, RedmineException {
		deleteObject(Project.class, projectKey);
	}

	public void deleteIssue(Integer id) throws IOException,
			AuthenticationException, NotFoundException, RedmineException {
		deleteObject(Issue.class, Integer.toString(id));
	}

	/**
	 * 
	 * @param projectKey
	 * @param queryId id of the saved query in Redmine. the query must be accessible to the user 
	 *   represented by the API access key (if the Redmine project requires authorization). 
	 *   This parameter is <b>optional<b>, NULL can be provided to get all available issues.
	 * 
	 * @return list of Issue objects
	 * @throws IOException
	 * 
	 * @throws AuthenticationException
	 *             invalid or no API access key is used with the server, which
	 *             requires authorization. Check the constructor arguments.
	 * @throws RedmineException 
	 *             
	 * @see Issue
	 */
	public List<Issue> getIssues(String projectKey, Integer queryId) throws IOException, AuthenticationException, NotFoundException, RedmineException {
		// have to load users first because the issues response does not contain the users names
		// see http://www.redmine.org/issues/7487
//		List<User> users = getUsers();
//		Map<Integer, User> idToUserMap = buildIdToUserMap(users);
		
		Map<String, NameValuePair> params = new HashMap<String, NameValuePair>();
		if (queryId != null) {
			params.put("query_id", new BasicNameValuePair("query_id", String.valueOf(queryId)));
		}

		if ((projectKey != null) && (projectKey.length()>0)) {
			params.put("project_id", new BasicNameValuePair("project_id", projectKey));
		}

		List<Issue> issues = getObjectsList(Issue.class, params);
//		setUserFields(issues, idToUserMap);
		return issues;
	}
	
//	private void setUserFields(List<Issue> issues,
//			Map<Integer, User> idToUserMap) {
//		for (Issue issue : issues) {
//			User author = issue.getAuthor();
//			if (author != null) {
//				User completelyFilledUser = idToUserMap.get(author.getId());
//				issue.setAuthor(completelyFilledUser);
//			}
//			User assignee = issue.getAssignee();
//			if (assignee != null) {
//				User completelyFilledUser = idToUserMap.get(author.getId());
//				issue.setAssignee(completelyFilledUser);
//			}
//		}
//	}
//	private Map<Integer, User> buildIdToUserMap(List<User> usersList) {
//		Map<Integer, User> idToUserMap = new HashMap<Integer, User>();
//		for (User u : usersList) {
//			idToUserMap.put(u.getId(), u);
//		}
//		return idToUserMap;
//	}

	/**
	 * This ONLY works with Redmine 1.0.  Redmine 1.1 uses "objects per page" parameter instead!
	 */
	private void addPagingParameters(Map<String, NameValuePair> params) {
		params.put("per_page", new BasicNameValuePair("per_page", String.valueOf(objectsPerPage)));
	}
	
	/**
	 * Redmine 1.0 - specific version
	 */
	private <T> List<T> getObjectsListV104(Class<T> objectClass, Map<String, NameValuePair> params) throws IOException, AuthenticationException, NotFoundException, RedmineException {
		List<T>  objects = new ArrayList<T>();
		
		final int FIRST_REDMINE_PAGE = 1;
		int pageNum = FIRST_REDMINE_PAGE;
		// Redmine 1.0.4 (and Trunk at this moment - Dec 22, 2010) returns the same page1 when no other pages are available!!
		String firstPage=null;
		
		addPagingParameters(params);
//		addAuthParameters(params);
		
		do {
			params.put("page", new BasicNameValuePair("page", String.valueOf(pageNum)));
			List<NameValuePair> paramsList = new ArrayList<NameValuePair>(params.values());
			
			String query = urls.get(objectClass) + URL_POSTFIX;
			URI uri = createURI(query, paramsList);

			HttpGet http = new HttpGet(uri);
			Response response = sendRequest(http);
			if (response.getCode() ==	HttpStatus.SC_NOT_FOUND) {
				throw new NotFoundException("Server returned '404 not found'. response body:" + response.getBody());
			}

			String body = response.getBody();
			
			if (pageNum == FIRST_REDMINE_PAGE) {
				firstPage = body;
			} else {
				// check that the response is NOT equal to the First Page
				// - this would indicate that no more pages are available (for Redmine 1.0.*);
				if (firstPage.equals(body)) {
					// done, no more pages. exit the loop
					break;
				}
			}
			List<T> foundItems = RedmineXMLParser.parseObjectsFromXML(objectClass, body);
			if (foundItems.size() == 0) {
				break;
			}
			objects.addAll(foundItems);

			pageNum++;
		} while (true);

		return objects;
	}

	// XXX fix this: why it is Map of string->pair? should be a flat set of params! 
	private <T> List<T> getObjectsList(Class<T> objectClass, Map<String, NameValuePair> params) throws IOException, AuthenticationException, NotFoundException, RedmineException {
		if (currentMode.equals(MODE.REDMINE_1_1_OR_CHILIPROJECT_1_2)) {
			return getObjectsListV11(objectClass, params);
		} else if (currentMode.equals(MODE.REDMINE_1_0)) {
			return getObjectsListV104(objectClass, params);
		} else {
			throw new RuntimeException("unsupported mode:" + currentMode + ". supported modes are: " + 
					MODE.REDMINE_1_0 + " and " + MODE.REDMINE_1_1_OR_CHILIPROJECT_1_2);
		}
	}
	
	/**
	 * Redmine 1.1 / Chiliproject 1.2 - specific version
	 */
	private <T> List<T> getObjectsListV11(Class<T> objectClass, Map<String, NameValuePair> params) throws IOException, AuthenticationException, NotFoundException, RedmineException {
		List<T>  objects = new ArrayList<T>();
		
		int limit = 25;
		params.put("limit", new BasicNameValuePair("limit", String.valueOf(limit)));
		int offset = 0;
		int totalIssuesFoundOnServer;
		do {
			params.put("offset", new BasicNameValuePair("offset", String.valueOf(offset)));
			List<NameValuePair> paramsList = new ArrayList<NameValuePair>(params.values());
			
			String query = urls.get(objectClass) + URL_POSTFIX;
			URI uri = createURI(query, paramsList);

			HttpGet http = new HttpGet(uri);

			Response response = sendRequest(http);
			if (response.getCode() ==	HttpStatus.SC_NOT_FOUND) {
				throw new NotFoundException("Server returned '404 not found'. response body:" + response.getBody());
			}
			String body = response.getBody();
			totalIssuesFoundOnServer = RedmineXMLParser.parseObjectsTotalCount(body);
			
			List<T> foundItems = RedmineXMLParser.parseObjectsFromXML(objectClass, body);
			if (foundItems.size() == 0) {
				break;
			}
			objects.addAll(foundItems);

			offset+= foundItems.size();
		} while (offset<totalIssuesFoundOnServer);

		return objects;
	}

	private <T> T getObject(Class<T> objectClass, Integer id, NameValuePair... params)
			throws IOException, AuthenticationException, NotFoundException,
			RedmineException {

		String query = urls.get(objectClass) + "/" + id + URL_POSTFIX;
		URI uri = createURI(query, params);
		String body = sendGet(uri);
		return RedmineXMLParser.parseObjectFromXML(objectClass, body);
	}

	// TODO is there a way to get rid of the 1st parameter and use generics?
	private <T> T createObject(Class<T> classs, T obj) throws IOException, AuthenticationException, NotFoundException, RedmineException {

		URI uri = getCreateURI(obj.getClass());
		HttpPost http = new HttpPost(uri);
		
		String xml = RedmineXMLGenerator.toXML(obj);
		setEntity((HttpEntityEnclosingRequest)http, xml);

		Response response = sendRequest(http);
		if (response.getCode() ==	HttpStatus.SC_NOT_FOUND) {
			throw new NotFoundException("Server returned '404 not found'. response body:" + response.getBody());
		}
		return RedmineXMLParser.parseObjectFromXML(classs, response.getBody());
		
	}
	
	/*
	 * note: This method cannot return the updated object from Redmine
	 * because the server does not provide any XML in response.
	 */
	private <T extends Identifiable> void updateObject(Class<T> classs, T obj) throws IOException, AuthenticationException, NotFoundException, RedmineException {
		URI uri = getUpdateURI(obj.getClass(), Integer.toString(obj.getId()));
		HttpPut http = new HttpPut(uri);
		
		String xml = RedmineXMLGenerator.toXML(obj);
		setEntity((HttpEntityEnclosingRequest)http, xml);

		Response response = sendRequest(http);
		if (response.getCode() ==	HttpStatus.SC_NOT_FOUND) {
			throw new NotFoundException("Server returned '404 not found'. response body:" + response.getBody());
		}
	}
	
	private <T extends Identifiable> void deleteObject(Class<T> classs, String id) throws IOException, AuthenticationException, NotFoundException, RedmineException {
		URI uri = getUpdateURI(classs, id);
		HttpDelete http = new HttpDelete(uri);
		
		Response response = sendRequest(http);
		if (response.getCode() ==	HttpStatus.SC_NOT_FOUND) {
			throw new NotFoundException("Server returned '404 not found'. response body:" + response.getBody());
		}
	}

	private URI getCreateURI(Class zz) throws MalformedURLException {
		String query =urls.get(zz) + URL_POSTFIX; 
		return createURI(query);
	}

	private URI getUpdateURI(Class zz, String id) throws MalformedURLException {
		String query = urls.get(zz) + "/" + id + URL_POSTFIX;
		return createURI(query);
	}
	
	private String sendGet(URI uri) throws NotFoundException, IOException, AuthenticationException, RedmineException {
		HttpGet http = new HttpGet(uri);
		Response response = sendRequest(http);
		if (response.getCode() ==	HttpStatus.SC_NOT_FOUND) {
			throw new NotFoundException("Server returned '404 not found'. response body:" + response.getBody());
		}

		return response.getBody();
	}

	/**
	 * Sample usage:
	 * <p>
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
	 * @param project
	 *            project to create on the server
	 * 
	 * @return the newly created Project object.
	 * 
	 * @throws IOException
	 * @throws AuthenticationException
	 *             invalid or no API access key is used with the server, which
	 *             requires authorization. Check the constructor arguments.
	 * @throws RedmineException 
	 */
	public Project createProject(Project project) throws IOException,AuthenticationException, RedmineException {
		// see bug http://www.redmine.org/issues/7184
        URI uri = createURI("/projects.xml", new BasicNameValuePair("include", "trackers"));
        
		HttpPost httpPost = new HttpPost(uri);
		String createProjectXML = RedmineXMLGenerator.toXML(project);
//		System.out.println("create project:" + createProjectXML);
		setEntity(httpPost, createProjectXML);

		Response response = sendRequest(httpPost);
		Project createdProject = RedmineXMLParser.parseProjectFromXML(response.getBody());
		return createdProject;
	}

	/**
	 * 
	 * @param project
	 * @throws IOException
	 * @throws AuthenticationException
	 *             invalid or no API access key is used with the server, which
	 *             requires authorization. Check the constructor arguments.
	 * @throws RedmineException 
	 * @throws NotFoundException 
	 */
	public void updateProject(Project project) throws IOException,
			AuthenticationException, RedmineException, NotFoundException {
		updateObject(Project.class, project);
	}

	/**
	 * This number of objects (tasks, projects, users) will be requested from Redmine server in 1 request. 
	 */
	public int getObjectsPerPage() {
		return objectsPerPage;
	}

	// TODO add junit test
	/**
	 * This number of objects (tasks, projects, users) will be requested from Redmine server in 1 request. 
	 */
	public void setObjectsPerPage(int pageSize) {
		this.objectsPerPage = pageSize;
	}

	/**
	 * Load the list of users on the server.
	 * <p><b>This operation requires "Redmine Administrator" permission.</b>
	 * 
	 * @return list of User objects
	 * 
	 * @throws AuthenticationException
	 *             invalid or no API access key is used with the server, which
	 *             requires authorization. Check the constructor arguments.
	 * @throws NotFoundException 
	 * @throws RedmineException 
	 */
	public List<User> getUsers() throws IOException,AuthenticationException, NotFoundException, RedmineException{
		Map<String, NameValuePair> params = new HashMap<String, NameValuePair>();
		return getObjectsList(User.class, params);
	}

	public User getUserById(Integer userId) throws IOException, AuthenticationException, NotFoundException, RedmineException {
		return getObject(User.class, userId);
	}

	public User getCurrentUser() throws IOException, AuthenticationException, RedmineException{
        URI uri = createURI("/users/current.xml");
		HttpGet http = new HttpGet(uri);
		Response response = sendRequest(http);
		return RedmineXMLParser.parseUserFromXML(response.getBody());
	}

	public User createUser(User user) throws IOException,AuthenticationException, RedmineException, NotFoundException {
		return createObject(User.class, user);
	}
	
	/**
	 * This method cannot return the updated object from Redmine
	 * because the server does not provide any XML in response.
	 *
	 * @param user
	 * @throws IOException
	 * @throws AuthenticationException
	 *             invalid or no API access key is used with the server, which
	 *             requires authorization. Check the constructor arguments.
	 * @throws RedmineException 
	 * @throws NotFoundException some object is not found. e.g. the user with the given id
	 */
	public void updateUser(User user) throws IOException,
			AuthenticationException, RedmineException, NotFoundException {
		updateObject(User.class, user);
	}

	public List<TimeEntry> getTimeEntries() throws IOException,AuthenticationException, NotFoundException, RedmineException{
		Map<String, NameValuePair> params = new HashMap<String, NameValuePair>();
		return getObjectsList(TimeEntry.class, params);
	}
	
	/**
	 * @param id the database Id of the TimeEntry record
	 */
	public TimeEntry getTimeEntry(Integer id) throws IOException, AuthenticationException, NotFoundException, RedmineException {
		return getObject(TimeEntry.class, id);
	}

	public List<TimeEntry> getTimeEntriesForIssue(Integer issueId) throws IOException,AuthenticationException, NotFoundException, RedmineException{
		Map<String, NameValuePair> params = new HashMap<String, NameValuePair>();
		params.put("issue_id", new BasicNameValuePair("issue_id", Integer.toString(issueId)));

		return getObjectsList(TimeEntry.class, params);
	}
	
	public TimeEntry createTimeEntry(TimeEntry obj) throws IOException, AuthenticationException, NotFoundException, RedmineException {
		return createObject(TimeEntry.class, obj);
	}

	public void updateTimeEntry(TimeEntry obj) throws IOException, AuthenticationException, NotFoundException, RedmineException {
		updateObject(TimeEntry.class, obj);
	}

	public void deleteTimeEntry(Integer id) throws IOException, AuthenticationException, NotFoundException, RedmineException {
		deleteObject(TimeEntry.class, Integer.toString(id));
	}

}
