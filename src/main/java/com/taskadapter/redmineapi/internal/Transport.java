package com.taskadapter.redmineapi.internal;

import com.taskadapter.redmineapi.NotFoundException;
import com.taskadapter.redmineapi.RedmineAuthenticationException;
import com.taskadapter.redmineapi.RedmineException;
import com.taskadapter.redmineapi.RedmineFormatException;
import com.taskadapter.redmineapi.RedmineInternalError;
import com.taskadapter.redmineapi.RedmineManager;
import com.taskadapter.redmineapi.bean.Attachment;
import com.taskadapter.redmineapi.bean.CustomFieldDefinition;
import com.taskadapter.redmineapi.bean.File;
import com.taskadapter.redmineapi.bean.FluentStyle;
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
import com.taskadapter.redmineapi.bean.WikiPage;
import com.taskadapter.redmineapi.bean.WikiPageDetail;
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
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.StringWriter;
import java.net.URI;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Transport {
	private static final Map<Class<?>, EntityConfig<?>> OBJECT_CONFIGS = new HashMap<>();
	private static final String CONTENT_TYPE = "application/json; charset=utf-8";
	private static final int DEFAULT_OBJECTS_PER_PAGE = 25;
	private static final String KEY_TOTAL_COUNT = "total_count";
	private static final String KEY_LIMIT = "limit";
	private static final String KEY_OFFSET = "offset";

	private final Logger logger = LoggerFactory.getLogger(RedmineManager.class);
	private SimpleCommunicator<String> communicator;
	private Communicator<BasicHttpResponse> errorCheckingCommunicator;
	private Communicator<HttpResponse> authenticator;

    private String onBehalfOfUser = null;

    static {
		OBJECT_CONFIGS.put(
				Project.class,
				config("project", "projects",
						RedmineJSONBuilder::writeProject,
						RedmineJSONParser::parseProject));
		OBJECT_CONFIGS.put(
				Issue.class,
				config("issue", "issues", RedmineJSONBuilder::writeIssue,
						RedmineJSONParser::parseIssue));
		OBJECT_CONFIGS.put(
				User.class,
				config("user", "users", RedmineJSONBuilder::writeUser,
						RedmineJSONParser::parseUser));
		OBJECT_CONFIGS.put(
				Group.class,
				config("group", "groups", RedmineJSONBuilder::writeGroup,
						RedmineJSONParser::parseGroup));
		OBJECT_CONFIGS.put(
				IssueCategory.class,
				config("issue_category", "issue_categories",
						RedmineJSONBuilder::writeCategory,
						RedmineJSONParser::parseCategory));
		OBJECT_CONFIGS.put(
				Version.class,
				config("version", "versions",
						RedmineJSONBuilder::writeVersion,
						RedmineJSONParser::parseVersion));
		OBJECT_CONFIGS.put(
				TimeEntry.class,
				config("time_entry", "time_entries",
						RedmineJSONBuilder::writeTimeEntry,
						RedmineJSONParser::parseTimeEntry));
		OBJECT_CONFIGS.put(News.class,
				config("news", "news", null, RedmineJSONParser::parseNews));
		OBJECT_CONFIGS.put(
				IssueRelation.class,
				config("relation", "relations",
						RedmineJSONBuilder::writeRelation,
						RedmineJSONParser::parseRelation));
		OBJECT_CONFIGS.put(
				Tracker.class,
				config("tracker", "trackers", null,
						RedmineJSONParser::parseTracker));
		OBJECT_CONFIGS.put(
				IssueStatus.class,
				config("status", "issue_statuses", null,
						RedmineJSONParser::parseStatus));
		OBJECT_CONFIGS
				.put(SavedQuery.class,
						config("query", "queries", null,
								RedmineJSONParser::parseSavedQuery));
		OBJECT_CONFIGS.put(Role.class,
				config("role", "roles", null, RedmineJSONParser::parseRole));
		OBJECT_CONFIGS.put(
				Membership.class,
				config("membership", "memberships",
						RedmineJSONBuilder::writeMembership,
						RedmineJSONParser::parseMembership));
        OBJECT_CONFIGS.put(
                IssuePriority.class,
                config("issue_priority", "issue_priorities", null,
                        RedmineJSONParser::parseIssuePriority));
        OBJECT_CONFIGS.put(
                TimeEntryActivity.class,
                config("time_entry_activity", "time_entry_activities", null,
                        RedmineJSONParser::parseTimeEntryActivity));

        OBJECT_CONFIGS.put(
                Watcher.class,
                config("watcher", "watchers", null,
                        RedmineJSONParser::parseWatcher));

        OBJECT_CONFIGS.put(
                WikiPage.class,
                config("wiki_page", "wiki_pages", null, RedmineJSONParser::parseWikiPage)
        );

        OBJECT_CONFIGS.put(
                WikiPageDetail.class,
                config("wiki_page", null, RedmineJSONBuilder::writeWikiPageDetail, RedmineJSONParser::parseWikiPageDetail)
        );
        OBJECT_CONFIGS.put(
                CustomFieldDefinition.class,
                config("custom_field", "custom_fields", null,
                        RedmineJSONParser::parseCustomFieldDefinition));
		OBJECT_CONFIGS.put(
				Attachment.class,
				config("attachment", "attachments", null,
						RedmineJSONParser::parseAttachments));
		OBJECT_CONFIGS.put(
				File.class,
				config("file", "files", null, RedmineJSONParser::parseFiles));
    }

	private URIConfigurator configurator;
	private int objectsPerPage = DEFAULT_OBJECTS_PER_PAGE;
	private static final String CHARSET = "UTF-8";

	public Transport(URIConfigurator configurator, HttpClient client) {
		var baseCommunicator = new BaseCommunicator(client);
		var redmineAuthenticator = new RedmineAuthenticator<>(baseCommunicator, CHARSET);
		configure(configurator, redmineAuthenticator);
	}

	public Transport(URIConfigurator configurator, Communicator communicator) {
		configure(configurator, communicator);
	}

	private void configure(URIConfigurator configurator, Communicator communicator) {
		this.configurator = configurator;
		this.authenticator = communicator;
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

	public User getCurrentUser(RequestParam... params) throws RedmineException {
		URI uri = getURIConfigurator().createURI("users/current.json", params);
		HttpGet http = new HttpGet(uri);
		String response = send(http);
		return parseResponse(response, "user", RedmineJSONParser::parseUser);
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
	public <T> T addObject(T object, RequestParam... params)
			throws RedmineException {
		final EntityConfig<T> config = getConfig(object.getClass());
        if (config.writer == null) {
            throw new RuntimeException("can't create object: writer is not implemented or is not registered in RedmineJSONBuilder for object " + object);
        }
		URI uri = getURIConfigurator().getObjectsURI(object.getClass(), params);
		HttpPost httpPost = new HttpPost(uri);
		String body = RedmineJSONBuilder.toSimpleJSON(config.singleObjectName, object, config.writer);
		setEntity(httpPost, body);
		String response = send(httpPost);
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
			RequestParam... params) throws RedmineException {
		final EntityConfig<T> config = getConfig(object.getClass());
		URI uri = getURIConfigurator().getChildObjectsURI(parentClass,
				parentId, object.getClass(), params);
		HttpPost httpPost = new HttpPost(uri);
		String body = RedmineJSONBuilder.toSimpleJSON(config.singleObjectName,
				object, config.writer);
		setEntity(httpPost, body);
		String response = send(httpPost);
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
			RequestParam... params) throws RedmineException {
		final EntityConfig<T> config = getConfig(obj.getClass());
		final Integer id = obj.getId();
		if (id == null) {
			throw new RuntimeException("'id' field cannot be NULL in the given object:" +
					" it is required to identify the object in the target system");
		}
		final URI uri = getURIConfigurator().getObjectURI(obj.getClass(),
				Integer.toString(id), params);
		final HttpPut http = new HttpPut(uri);
		final String body = RedmineJSONBuilder.toSimpleJSON(
				config.singleObjectName, obj, config.writer);
		setEntity(http, body);
		send(http);
	}

	/*
	 * note: This method cannot return the updated object from Redmine because
	 * the server does not provide anything in response.
	 */
	public <T> void updateChildEntry(Class<?> parentClass, String parentId,
			T obj, String objId, RequestParam... params) throws RedmineException {
		final EntityConfig<T> config = getConfig(obj.getClass());
		URI uri = getURIConfigurator().getChildIdURI(parentClass, parentId, obj.getClass(), objId, params);
		final HttpPut http = new HttpPut(uri);
		final String body = RedmineJSONBuilder.toSimpleJSON(config.singleObjectName, obj, config.writer);
		setEntity(http, body);
		send(http);
	}

	/**
	 * Performs "delete child Id" request.
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
    public <T> void deleteChildId(Class<?> parentClass, String parentId, T object, Integer value) throws RedmineException {
        URI uri = getURIConfigurator().getChildIdURI(parentClass, parentId, object.getClass(), value);
        HttpDelete httpDelete = new HttpDelete(uri);
        String response = send(httpDelete);
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
		send(http);
	}

	/**
	 * @param classs
	 *            target class
	 * @param key
	 *            item key
	 * @param params
	 *            extra arguments.
	 * @throws RedmineAuthenticationException
	 *             invalid or no API access key is used with the server, which
	 *             requires authorization. Check the constructor arguments.
	 * @throws NotFoundException
	 *             the object with the given key is not found
	 * @throws RedmineException
	 */
	public <T> T getObject(Class<T> classs, String key, RequestParam... params)
			throws RedmineException {
		final EntityConfig<T> config = getConfig(classs);
		final URI uri = getURIConfigurator().getObjectURI(classs, key, params);
		final HttpGet http = new HttpGet(uri);
		String response = send(http);
		logger.debug(response);
		return parseResponse(response, config.singleObjectName, config.parser);
	}

	/**
	 * Downloads redmine content.
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
        if (onBehalfOfUser != null) {
            request.addHeader("X-Redmine-Switch-User", onBehalfOfUser);
        }
        return errorCheckingCommunicator.sendRequest(request, handler);
    }

	/**
	 * Deprecated because Redmine server can return invalid string depending on its configuration.
	 * See https://github.com/taskadapter/redmine-java-api/issues/78 .
	 * <p>Use {@link #upload(InputStream, long)} instead.
	 *
	 * <p>
	 * Uploads content on a server. This method calls {@link #upload(InputStream, long)} with -1 as content length.
	 * 
	 * @param content the content stream.
	 * @return uploaded item token.
	 * @throws RedmineException if something goes wrong.
	 */
	@Deprecated
	public String upload(InputStream content) throws RedmineException {
		return upload(content, -1);
	}

	/**
	 * @param content the content
	 * @param contentLength the length of the content in bytes. you can provide -1 but be aware that some
	 *                      users reported Redmine configuration problems that prevent it from processing -1 correctly.
	 *                      See https://github.com/taskadapter/redmine-java-api/issues/78 for details.
	 * @return the string token of the uploaded item. see {@link Attachment#getToken()}
	 */
	public String upload(InputStream content, long contentLength) throws RedmineException {
		final URI uploadURI = getURIConfigurator().getUploadURI();
		final HttpPost request = new HttpPost(uploadURI);
		final AbstractHttpEntity entity = new InputStreamEntity(content, contentLength);
		/* Content type required by a Redmine */
		entity.setContentType("application/octet-stream");
		request.setEntity(entity);

		final String result = send(request);
		return parseResponse(result, "upload", input -> JsonInput.getStringNotNull(input, "token"));
	}

	/**
	 * @param classs
	 *            target class
	 * @param key
	 *            item key
	 * @param params
	 *            extra arguments.
	 * @throws RedmineAuthenticationException
	 *             invalid or no API access key is used with the server, which
	 *             requires authorization. Check the constructor arguments.
	 * @throws NotFoundException
	 *             the object with the given key is not found
	 * @throws RedmineException
	 */
	public <T> T getObject(Class<T> classs, Integer key, RequestParam... params) throws RedmineException {
		return getObject(classs, key.toString(), params);
	}

	public <T> List<T> getObjectsList(Class<T> objectClass, RequestParam... params) throws RedmineException {
		return getObjectsList(objectClass, Arrays.asList(params));
	}

	/**
	 * Returns all objects found using the provided parameters.
	 * This method IGNORES "limit" and "offset" parameters and handles paging AUTOMATICALLY for you.
	 * Please use getObjectsListNoPaging() method if you want to control paging yourself with "limit" and "offset" parameters.
	 * 
	 * @return objects list, never NULL
	 *
	 * @see #getObjectsListNoPaging(Class, Collection)
	 */
	public <T> List<T> getObjectsList(Class<T> objectClass,
									  Collection<? extends RequestParam> params) throws RedmineException {
		final List<T> result = new ArrayList<>();
		int offset = 0;

		Integer totalObjectsFoundOnServer;
		do {
			final List<RequestParam> newParams = new ArrayList<>(params);
			newParams.add(new RequestParam("limit", String.valueOf(objectsPerPage)));
			newParams.add(new RequestParam("offset", String.valueOf(offset)));

			final ResultsWrapper<T> wrapper = getObjectsListNoPaging(objectClass, newParams);
			result.addAll(wrapper.getResults());

			totalObjectsFoundOnServer = wrapper.getTotalFoundOnServer();
			// Necessary for trackers.
			// TODO Alexey: is this still necessary for Redmine 2.x?
			if (totalObjectsFoundOnServer == null) {
				break;
			}
			if (!wrapper.hasSomeResults()) {
				break;
			}
			offset += wrapper.getResultsNumber();
		} while (offset < totalObjectsFoundOnServer);
		return result;
	}

	/**
	 * Returns an object list. Provide your own "limit" and "offset" parameters if you need those, otherwise
	 * this method will return the first page of some default size only (this default is controlled by
	 * your Redmine configuration).
	 *
	 * @return objects list, never NULL
	 */
	public <T> ResultsWrapper<T> getObjectsListNoPaging(Class<T> objectClass,
											  Collection<? extends RequestParam> params) throws RedmineException {
		final EntityConfig<T> config = getConfig(objectClass);
		try {
			final JSONObject responseObject = getJsonResponseFromGet(objectClass, params);
			List<T> results = JsonInput.getListOrNull(responseObject, config.multiObjectName, config.parser);
			Integer totalFoundOnServer = JsonInput.getIntOrNull(responseObject, KEY_TOTAL_COUNT);
			Integer limitOnServer = JsonInput.getIntOrNull(responseObject, KEY_LIMIT);
			Integer offsetOnServer = JsonInput.getIntOrNull(responseObject, KEY_OFFSET);
			return new ResultsWrapper<>(totalFoundOnServer, limitOnServer, offsetOnServer, results);
		} catch (JSONException e) {
			throw new RedmineFormatException(e);
		}
	}

	/**
	 * Use this method if you need direct access to Json results.
	 <pre>
	 Params params = new Params()
	   .add(...)
     getJsonResponseFromGet(Issue.class, params);
	 </pre>
	 */
	public <T> JSONObject getJsonResponseFromGet(Class<T> objectClass,
												 Collection<? extends RequestParam> params) throws RedmineException, JSONException {
		final List<RequestParam> newParams = new ArrayList<>(params);
		List<RequestParam> paramsList = new ArrayList<>(newParams);
		final URI uri = getURIConfigurator().getObjectsURI(objectClass, paramsList);
		final HttpGet http = new HttpGet(uri);
		final String response = send(http);
		return RedmineJSONParser.getResponse(response);
	}

	public <T> List<T> getChildEntries(Class<?> parentClass, int parentId, Class<T> classs) throws RedmineException {
		return getChildEntries(parentClass, parentId + "", classs);
	}

	/**
	 * Delivers a list of a child entries.
	 * 
	 * @param classs
	 *            target class.
	 */
	public <T> List<T> getChildEntries(Class<?> parentClass, String parentKey, Class<T> classs) throws RedmineException {
		final EntityConfig<T> config = getConfig(classs);
		final URI uri = getURIConfigurator().getChildObjectsURI(parentClass,
				parentKey, classs, new RequestParam("limit", String.valueOf(objectsPerPage)));

		HttpGet http = new HttpGet(uri);
		String response = send(http);
		final JSONObject responseObject;
		try {
			responseObject = RedmineJSONParser.getResponse(response);
			return JsonInput.getListNotNull(responseObject, config.multiObjectName, config.parser);
		} catch (JSONException e) {
			throw new RedmineFormatException("Bad categories response " + response, e);
		}
	}

    /**
     * Delivers a single child entry by its identifier.
     */
    public <T> T getChildEntry(Class<?> parentClass, String parentId,
                               Class<T> classs, String childId, RequestParam... params) throws RedmineException {
        final EntityConfig<T> config = getConfig(classs);
		final URI uri = getURIConfigurator().getChildIdURI(parentClass, parentId, classs, childId, params);
        HttpGet http = new HttpGet(uri);
        String response = send(http);

        return parseResponse(response, config.singleObjectName, config.parser);
    }

    /**
	 * This number of objects (tasks, projects, users) will be requested from
	 * Redmine server in 1 request.
	 */
	public void setObjectsPerPage(int pageSize) {
		if (pageSize <= 0) {
			throw new IllegalArgumentException("Page size must be >= 0. You provided: " + pageSize);
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
		String response = send(httpPost);
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
		String response = send(httpPost);
		logger.debug(response);
	}

    private String send(HttpRequestBase http) throws RedmineException {
        if (onBehalfOfUser != null) {
            http.addHeader("X-Redmine-Switch-User", onBehalfOfUser);
        }
        return communicator.sendRequest(http);
    }

	private <T> T parseResponse(String response, String tag,
                                     JsonObjectParser<T> parser) throws RedmineFormatException {
		try {
			T parse = parser.parse(RedmineJSONParser.getResponseSingleObject(response, tag));
			if (parse instanceof FluentStyle) {
				((FluentStyle) parse).setTransport(this);
			}
			return parse;
		} catch (JSONException e) {
			throw new RedmineFormatException(e);
		}
	}

	private static void setEntity(HttpEntityEnclosingRequest request, String body) {
		setEntity(request, body, CONTENT_TYPE);
	}

	private static void setEntity(HttpEntityEnclosingRequest request, String body, String contentType) {
		StringEntity entity;
		try {
			entity = new StringEntity(body, CHARSET);
		} catch (UnsupportedCharsetException e) {
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
		return new EntityConfig<>(objectField, urlPrefix, writer, parser);
	}

    /**
     * This works only when the main authentication has led to Redmine Admin level user.
     * The given user name will be sent to the server in "X-Redmine-Switch-User" HTTP Header
     * to indicate that the action (create issue, delete issue, etc) must be done
     * on behalf of the given user name.
     *
     * @param loginName Redmine user login name to provide to the server
     *
     * @see <a href="http://www.redmine.org/issues/11755">Redmine issue 11755</a>
     */
    public void setOnBehalfOfUser(String loginName) {
        this.onBehalfOfUser = loginName;
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
