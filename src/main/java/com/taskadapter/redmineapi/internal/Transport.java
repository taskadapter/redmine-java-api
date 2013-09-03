package com.taskadapter.redmineapi.internal;

import java.io.InputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.taskadapter.redmineapi.NotFoundException;
import com.taskadapter.redmineapi.RedmineAuthenticationException;
import com.taskadapter.redmineapi.RedmineException;
import com.taskadapter.redmineapi.RedmineFormatException;
import com.taskadapter.redmineapi.RedmineInternalError;
import com.taskadapter.redmineapi.RedmineManager;
import com.taskadapter.redmineapi.RedmineOptions;
import com.taskadapter.redmineapi.bean.Group;
import com.taskadapter.redmineapi.bean.Identifiable;
import com.taskadapter.redmineapi.bean.Issue;
import com.taskadapter.redmineapi.bean.IssueCategory;
import com.taskadapter.redmineapi.bean.IssuePriority;
import com.taskadapter.redmineapi.bean.IssueRelation;
import com.taskadapter.redmineapi.bean.IssueStatus;
import com.taskadapter.redmineapi.bean.Membership;
import com.taskadapter.redmineapi.bean.News;
import com.taskadapter.redmineapi.bean.Project;
import com.taskadapter.redmineapi.bean.Role;
import com.taskadapter.redmineapi.bean.SavedQuery;
import com.taskadapter.redmineapi.bean.TimeEntry;
import com.taskadapter.redmineapi.bean.TimeEntryActivity;
import com.taskadapter.redmineapi.bean.Tracker;
import com.taskadapter.redmineapi.bean.User;
import com.taskadapter.redmineapi.bean.Version;
import com.taskadapter.redmineapi.bean.Watcher;
import com.taskadapter.redmineapi.internal.comm.BaseCommunicator;
import com.taskadapter.redmineapi.internal.comm.BasicHttpResponse;
import com.taskadapter.redmineapi.internal.comm.Communicator;
import com.taskadapter.redmineapi.internal.comm.Communicators;
import com.taskadapter.redmineapi.internal.comm.ContentHandler;
import com.taskadapter.redmineapi.internal.comm.SimpleCommunicator;
import com.taskadapter.redmineapi.internal.comm.redmine.RedmineAuthenticator;
import com.taskadapter.redmineapi.internal.comm.redmine.RedmineErrorHandler;
import com.taskadapter.redmineapi.internal.json.JsonInput;
import com.taskadapter.redmineapi.internal.json.JsonObjectParser;
import com.taskadapter.redmineapi.internal.json.JsonObjectWriter;

public final class Transport {
	private static final Map<Class<?>, EntityConfig<?>> OBJECT_CONFIGS = new HashMap<Class<?>, EntityConfig<?>>();
	private static final String CONTENT_TYPE = "application/json; charset=utf-8";
	private static final int DEFAULT_OBJECTS_PER_PAGE = 25;
	private static final String KEY_TOTAL_COUNT = "total_count";
	private final Logger logger = LoggerFactory.getLogger(RedmineManager.class);
	private final SimpleCommunicator<String> communicator;
	private final Communicator<BasicHttpResponse> errorCheckingCommunicator;
	private final BaseCommunicator baseCommunicator;
	private final RedmineAuthenticator<HttpResponse> authenticator;

  static {
		OBJECT_CONFIGS.put(
				Project.class,
				config("project", "projects",
						RedmineJSONBuilder.PROJECT_WRITER,
						RedmineJSONParser.PROJECT_PARSER));
		OBJECT_CONFIGS.put(
				Issue.class,
				config("issue", "issues", RedmineJSONBuilder.ISSUE_WRITER,
						RedmineJSONParser.ISSUE_PARSER));
		OBJECT_CONFIGS.put(
				User.class,
				config("user", "users", RedmineJSONBuilder.USER_WRITER,
						RedmineJSONParser.USER_PARSER));
		OBJECT_CONFIGS.put(
				Group.class,
				config("group", "groups", RedmineJSONBuilder.GROUP_WRITER,
						RedmineJSONParser.GROUP_PARSER));
		OBJECT_CONFIGS.put(
				IssueCategory.class,
				config("issue_category", "issue_categories",
						RedmineJSONBuilder.CATEGORY_WRITER,
						RedmineJSONParser.CATEGORY_PARSER));
		OBJECT_CONFIGS.put(
				Version.class,
				config("version", "versions",
						RedmineJSONBuilder.VERSION_WRITER,
						RedmineJSONParser.VERSION_PARSER));
		OBJECT_CONFIGS.put(
				TimeEntry.class,
				config("time_entry", "time_entries",
						RedmineJSONBuilder.TIME_ENTRY_WRITER,
						RedmineJSONParser.TIME_ENTRY_PARSER));
		OBJECT_CONFIGS.put(News.class,
				config("news", "news", null, RedmineJSONParser.NEWS_PARSER));
		OBJECT_CONFIGS.put(
				IssueRelation.class,
				config("relation", "relations",
						RedmineJSONBuilder.RELATION_WRITER,
						RedmineJSONParser.RELATION_PARSER));
		OBJECT_CONFIGS.put(
				Tracker.class,
				config("tracker", "trackers", null,
						RedmineJSONParser.TRACKER_PARSER));
		OBJECT_CONFIGS.put(
				IssueStatus.class,
				config("status", "issue_statuses", null,
						RedmineJSONParser.STATUS_PARSER));
		OBJECT_CONFIGS
				.put(SavedQuery.class,
						config("query", "queries", null,
								RedmineJSONParser.QUERY_PARSER));
		OBJECT_CONFIGS.put(Role.class,
				config("role", "roles", null, RedmineJSONParser.ROLE_PARSER));
		OBJECT_CONFIGS.put(
				Membership.class,
				config("membership", "memberships",
						RedmineJSONBuilder.MEMBERSHIP_WRITER,
						RedmineJSONParser.MEMBERSHIP_PARSER));
        OBJECT_CONFIGS.put(
                IssuePriority.class,
                config("issue_priority", "issue_priorities", null,
                        RedmineJSONParser.ISSUE_PRIORITY_PARSER));
        OBJECT_CONFIGS.put(
                TimeEntryActivity.class,
                config("time_entry_activity", "time_entry_activities", null,
                        RedmineJSONParser.TIME_ENTRY_ACTIVITY_PARSER));

        OBJECT_CONFIGS.put(
                Watcher.class,
                config("watcher", "watchers", null,
                        RedmineJSONParser.WATCHER_PARSER));
	}

	private final URIConfigurator configurator;
	private String login;
	private String password;
	private int objectsPerPage = DEFAULT_OBJECTS_PER_PAGE;
	private static final String CHARSET = "UTF-8";

	public Transport(URIConfigurator configurator, RedmineOptions options) {
		this.configurator = configurator;
		this.baseCommunicator = new BaseCommunicator(options);
		this.authenticator = new RedmineAuthenticator<HttpResponse>(
				baseCommunicator, CHARSET);
		final ContentHandler<BasicHttpResponse, BasicHttpResponse> errorProcessor = new RedmineErrorHandler();
		errorCheckingCommunicator = Communicators.fmap(
				authenticator,
				Communicators.compose(errorProcessor,
						Communicators.transportDecoder()));
    Communicator<String> coreCommunicator = Communicators.fmap(errorCheckingCommunicator,
            Communicators.contentReader());
		this.communicator = Communicators.simplify(coreCommunicator,
                Communicators.<String>identityHandler());
	}

	public User getCurrentUser(NameValuePair... params) throws RedmineException {
		URI uri = getURIConfigurator().createURI("users/current.json", params);
		HttpGet http = new HttpGet(uri);
		String response = getCommunicator().sendRequest(http);
		return parseResponse(response, "user", RedmineJSONParser.USER_PARSER);
	}

	/**
	 * Performs an "add object" request.
	 * 
	 * @param object
	 *            object to use.
	 * @param params
	 *            name params.
	 * @return object to use.
	 * @throws RedmineException
	 *             if something goes wrong.
	 */
	public <T> T addObject(T object, NameValuePair... params)
			throws RedmineException {
		final EntityConfig<T> config = getConfig(object.getClass());
		URI uri = getURIConfigurator().getObjectsURI(object.getClass(), params);
		HttpPost httpPost = new HttpPost(uri);
		String body = RedmineJSONBuilder.toSimpleJSON(config.singleObjectName,
				object, config.writer);
		setEntity(httpPost, body);
		String response = getCommunicator().sendRequest(httpPost);
		logger.debug(response);
		return parseResponse(response, config.singleObjectName, config.parser);
	}

	/**
	 * Performs an "add child object" request.
	 * 
	 * @param parentClass
	 *            parent object id.
	 * @param object
	 *            object to use.
	 * @param params
	 *            name params.
	 * @return object to use.
	 * @throws RedmineException
	 *             if something goes wrong.
	 */
	public <T> T addChildEntry(Class<?> parentClass, String parentId, T object,
			NameValuePair... params) throws RedmineException {
		final EntityConfig<T> config = getConfig(object.getClass());
		URI uri = getURIConfigurator().getChildObjectsURI(parentClass,
				parentId, object.getClass(), params);
		HttpPost httpPost = new HttpPost(uri);
		String body = RedmineJSONBuilder.toSimpleJSON(config.singleObjectName,
				object, config.writer);
		setEntity(httpPost, body);
		String response = getCommunicator().sendRequest(httpPost);
		logger.debug(response);
		return parseResponse(response, config.singleObjectName, config.parser);
	}

	/*
	 * note: This method cannot return the updated object from Redmine because
	 * the server does not provide any XML in response.
	 * 
	 * @since 1.8.0
	 */
	public <T extends Identifiable> void updateObject(T obj,
			NameValuePair... params) throws RedmineException {
		final EntityConfig<T> config = getConfig(obj.getClass());
		final URI uri = getURIConfigurator().getObjectURI(obj.getClass(),
				Integer.toString(obj.getId()));
		final HttpPut http = new HttpPut(uri);

		final String body = RedmineJSONBuilder.toSimpleJSON(
				config.singleObjectName, obj, config.writer);
		setEntity(http, body);

		getCommunicator().sendRequest(http);
	}

	/**
	 * Performs an "delete child Id" request.
	 * 
	 * @param parentClass
	 *            parent object id.
	 * @param object
	 *            object to use.
	 * @param value
	 *            child object id.
	 * @throws RedmineException
	 *             if something goes wrong.
	 */
	public <T> void deleteChildId(Class<?> parentClass, String parentId, T object, 
                Integer value) throws RedmineException {
            URI uri = getURIConfigurator().getChildIdURI(parentClass,
                    parentId, object.getClass(), value);
            HttpDelete httpDelete = new HttpDelete(uri);
            String response = getCommunicator().sendRequest(httpDelete);
            logger.debug(response);
	}

	/**
	 * Deletes an object.
	 * 
	 * @param classs
	 *            object class.
	 * @param id
	 *            object id.
	 * @throws RedmineException
	 *             if something goes wrong.
	 */
	public <T extends Identifiable> void deleteObject(Class<T> classs, String id)
			throws RedmineException {
		final URI uri = getURIConfigurator().getObjectURI(classs, id);
		final HttpDelete http = new HttpDelete(uri);
		getCommunicator().sendRequest(http);
	}

	/**
	 * @param classs
	 *            target class
	 * @param key
	 *            item key
	 * @param args
	 *            extra arguments.
	 * @throws RedmineAuthenticationException
	 *             invalid or no API access key is used with the server, which
	 *             requires authorization. Check the constructor arguments.
	 * @throws NotFoundException
	 *             the object with the given key is not found
	 * @throws RedmineException
	 */
	public <T> T getObject(Class<T> classs, String key, NameValuePair... args)
			throws RedmineException {
		final EntityConfig<T> config = getConfig(classs);
		final URI uri = getURIConfigurator().getObjectURI(classs, key, args);
		final HttpGet http = new HttpGet(uri);
		String response = getCommunicator().sendRequest(http);
		logger.debug(response);
		return parseResponse(response, config.singleObjectName, config.parser);
	}

	/**
	 * Downloads a redmine content.
	 * 
	 * @param uri
	 *            target uri.
	 * @param handler
	 *            content handler.
	 * @return handler result.
	 * @throws RedmineException
	 *             if something goes wrong.
	 */
	public <R> R download(String uri,
			ContentHandler<BasicHttpResponse, R> handler)
			throws RedmineException {
		final HttpGet request = new HttpGet(uri);
		return errorCheckingCommunicator.sendRequest(request, handler);
	}

	/**
	 * UPloads content on a server.
	 * 
	 * @param content
	 *            content stream.
	 * @return uploaded item token.
	 * @throws RedmineException
	 *             if something goes wrong.
	 */
	public String upload(InputStream content) throws RedmineException {
		final URI uploadURI = getURIConfigurator().getUploadURI();
		final HttpPost request = new HttpPost(uploadURI);
		final AbstractHttpEntity entity = new InputStreamEntity(content, -1);
		/* Content type required by a Redmine */
		entity.setContentType("application/octet-stream");
		request.setEntity(entity);

		final String result = getCommunicator().sendRequest(request);
		return parseResponse(result, "upload",
            RedmineJSONParser.UPLOAD_TOKEN_PARSER);
	}

	/**
	 * @param classs
	 *            target class
	 * @param key
	 *            item key
	 * @param args
	 *            extra arguments.
	 * @throws RedmineAuthenticationException
	 *             invalid or no API access key is used with the server, which
	 *             requires authorization. Check the constructor arguments.
	 * @throws NotFoundException
	 *             the object with the given key is not found
	 * @throws RedmineException
	 */
	public <T> T getObject(Class<T> classs, Integer key, NameValuePair... args)
			throws RedmineException {
		return getObject(classs, key.toString(), args);
	}

	public <T> List<T> getObjectsList(Class<T> objectClass,
			NameValuePair... params) throws RedmineException {
		return getObjectsList(objectClass, Arrays.asList(params));
	}

	/**
	 * Returns an object list.
	 * 
	 * @return objects list, never NULL
	 */
	public <T> List<T> getObjectsList(Class<T> objectClass,
			Collection<? extends NameValuePair> params) throws RedmineException {
		final EntityConfig<T> config = getConfig(objectClass);
		final List<T> result = new ArrayList<T>();

		final List<NameValuePair> newParams = new ArrayList<NameValuePair>(
				params);

		newParams.add(new BasicNameValuePair("limit", String
				.valueOf(objectsPerPage)));
		int offset = 0;

		int totalObjectsFoundOnServer;
		do {
			List<NameValuePair> paramsList = new ArrayList<NameValuePair>(
					newParams);
			paramsList.add(new BasicNameValuePair("offset", String
					.valueOf(offset)));

			final URI uri = getURIConfigurator().getObjectsURI(objectClass,
					paramsList);

			logger.debug(uri.toString());
			final HttpGet http = new HttpGet(uri);

			final String response = getCommunicator().sendRequest(http);
			logger.debug("received: " + response);

			final List<T> foundItems;
			try {
				final JSONObject responseObject = RedmineJSONParser
						.getResponse(response);
				foundItems = JsonInput.getListOrNull(responseObject,
						config.multiObjectName, config.parser);
				result.addAll(foundItems);

				/* Necessary for trackers */
				if (!responseObject.has(KEY_TOTAL_COUNT)) {
					break;
				}
				totalObjectsFoundOnServer = JsonInput.getInt(responseObject,
						KEY_TOTAL_COUNT);
			} catch (JSONException e) {
				throw new RedmineFormatException(e);
			}

			if (foundItems.size() == 0) {
				break;
			}

			offset += foundItems.size();
		} while (offset < totalObjectsFoundOnServer);

		return result;
	}

	/**
	 * This number of objects (tasks, projects, users) will be requested from
	 * Redmine server in 1 request.
	 */
	public int getObjectsPerPage() {
		return objectsPerPage;
	}

	/**
	 * Delivers a list of a child entries.
	 * 
	 * @param classs
	 *            target class.
	 */
	public <T> List<T> getChildEntries(Class<?> parentClass, String parentId,
			Class<T> classs) throws RedmineException {
		final EntityConfig<T> config = getConfig(classs);
		final URI uri = getURIConfigurator().getChildObjectsURI(parentClass,
				parentId, classs, new BasicNameValuePair("limit", String
						.valueOf(objectsPerPage)));

		HttpGet http = new HttpGet(uri);
		String response = getCommunicator().sendRequest(http);
		final JSONObject responseObject;
		try {
			responseObject = RedmineJSONParser.getResponse(response);
			return JsonInput.getListNotNull(responseObject,
					config.multiObjectName, config.parser);
		} catch (JSONException e) {
			throw new RedmineFormatException("Bad categories response " + response, e);
		}
	}

	/**
	 * This number of objects (tasks, projects, users) will be requested from
	 * Redmine server in 1 request.
	 */
	public void setObjectsPerPage(int pageSize) {
		if (pageSize <= 0) {
			throw new IllegalArgumentException(
					"Page size must be >= 0. You provided: " + pageSize);
		}
		this.objectsPerPage = pageSize;
	}
	
	public void addUserToGroup(int userId, int groupId) throws RedmineException {
		logger.debug("adding user " + userId + " to group " + groupId + "...");
		URI uri = getURIConfigurator().getChildObjectsURI(Group.class, Integer.toString(groupId), User.class);
		HttpPost httpPost = new HttpPost(uri);
		final StringWriter writer = new StringWriter();
		final JSONWriter jsonWriter = new JSONWriter(writer);
		try {
			jsonWriter.object().key("user_id").value(userId).endObject();
		} catch (JSONException e) {
			throw new RedmineInternalError("Unexpected exception", e);
		}
		String body = writer.toString();
		setEntity(httpPost, body);
		String response = getCommunicator().sendRequest(httpPost);
		logger.debug(response);
	}

	public void addWatcherToIssue(int watcherId, int issueId) throws RedmineException {
		logger.debug("adding watcher " + watcherId + " to issue " + issueId + "...");
		URI uri = getURIConfigurator().getChildObjectsURI(Issue.class, Integer.toString(issueId), Watcher.class);
		HttpPost httpPost = new HttpPost(uri);
		final StringWriter writer = new StringWriter();
		final JSONWriter jsonWriter = new JSONWriter(writer);
		try {
			jsonWriter.object().key("user_id").value(watcherId).endObject();
		} catch (JSONException e) {
			throw new RedmineInternalError("Unexpected exception", e);
		}
		String body = writer.toString();
		setEntity(httpPost, body);
		String response = getCommunicator().sendRequest(httpPost);
		logger.debug(response);
	}

	private SimpleCommunicator<String> getCommunicator()
			throws RedmineException {
		return communicator;
	}

	private static <T> T parseResponse(String response, String tag,
                                     JsonObjectParser<T> parser) throws RedmineFormatException {
		try {
			return parser.parse(RedmineJSONParser.getResponseSingleObject(response, tag));
		} catch (JSONException e) {
			throw new RedmineFormatException(e);
		}
	}

	private void setEntity(HttpEntityEnclosingRequest request, String body) {
		setEntity(request, body, CONTENT_TYPE);
	}

	private void setEntity(HttpEntityEnclosingRequest request, String body, String contentType) {
		StringEntity entity;
		try {
			entity = new StringEntity(body, CHARSET);
		} catch (UnsupportedEncodingException e) {
			throw new RedmineInternalError("Required charset " + CHARSET
					+ " is not supported", e);
		}
		entity.setContentType(contentType);
		request.setEntity(entity);
	}

	@SuppressWarnings("unchecked")
	private <T> EntityConfig<T> getConfig(Class<?> class1) {
		final EntityConfig<?> guess = OBJECT_CONFIGS.get(class1);
		if (guess == null)
			throw new RedmineInternalError("Unsupported class " + class1);
		return (EntityConfig<T>) guess;
	}

	private URIConfigurator getURIConfigurator() {
		return configurator;
	}

	private static <T> EntityConfig<T> config(String objectField,
			String urlPrefix, JsonObjectWriter<T> writer,
			JsonObjectParser<T> parser) {
		return new EntityConfig<T>(objectField, urlPrefix, writer, parser);
	}

	public void setCredentials(String login, String password) {
		this.login = login;
		this.password = password;
		authenticator.setCredentials(login, password);
	}

	public void shutdown() {
		baseCommunicator.shutdown();
	}

	public void setPassword(String password) {
		setCredentials(login, password);
	}

	public void setLogin(String login) {
		setCredentials(login, password);
	}

	/**
	 * Entity config.
	 */
	static class EntityConfig<T> {
		final String singleObjectName;
		final String multiObjectName;
		final JsonObjectWriter<T> writer;
		final JsonObjectParser<T> parser;

		public EntityConfig(String objectField, String urlPrefix,
				JsonObjectWriter<T> writer, JsonObjectParser<T> parser) {
			super();
			this.singleObjectName = objectField;
			this.multiObjectName = urlPrefix;
			this.writer = writer;
			this.parser = parser;
		}
	}

}
