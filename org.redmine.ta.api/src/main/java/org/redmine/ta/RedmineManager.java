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
import java.net.URISyntaxException;
import java.net.URL;
import java.text.SimpleDateFormat;
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
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.redmine.ta.beans.Identifiable;
import org.redmine.ta.beans.Issue;
import org.redmine.ta.beans.Project;
import org.redmine.ta.beans.TimeEntry;
import org.redmine.ta.beans.User;
import org.redmine.ta.internal.HttpUtil;
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

	private String host;
	private String apiAccessKey;
	private int objectsPerPage = DEFAULT_OBJECTS_PER_PAGE;

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
	
	/**
	 * Creates an instance of RedmineManager class. Host and apiAccessKey are not checked at this moment.
	 * 
	 * @param host complete Redmine server web URI, including protocol and port number. Example: http://demo.redmine.org:8080
	 * @param apiAccessKey Redmine API access key. It is shown on "My Account" / "API access key" webpage 
	 *   (check  <i>http://redmine_server_url/my/account<i> URL).
	 *   This parameter is <b>optional</b> (can be set to NULL) for Redmine projects, which are "public". 
	 */
	public RedmineManager(String host, String apiAccessKey) {
		super();
		this.host = host;
		this.apiAccessKey = apiAccessKey;
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
        String query = getCreateIssueURI();
		HttpPost http = new HttpPost(query);
		String xmlBody = getIssueXML(projectKey, issue);
		
		setEntity(http, xmlBody);
		Response response = sendRequest(http);
		if (response.getCode() == HttpStatus.SC_NOT_FOUND) {
			throw new NotFoundException("Project with key '" + projectKey + "' is not found.");
		}
		Issue newIssue = RedmineXMLParser.parseIssueFromXML(response.getBody());
		return newIssue;
	}
	
	/**
	 * update_issue    = PUT  ${host}/issues/${id}.xml?key=${apiAccessKey}
	 */
	private String getUpdateIssueURI(Integer id) {
        return host + "/issues/" + id + ".xml?key=" + apiAccessKey;
	}

	/**
	 * create_issue    = POST ${host}/issues.xml?key=${apiAccessKey}
	 */
	private String getCreateIssueURI(){
        return host + "/issues.xml?key=" + apiAccessKey;
	}

	private String getURLProjectByKey(String key){
        return host + "/projects/" + key + ".xml?key=" +apiAccessKey;
	}
	
	/**
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
		/* note: This method cannot return the updated Issue from Redmine 
		 * because the server does not provide any XML in response.
		 */
		String query = getUpdateIssueURI(issue.getId());
		HttpPut httpRequest = new HttpPut(query);

		// XXX add "notes" xml node. see http://www.redmine.org/wiki/redmine/Rest_Issues
		String NO_PROJECT_KEY = null;
		String xmlBody = getIssueXML(NO_PROJECT_KEY, issue);

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
		System.out.println(request.getRequestLine());
//		DefaultHttpClient httpclient = new DefaultHttpClient();
//		wrapClient(httpclient);
		HttpClient httpclient = HttpUtil.getNewHttpClient();
		
		HttpResponse httpResponse = httpclient.execute((HttpUriRequest)request);
		
		// System.out.println("----------------------------------------");
		System.out.println(httpResponse.getStatusLine());
		int responseCode = httpResponse.getStatusLine().getStatusCode();
		if (responseCode ==	HttpStatus.SC_UNAUTHORIZED) {
			throw new AuthenticationException("Authorization error. Please check if you provided a valid API access key in the constructor arguments");
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
//		if (responseEntity != null) {
//			System.out.println("Response content length: "
//					+ responseEntity.getContentLength());
//		}
		// System.out.println(responseBody);
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
	
	static String REDMINE_START_DATE_FORMAT = "yyyy-MM-dd";
	static SimpleDateFormat sdf =       new SimpleDateFormat(REDMINE_START_DATE_FORMAT);

	// Can't use Castor here because this "post" format differs from "get" one.
	// see http://www.redmine.org/issues/6128#note-2 for details
	private String getIssueXML(String projectKey, Issue issue) {
		String xml = "<issue>";
		if (projectKey != null) {
			// projectKey is required for "new issue" request, but not for "update issue" one.
			xml += "<project_id>" + projectKey + "</project_id>";
		}
		if (issue.getParentId() != null) {
			xml += "<parent_issue_id>" + issue.getParentId() + "</parent_issue_id>";
		}
		if (issue.getSubject()!= null) {
			xml += "<subject>" + issue.getSubject() + "</subject>";
		}

		if (issue.getTracker() != null) {
			xml += "<tracker_id>" + issue.getTracker().getId() + "</tracker_id>";
		}

		if (issue.getStartDate() != null) {
			String strDate = sdf.format(issue.getStartDate());
			xml += "<start_date>" + strDate + "</start_date>";
		}
		
		if (issue.getDueDate() != null) {
			String strDate = sdf.format(issue.getDueDate());
			xml += "<due_date>" + strDate + "</due_date>";
		}

		User ass = issue.getAssignee();
		if (ass != null) {
			xml += "<assigned_to_id>" + ass.getId() + "</assigned_to_id>";
		}

		if (issue.getEstimatedHours() != null) {
			xml += "<estimated_hours>" + issue.getEstimatedHours() + "</estimated_hours>";
		}
		
		if (issue.getDescription() != null) {
			xml += "<description>" + issue.getDescription() + "</description>";
		}
		
		xml += "</issue>";
		return xml;
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
		try {
			return getObjectsListV104(Project.class, params);
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

		return getObjectsListV104(Issue.class, params);
	}
	
	/**
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
		return getObject(Issue.class, id);
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
        String query = getURLProjectByKey(projectKey);
		// see bug http://www.redmine.org/issues/7184
        query+="&include=trackers";
		HttpGet http = new HttpGet(query);
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

	// XXX this method will be used soon instead of temporary getIssuesV104()
/*	private List<Issue> getIssuesTrunk(String projectKey, String queryId) throws IOException, AuthenticationException {
		WebConnector c = new WebConnector();
		List<Issue>  allTasks = new ArrayList<Issue>();
		
		int offsetIssuesNum = 0;
		int totalIssuesFoundOnServer = RedmineXMLParser.UNKNOWN;
		int loaded = -1;
//		int pageNum=0;
		
		do {
			URL url = buildGetIssuesByQueryURL(projectKey, queryId, offsetIssuesNum);

			StringBuffer responseXML = c.loadData(url);

			totalIssuesFoundOnServer = RedmineXMLParser.parseIssuesTotalCount(responseXML
					.toString());
			
			List<Issue> foundIssues = RedmineXMLParser.parseIssuesFromXML(responseXML.toString());
			// assuming every page has the same number of items 
			loaded = foundIssues.size();
			allTasks.addAll(foundIssues);
			offsetIssuesNum+= loaded;
			// stop after 1st page if we don't know how many pages total (this required Redmine trunk version, it's not part of 1.0.4!)
			if (totalIssuesFoundOnServer == RedmineXMLParser.UNKNOWN) {
				totalIssuesFoundOnServer = loaded;
			}
		} while (offsetIssuesNum < totalIssuesFoundOnServer);

		return allTasks;
	}
*/

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

		List<Issue> issues = getObjectsListV104(Issue.class, params);
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
	 * @param params
	 */
	private void addPagingParameters(Map<String, NameValuePair> params) {
		params.put("per_page", new BasicNameValuePair("per_page", String.valueOf(objectsPerPage)));
	}
	
	private void addAuthParameters(Map<String, NameValuePair> params) {
		if ((apiAccessKey != null) && (apiAccessKey.length()>0)) {
			params.put("key", new BasicNameValuePair("key", apiAccessKey));
		}
	}
	
	private <T> List<T> getObjectsListV104(Class<T> objectClass, Map<String, NameValuePair> params) throws IOException, AuthenticationException, NotFoundException, RedmineException {
		List<T>  objects = new ArrayList<T>();
		
		final int FIRST_REDMINE_PAGE = 1;
		int pageNum = FIRST_REDMINE_PAGE;
		// Redmine 1.0.4 (and Trunk at this moment - Dec 22, 2010) returns the same page1 when no other pages are available!!
		String firstPage=null;
		
		addPagingParameters(params);
		addAuthParameters(params);
		
		do {
			params.put("page", new BasicNameValuePair("page", String.valueOf(pageNum)));
			List<NameValuePair> paramsList = new ArrayList<NameValuePair>(params.values());
			
			URI uri;
			try {
				String query = urls.get(objectClass) + URL_POSTFIX;
				uri = getURI(query, paramsList);
			} catch (MalformedURLException e) {
				throw new RuntimeException("MalformedURLException: " + e.getMessage());
			}

			HttpGet http = new HttpGet(uri);
// 			commented out because this only works in Redmine 1.1 !!			
//			totalIssuesFoundOnServer = RedmineXMLParser.parseIssuesTotalCount(responseXML
//					.toString());

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
				// and this is to provide compatibility with Redmine 1.1.0
				break;
			}
			objects.addAll(foundItems);

			pageNum++;
		} while (true);

		return objects;
	}

	private <T> T getObject(Class<T> objectClass, Integer id) throws IOException, AuthenticationException, NotFoundException, RedmineException {
		Map<String, NameValuePair> params = new HashMap<String, NameValuePair>();
		String url = urls.get(objectClass) + "/" + id + URL_POSTFIX;
		String body = sendGet(url, params);
		return RedmineXMLParser.parseObjectFromXML(objectClass, body);
	}

	// TODO is there a way to get rid of the 1st parameter and use generics?
	private <T> T createObject(Class<T> classs, T obj) throws IOException, AuthenticationException, NotFoundException, RedmineException {
		Map<String, NameValuePair> params = new HashMap<String, NameValuePair>();
		addAuthParameters(params);
		List<NameValuePair> paramsList = new ArrayList<NameValuePair>(params.values());

		URI uri = getCreateURI(obj.getClass(), paramsList);
		HttpPost http = new HttpPost(uri);
		
		String xml = RedmineXMLParser.convertObjectToXML(obj);
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
		Map<String, NameValuePair> params = new HashMap<String, NameValuePair>();
		addAuthParameters(params);
		List<NameValuePair> paramsList = new ArrayList<NameValuePair>(params.values());

		URI uri = getUpdateURI(obj.getClass(), Integer.toString(obj.getId()), paramsList);
		HttpPut http = new HttpPut(uri);
		
		String xml = RedmineXMLParser.convertObjectToXML(obj);
		setEntity((HttpEntityEnclosingRequest)http, xml);

		Response response = sendRequest(http);
		if (response.getCode() ==	HttpStatus.SC_NOT_FOUND) {
			throw new NotFoundException("Server returned '404 not found'. response body:" + response.getBody());
		}
	}
	
	private <T extends Identifiable> void deleteObject(Class<T> classs, String id) throws IOException, AuthenticationException, NotFoundException, RedmineException {
		Map<String, NameValuePair> params = new HashMap<String, NameValuePair>();
		addAuthParameters(params);
		List<NameValuePair> paramsList = new ArrayList<NameValuePair>(params.values());

		URI uri = getUpdateURI(classs, id, paramsList);
		HttpDelete http = new HttpDelete(uri);
		
//		String xml = RedmineXMLParser.convertObjectToXML(obj);
//		setEntity((HttpEntityEnclosingRequest)http, xml);

		Response response = sendRequest(http);
		if (response.getCode() ==	HttpStatus.SC_NOT_FOUND) {
			throw new NotFoundException("Server returned '404 not found'. response body:" + response.getBody());
		}
	}

	private URI getCreateURI(Class zz, List<NameValuePair> paramsList) throws MalformedURLException {
		String url =urls.get(zz) + URL_POSTFIX; 
		return getURI(url, paramsList);
	}

	private URI getUpdateURI(Class zz, String id, List<NameValuePair> paramsList) throws MalformedURLException {
		String url = urls.get(zz) + "/" + id + URL_POSTFIX;
		return getURI(url, paramsList);
	}
	
	private URI getURI(String url, List<NameValuePair> paramsList) throws MalformedURLException {
		URI uri = null;

		try {
			String path = url;
			URL aURL = new URL(host);
			if (aURL.getPath().length()>0){
				path = aURL.getPath() + "/" + path;
			}
			uri = URIUtils.createURI(aURL.getProtocol(), aURL.getHost(), aURL.getPort(), path, 
				    URLEncodedUtils.format(paramsList, CHARSET), null);
		} catch (URISyntaxException e) {
			throw new RuntimeException("URISyntaxException: " + e.getMessage());
		}
		return uri;
	}
	
	private String sendGet(String url, Map<String, NameValuePair> params) throws NotFoundException, IOException, AuthenticationException, RedmineException {
		addAuthParameters(params);
		List<NameValuePair> paramsList = new ArrayList<NameValuePair>(params.values());
		URI uri;
		try {
			uri = getURI(url, paramsList);
		} catch (MalformedURLException e) {
			throw new RuntimeException("MalformedURLException:" + e.getMessage());
		}

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
        String query = buildCreateProjectURI();
		// see bug http://www.redmine.org/issues/7184
        query+="&include=trackers";
        
		HttpPost httpPost = new HttpPost(query);
		String createProjectXML = RedmineXMLParser.convertObjectToXML(project);
//		System.out.println("create project:" + createProjectXML);
		setEntity(httpPost, createProjectXML);

		Response response = sendRequest(httpPost);
		Project createdProject = RedmineXMLParser.parseProjectFromXML(response.getBody());
		return createdProject;
	}

	private String buildCreateProjectURI() {
		return host + "/projects.xml?key=" + apiAccessKey;
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
		return getObjectsListV104(User.class, params);
	}

	public User getUserById(Integer userId) throws IOException, AuthenticationException, NotFoundException, RedmineException {
		return getObject(User.class, userId);
	}

	public User getCurrentUser() throws IOException, AuthenticationException, RedmineException{
        String query = getURLCurrentUser();
		HttpGet http = new HttpGet(query);
		Response response = sendRequest(http);
		return RedmineXMLParser.parseUserFromXML(response.getBody());
	}

	private String getURLCurrentUser() {
		return host + "/users/current.xml?key=" +apiAccessKey;
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
		return getObjectsListV104(TimeEntry.class, params);
	}
	
	/**
	 * 
	 * @param id the database Id of the TimeEntry record
	 * @return
	 * @throws IOException
	 * @throws AuthenticationException
	 * @throws NotFoundException
	 * @throws RedmineException
	 */
	public TimeEntry getTimeEntry(Integer id) throws IOException, AuthenticationException, NotFoundException, RedmineException {
		return getObject(TimeEntry.class, id);
	}

	public List<TimeEntry> getTimeEntriesForIssue(Integer issueId) throws IOException,AuthenticationException, NotFoundException, RedmineException{
		Map<String, NameValuePair> params = new HashMap<String, NameValuePair>();
		params.put("issue_id", new BasicNameValuePair("issue_id", Integer.toString(issueId)));

		return getObjectsListV104(TimeEntry.class, params);
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
