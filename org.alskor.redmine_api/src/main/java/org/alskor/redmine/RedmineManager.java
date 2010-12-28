package org.alskor.redmine;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.alskor.httputils.AuthenticationException;
import org.alskor.httputils.NotFoundException;
import org.alskor.httputils.WebConnector;
import org.alskor.redmine.beans.Issue;
import org.alskor.redmine.beans.Project;
import org.alskor.redmine.beans.User;
import org.alskor.redmine.internal.RedmineXMLParser;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.exolab.castor.mapping.Mapping;
import org.exolab.castor.xml.Marshaller;
import org.xml.sax.InputSource;

import com.alskor.taskadapter.license.License;
import com.alskor.taskadapter.license.LicenseManager;
import com.alskor.taskadapter.license.LicenseValidationException;


/**
 * <b>Entry point</b> for the API: use this class to communicate with Redmine servers.
 * 
 * @author Alexey Skorokhodov
 */
public class RedmineManager {
	private static final String CONTENT_TYPE = "text/xml; charset=utf-8";

	private static final int DEFAULT_TASKS_PER_PAGE = 25;

	private static final String LICENSE_ERROR_MESSAGE = "Redmine Java API: license is not found. Working in ----TRIAL---- mode."
		+ "\nPlease buy a license on " + LicenseManager.PRODUCT_WEBSITE_URL;

	private String host;
	private String apiAccessKey;
	private int tasksPerPage = DEFAULT_TASKS_PER_PAGE;

//	private static enum REDMINE_VERSION {
//		V104, TRUNK
//	}
	
//	private REDMINE_VERSION mode = REDMINE_VERSION.V104;
	
	private static boolean trialMode = true;
	
	static {
		trialMode = true;
		
		License licenseRedmineAPI = null;
		try {
			licenseRedmineAPI = LicenseManager.getRedmineApiLicense();
		} catch (LicenseValidationException e) {
			// ignore, do not show the stacktrace!
		}
		License licenseTaskAdapter = null;
		try {
			licenseTaskAdapter = LicenseManager.getTaskAdapterLicense();
		} catch (LicenseValidationException e) {
			// ignore, do not show the stacktrace!
		}

		if (licenseRedmineAPI != null) { 
			System.out.println("Redmine Java API: loaded valid license. Registered to: " + licenseRedmineAPI);
			trialMode = false;
		} 
		if (trialMode && (licenseTaskAdapter != null)) {
			System.out.println("Redmine Java API: will use Task Adapter's license: " + licenseTaskAdapter);
			trialMode = false;
		}
		
		// still trial mode?
		if (trialMode) {
			System.err.println(LICENSE_ERROR_MESSAGE);
		}
	}

	/**
	 * @param host complete URI, including protocol and port number. sample: http://demo.redmine.org:8080
	 * @param apiAccessKey Redmine API authentication key.
	 */
	public RedmineManager(String host, String apiAccessKey) {
		super();
		this.host = host;
		this.apiAccessKey = apiAccessKey;
	}

	public Issue createIssue(String projectKey, Issue issue) throws IOException,AuthenticationException, NotFoundException {
        String query = getCreateIssueURI();
		HttpPost http = new HttpPost(query);
		String xmlBody = getIssueXML(projectKey, issue);
		setEntity(http, xmlBody);
		HttpResponse response = sendRequestInternal(http);
		int code = getCode(response);
		if (code == HttpStatus.SC_NOT_FOUND) {
			throw new NotFoundException("Project with key '" + projectKey + "' is not found.");
		}
		String body = getBody(response);
		Issue newIssue = RedmineXMLParser.parseIssueFromXML(body);
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

	/**
	 * <p>HTTP request:
	 * <pre>GET /issues/[id].xml</pre>
	 */
	private String getURLIssueById(Integer id){
        return host + "/issues/" + id + ".xml?key=" +apiAccessKey;
	}

	private String getURLProjectByKey(String key){
        return host + "/projects/" + key + ".xml?key=" +apiAccessKey;
	}
	
	public void updateIssue(Issue issue) throws IOException,AuthenticationException, NotFoundException {
		/* note: This method cannot return the updated Issue from Redmine 
		 * because the server does not provide any XML in response.
		 */
		String query = getUpdateIssueURI(issue.getId());
		HttpPut httpRequest = new HttpPut(query);

		// XXX add "notes" xml node. see http://www.redmine.org/wiki/redmine/Rest_Issues
		String NO_PROJECT_KEY = null;
		String xmlBody = getIssueXML(NO_PROJECT_KEY, issue);

		setEntity(httpRequest, xmlBody);
		HttpResponse response = sendRequestInternal(httpRequest);
		int code = getCode(response);
		if (code == HttpStatus.SC_NOT_FOUND) {
			throw new NotFoundException("Issue with id="+ issue.getId() + " is not found.");
		}
	}
	
	private void setEntity(HttpEntityEnclosingRequest request, String xmlBody) throws UnsupportedEncodingException {
		StringEntity entity = new StringEntity(xmlBody, "UTF-8");
		entity.setContentType(CONTENT_TYPE);
		request.setEntity(entity);
	}
	
	// TODO maybe add NotFoundException to this method itself rather than to calling methods?
	// this aproach has pros and cons...
	private HttpResponse sendRequestInternal(HttpRequest request) throws ClientProtocolException, IOException, AuthenticationException {
//		System.out.println(request.getRequestLine());
		DefaultHttpClient httpclient = new DefaultHttpClient();
		wrapClient(httpclient);
		
		HttpResponse response = httpclient.execute((HttpUriRequest)request);
//		HttpEntity responseEntity = response.getEntity();
		
		// System.out.println("----------------------------------------");
		System.out.println(response.getStatusLine());
		int responseCode = response.getStatusLine().getStatusCode();
		if (responseCode ==	HttpStatus.SC_UNAUTHORIZED) {
			// TODO should I show this key in the clear text?
			throw new AuthenticationException("Authorization error for API access key = " + apiAccessKey);
		}
		
//		if (responseEntity != null) {
//			System.out.println("Response content length: "
//					+ responseEntity.getContentLength());
//		}
		// System.out.println(responseBody);
		httpclient.getConnectionManager().shutdown();
//		String responseBody = EntityUtils.toString(responseEntity);
		return response;
	}

	static String REDMINE_START_DATE_FORMAT = "yyyy-MM-dd";
	static SimpleDateFormat sdf =       new SimpleDateFormat(REDMINE_START_DATE_FORMAT);

	private static int getCode(HttpResponse response) {
		return response.getStatusLine().getStatusCode();
	}
	
	private static String getBody(HttpResponse response) throws ParseException, IOException {
		HttpEntity responseEntity = response.getEntity();
		String responseBody = EntityUtils.toString(responseEntity);
		return responseBody;
	}
	  
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
		xml += "<subject>" + issue.getSubject() + "</subject>";

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
	 * Get available projects list.
	 * 
	 * @return list of Project objects
	 */
	public List<Project> getProjects() throws IOException,AuthenticationException{
		URL url = buildGetProjectsURL();
		WebConnector c = new WebConnector();
		StringBuffer response = c.loadData(url);
		return RedmineXMLParser.parseProjectsFromXML(response.toString());
	}
	
	/**
	 * sample: http://demo.redmine.org/projects.xml?key=abc
	 */
	private URL buildGetProjectsURL() {
		String query = host + "/projects.xml";
		if (apiAccessKey != null) {
			query += "?key=" + apiAccessKey;
		}
		try {
			return new URL(query);
		} catch (MalformedURLException e) {
			throw new RuntimeException("can't create URL for this string: '" + query + "'. Reason: " +e);
		}
	}

	public List<Issue> createIssues(String projectKey, List<Issue> tasks) {
		List<Issue> createdIssues = new ArrayList<Issue>();
		Iterator<Issue> it = tasks.iterator();
		while (it.hasNext()) {
			Issue issueToCreate = it.next();
			try {
				Issue newIssue = createIssue(projectKey, issueToCreate);
				createdIssues.add(newIssue);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		return createdIssues;
	}
	
	/**
	 * There could be several issues with the same summary, so the method returns List. 
	 * 
	 * @param summaryField
	 * @return empty list if not issues with this summary field exist, never NULL
	 */
	public List<Issue> getIssuesBySummary(String projectKey, String summaryField) throws NoSuchAlgorithmException, IOException, AuthenticationException {
		URL url = getQueryIssueBySummaryURL(projectKey, summaryField);
		
		WebConnector c = new WebConnector();
		StringBuffer response = c.loadData(url);

		List<Issue> foundIssues = RedmineXMLParser.parseIssuesFromXML(response.toString());
		return foundIssues;
	}
	
	private URL getQueryIssueBySummaryURL(String projectKey, String issueSummary) {
		String charset = "UTF-8";
		String query;
		try {
			query = String.format("/projects/%s/issues.xml?subject=%s", 
				     URLEncoder.encode(projectKey, charset),
				     URLEncoder.encode(issueSummary, charset));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
		
		if ((apiAccessKey != null) && (!apiAccessKey.isEmpty())) {
			try {
				query += String.format("&key=%s", 
					     URLEncoder.encode(apiAccessKey, charset) );
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException(e);
			}
		}
		URL url;
		try {
			url = new URL(host + query);
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
		
		return url;
	}

	public Issue getIssueById(Integer id) throws IOException, AuthenticationException, NotFoundException {
        String query = getURLIssueById(id);
		HttpGet http = new HttpGet(query);
		HttpResponse response = sendRequestInternal(http);
		int code = getCode(response);
		if (code == HttpStatus.SC_NOT_FOUND) {
			throw new NotFoundException("Issue with id '" + id + "' is not found.");
		}
		String body = getBody(response);
		Issue issue = RedmineXMLParser.parseIssueFromXML(body);
		return issue;
	}
	
	/**
	 * 
	 * @param projectKey string key like "project-ABC", NOT a database numeric ID
	 * @return Redmine's project
	 */
	public Project getProjectByIdentifier(String projectKey) throws IOException, AuthenticationException, NotFoundException {
        String query = getURLProjectByKey(projectKey);
		HttpGet http = new HttpGet(query);
		HttpResponse response = sendRequestInternal(http);
		int code = getCode(response);
		if (code == HttpStatus.SC_NOT_FOUND) {
			throw new NotFoundException("Project with key '" + projectKey + "' is not found.");
		}
		String body = getBody(response); 
		Project projectFromServer = RedmineXMLParser.parseProjectFromXML(body);
		return projectFromServer;
	}

	/**
	 * @param projectKey string key like "project-ABC", NOT a database numeric ID
	 * 
	 * @throws NotFoundException if the project with the given key is not found 
	 */
	public void deleteProject(String projectKey) throws IOException, AuthenticationException, NotFoundException {
        String query = getURLProjectByKey(projectKey);
        HttpDelete http = new HttpDelete(query);
		HttpResponse response = sendRequestInternal(http);
		int code = getCode(response);
		if (code == HttpStatus.SC_NOT_FOUND) {
			throw new NotFoundException("Project with key '" + projectKey + "' is not found.");
		}
	}
	
	// XXX this duplicates code in SSLSomething class.
	private static HttpClient wrapClient(HttpClient base) {
		try {
			SSLContext ctx = SSLContext.getInstance("TLS");
			X509TrustManager tm = new X509TrustManager() {

				public void checkClientTrusted(X509Certificate[] xcs,
						String string) throws CertificateException {
				}

				public void checkServerTrusted(X509Certificate[] xcs,
						String string) throws CertificateException {
				}

				public X509Certificate[] getAcceptedIssuers() {
					return null;
				}
			};
			ctx.init(null, new TrustManager[] { tm }, null);
			SSLSocketFactory ssf = new SSLSocketFactory(ctx);
			ssf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
			ClientConnectionManager ccm = base.getConnectionManager();
			SchemeRegistry sr = ccm.getSchemeRegistry();
			// XXX support other ports, not just 443?
			sr.register(new Scheme("https", ssf, 443));
			return new DefaultHttpClient(ccm, base.getParams());
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	public List<Issue> getIssues(String projectKey, String queryId) throws IOException, AuthenticationException {
//		if (mode.equals(REDMINE_VERSION.TRUNK)) {
//			return getIssuesTrunk(projectKey, queryId);
//		} else if (mode.equals(REDMINE_VERSION.V104)) {
		List<Issue> issues = getIssuesV104(projectKey, queryId);
		
		if (trialMode) {
			List<Issue> trialList = new ArrayList<Issue>(issues.size()); 
			for (int i=0; i<LicenseManager.TRIAL_TASKS_NUMBER_LIMIT; i++) {
				trialList.add(issues.get(i));
			}
			issues = trialList;
		}

		return issues; 
//		}
//		throw new RuntimeException("unsupported mode: " + mode);
	}
	
	// XXX this method will be used soon instead of temporary getIssuesV104()
	private List<Issue> getIssuesTrunk(String projectKey, String queryId) throws IOException, AuthenticationException {
		WebConnector c = new WebConnector();
		List<Issue>  allTasks = new ArrayList<Issue>();
		
		int offsetIssuesNum = 0;
		int totalIssuesFoundOnServer = RedmineXMLParser.UNKNOWN;
		int loaded = -1;
//		int pageNum=0;
		
		do {
			URL url = buildGetIssuesByQueryURL(projectKey, queryId, offsetIssuesNum);

			StringBuffer responseXML = c.loadData(url);
//			System.err.println("RedmineManager: "  + responseXML);

			totalIssuesFoundOnServer = RedmineXMLParser.parseIssuesTotalCount(responseXML
					.toString());
			
			List<Issue> foundIssues = RedmineXMLParser.parseIssuesFromXML(responseXML.toString());
			// assuming every page has the same number of items 
			loaded = foundIssues.size();
//			System.err.println("totalIssuesFoundOnServer="
//					+ totalIssuesFoundOnServer + " loaded = "
//					+ loaded);
			allTasks.addAll(foundIssues);
			offsetIssuesNum+= loaded;
			// stop after 1st page if we don't know how many pages total (this required Redmine trunk version, it's not part of 1.0.4!)
			if (totalIssuesFoundOnServer == RedmineXMLParser.UNKNOWN) {
				totalIssuesFoundOnServer = loaded;
			}
		} while (offsetIssuesNum < totalIssuesFoundOnServer);

		return allTasks;
	}

	// IMPORTANT!! this method works with both Redmine 1.0.4 and Redmine/TRUNK versions (Dec 22, 2010)
	private List<Issue> getIssuesV104(String projectKey, String queryId) throws IOException, AuthenticationException {
//		System.out.println("using redmine 1.0.4 compatibility mode");
		WebConnector c = new WebConnector();
		List<Issue>  allTasks = new ArrayList<Issue>();
		
//		int loaded = -1;
		final int FIRST_REDMINE_PAGE = 1;
		int pageNum = FIRST_REDMINE_PAGE;
//		int loadedOnPreviousStep;
//		boolean readMore = false;
		// Redmine 1.0.4 (and Trunk at this moment - Dec 22, 2010) returns the same page1 when no other pages are available!!
		StringBuffer firstPage=null;
		
		do {
//			loadedOnPreviousStep = loaded;
			URL url = buildGetIssuesByQueryURLRedmine104(projectKey, queryId,
					pageNum);

			StringBuffer responseXML = c.loadData(url);
			String responseXmlString = responseXML.toString();
//			System.err.println(responseXmlString);
			if (pageNum == FIRST_REDMINE_PAGE) {
				firstPage = responseXML;
			} else {
				// check that the response is NOT equal to the First Page
				// - this would indicate that no more pages are available (for Redmine 1.0.*);
				if (firstPage.toString().equals(responseXmlString)) {
					// done, no more pages. exit the loop
					break;
				}
			}
			List<Issue> foundIssues = RedmineXMLParser.parseIssuesFromXML(responseXmlString);
			if (foundIssues.size() == 0) {
				// and this is to provide compatibility with Redmine 1.1.0
				break;
			}
			// assuming every page has the same number of items 
//			loaded = foundIssues.size();
			allTasks.addAll(foundIssues);

//			readMore = false;
//			if ((pageNum == 1) && loaded==tasksPerPage) {
//				readMore = true;
//			} else if (loaded ==loadedOnPreviousStep){
//				readMore = true;
//			}
			pageNum++;
		} while (true);

		return allTasks;
	}

	/**
	 * sample: http://demo.redmine.org/projects/ace/issues.xml?query_id=302
	 */
	private URL buildGetIssuesByQueryURL(String projectKey, String queryId, int offsetIssuesNum) {
		String charset = "UTF-8";
		URL url = null;
		try {
			String query = String.format("/issues.xml?project_id=%s&query_id=%s",
					URLEncoder.encode(projectKey, charset),
					URLEncoder.encode(queryId, charset));
			query += "&offset=" + offsetIssuesNum;
			query += "&limit=" + tasksPerPage;
			if ((apiAccessKey != null) && (!apiAccessKey.isEmpty())) {
				query += String.format("&key=%s",
						URLEncoder.encode(apiAccessKey, charset));
			}
			url = new URL(host + query);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return url;

	}

	private URL buildGetIssuesByQueryURLRedmine104(String projectKey, String queryId, int pageNum) {
		String charset = "UTF-8";
		URL url = null;
		try {
			String query = "/issues.xml";
			query += "?page=" + pageNum;
			query += "&per_page=" + tasksPerPage;
			if ((projectKey != null) && (!projectKey.isEmpty())) {
				query += String.format("&project_id=%s",
						URLEncoder.encode(projectKey, charset));
			}
			if ((queryId != null) && (!queryId.isEmpty())) {
				query += String.format("&query_id=%s",
						URLEncoder.encode(queryId, charset));
			}
			if ((apiAccessKey != null) && (!apiAccessKey.isEmpty())) {
				query += String.format("&key=%s",
						URLEncoder.encode(apiAccessKey, charset));
			}
			url = new URL(host + query);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return url;

	}

	public Project createProject(Project project) throws IOException,AuthenticationException {
        String query = buildCreateProjectURI();
		HttpPost httpPost = new HttpPost(query);
		String createProjectXML = convertToXML(project);
//		System.out.println("create project:" + createProjectXML);
		setEntity(httpPost, createProjectXML);

		HttpResponse response = sendRequestInternal(httpPost);
		String body = getBody(response);
		Project createdProject = RedmineXMLParser.parseProjectFromXML(body);
		return createdProject;
	}

	private String convertToXML(Project project) {
		StringWriter writer = new StringWriter();
		try {
			Marshaller m = getMarshaller(RedmineXMLParser.MAPPING_PROJECTS_LIST, writer);
			m.marshal(project);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return writer.toString();
	}

	private String buildCreateProjectURI() {
		return host + "/projects.xml?key=" + apiAccessKey;
	}

	private String buildUpdateProjectURI(String key) {
		return host + "/projects/" + key +".xml?key=" + apiAccessKey;
	}

	private static Marshaller getMarshaller(String configFile, Writer writer) {
		InputSource inputSource = new InputSource(
				RedmineManager.class.getResourceAsStream(configFile));
		Mapping mapping = new Mapping();
		mapping.loadMapping(inputSource);

		Marshaller marshaller;
		try {
			marshaller = new Marshaller(writer);
			marshaller.setMapping(mapping);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return marshaller;
	}

	public void updateProject(Project project) throws IOException,
			AuthenticationException {
		/*
		 * note: This method cannot return the updated object from Redmine
		 * because the server does not provide any XML in response.
		 */
		String query = buildUpdateProjectURI(project.getIdentifier());
		HttpPut httpRequest = new HttpPut(query);
		String projectXML = convertToXML(project);
		setEntity(httpRequest, projectXML);
		sendRequestInternal(httpRequest);

	}

	/**
	 * This number of tasks will be requested from Redmine server in getIssues() call. 
	 */
	public int getTasksPerPage() {
		return tasksPerPage;
	}

	// TODO add junit test
	/**
	 * This number of tasks will be requested from Redmine server in getIssues() call. 
	 */
	public void setTasksPerPage(int tasksPerPage) {
		this.tasksPerPage = tasksPerPage;
	}
	
/*	public void setRedmineVersion(REDMINE_VERSION v){
		mode = v;
	}
*/
}
