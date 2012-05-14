package org.redmine.ta.internal;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.redmine.ta.RedmineFormatException;
import org.redmine.ta.beans.Attachment;
import org.redmine.ta.beans.CustomField;
import org.redmine.ta.beans.Issue;
import org.redmine.ta.beans.IssueCategory;
import org.redmine.ta.beans.IssueRelation;
import org.redmine.ta.beans.Journal;
import org.redmine.ta.beans.Project;
import org.redmine.ta.beans.Tracker;
import org.redmine.ta.beans.User;
import org.redmine.ta.beans.Version;
import org.redmine.ta.internal.json.JsonFormatException;
import org.redmine.ta.internal.json.JsonInput;
import org.redmine.ta.internal.json.JsonObjectParser;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

/**
 * A parser for JSON items sent by Redmine. * TODO use maps for keys common to
 * builder and parser
 */
public class RedmineJSONParser {

	private static final String KEY_TOTAL_COUNT = "total_count";

	public static final JsonObjectParser<Tracker> TRACKER_PARSER = new JsonObjectParser<Tracker>() {
		@Override
		public Tracker parse(JsonElement input) throws JsonFormatException {
			return parseTracker(JsonInput.toObject(input));
		}
	};

	public static final JsonObjectParser<Project> PROJECT_PARSER = new JsonObjectParser<Project>() {
		@Override
		public Project parse(JsonElement input) throws JsonFormatException {
			return parseProject(JsonInput.toObject(input));
		}
	};

	public static final JsonObjectParser<Issue> ISSUE_PARSER = new JsonObjectParser<Issue>() {
		@Override
		public Issue parse(JsonElement input) throws JsonFormatException {
			return parseIssue(JsonInput.toObject(input));
		}
	};

	public static final JsonObjectParser<User> USER_PARSER = new JsonObjectParser<User>() {
		@Override
		public User parse(JsonElement input) throws JsonFormatException {
			return parseUser(JsonInput.toObject(input));
		}
	};

	public static final JsonObjectParser<CustomField> CUSTOM_FIELD_PARSER = new JsonObjectParser<CustomField>() {
		@Override
		public CustomField parse(JsonElement input) throws JsonFormatException {
			return parseCustomField(JsonInput.toObject(input));
		}
	};

	public static final JsonObjectParser<Journal> JOURNAL_PARSER = new JsonObjectParser<Journal>() {
		@Override
		public Journal parse(JsonElement input) throws JsonFormatException {
			return parseJournal(JsonInput.toObject(input));
		}
	};

	public static final JsonObjectParser<Attachment> ATTACHMENT_PARSER = new JsonObjectParser<Attachment>() {
		@Override
		public Attachment parse(JsonElement input) throws JsonFormatException {
			return parseAttachments(JsonInput.toObject(input));
		}
	};

	public static final JsonObjectParser<IssueRelation> RELATION_PARSER = new JsonObjectParser<IssueRelation>() {
		@Override
		public IssueRelation parse(JsonElement input)
				throws JsonFormatException {
			return parseRelation(JsonInput.toObject(input));
		}
	};

	public static final JsonObjectParser<Version> VERSION_PARSER = new JsonObjectParser<Version>() {
		@Override
		public Version parse(JsonElement input) throws JsonFormatException {
			return parseVersion(JsonInput.toObject(input));
		}
	};

	public static final JsonObjectParser<IssueCategory> CATEGORY_PARSER = new JsonObjectParser<IssueCategory>() {
		@Override
		public IssueCategory parse(JsonElement input)
				throws JsonFormatException {
			return parseCategory(JsonInput.toObject(input));
		}
	};

	public static final JsonObjectParser<String> ERROR_PARSER = new JsonObjectParser<String>() {
		@Override
		public String parse(JsonElement input) throws JsonFormatException {
			return input.toString();
		}
	};

	private static final Map<Class<?>, String> redmineSingleResponseKeys = new HashMap<Class<?>, String>() {
		private static final long serialVersionUID = 9127978873143743650L;

		{
			put(Project.class, "project");
			put(Issue.class, "issue");
		}
	};
	private static final Map<Class<?>, String> redmineListResponseKeys = new HashMap<Class<?>, String>() {
		private static final long serialVersionUID = -3514773352872587112L;

		{
			put(Project.class, "projects");
			put(Issue.class, "issues");
		}
	};

	private static JsonParser jsonParser = new JsonParser();

	private static Gson gson = null;

	static {
		gson = new GsonBuilder().setDateFormat("yyyy/MM/dd").create();

	}

	/**
	 * Parses a tracker.
	 * 
	 * @param object
	 *            object to parse.
	 * @return parsed tracker.
	 * @throws RedmineFormatException
	 *             if object is not a valid tracker.
	 */
	public static Tracker parseTracker(JsonObject object)
			throws JsonFormatException {
		final int id = JsonInput.getInt(object, "id");
		final String name = JsonInput.getStringNotNull(object, "name");
		return new Tracker(id, name);
	}

	/**
	 * Parses a project.
	 * 
	 * @param content
	 *            content to parse.
	 * @return parsed project.
	 */
	public static Project parseProject(JsonObject content)
			throws JsonFormatException {
		final Project result = new Project();
		result.setId(JsonInput.getInt(content, "id"));
		result.setIdentifier(JsonInput.getStringOrNull(content, "identifier"));
		result.setName(JsonInput.getStringNotNull(content, "name"));
		result.setDescription(JsonInput.getStringOrNull(content, "description"));
		result.setHomepage(JsonInput.getStringOrNull(content, "homepage"));
		result.setCreatedOn(getDateOrNull(content, "created_on"));
		result.setUpdatedOn(getDateOrNull(content, "updated_on"));
		final JsonObject parentProject = JsonInput.getObjectOrNull(content,
				"parent");
		if (parentProject != null)
			result.setParentId(JsonInput.getInt(parentProject, "id"));
		result.setTrackers(JsonInput.getListOrNull(content, "trackers",
				TRACKER_PARSER));
		return result;
	}

	@SuppressWarnings("deprecation")
	public static Issue parseIssue(JsonObject content)
			throws JsonFormatException {
		final Issue result = new Issue();
		result.setId(JsonInput.getIntOrNull(content, "id"));
		result.setSubject(JsonInput.getStringOrNull(content, "subject"));
		final JsonObject parentIssueObject = JsonInput.getObjectOrNull(content,
				"parent");
		if (parentIssueObject != null)
			result.setParentId(JsonInput.getInt(parentIssueObject, "id"));
		result.setEstimatedHours(JsonInput.getFloatOrNull(content,
				"estimated_hours"));
		result.setSpentHours(JsonInput.getFloatOrNull(content, "spent_hours"));
		result.setAssignee(JsonInput.getObjectOrNull(content, "assigned_to",
				USER_PARSER));

		final JsonObject priorityObject = JsonInput.getObjectOrNull(content,
				"priority");
		if (priorityObject != null) {
			result.setPriorityText(JsonInput.getStringOrNull(priorityObject,
					"name"));
			result.setPriorityId(JsonInput.getIntOrNull(priorityObject, "id"));
		}

		result.setDoneRatio(JsonInput.getIntOrNull(content, "done_ratio"));
		result.setProject(JsonInput.getObjectOrNull(content, "project",
				PROJECT_PARSER));
		result.setAuthor(JsonInput.getObjectOrNull(content, "author",
				USER_PARSER));
		result.setStartDate(getShortDateOrNull(content, "start_date"));
		result.setDueDate(getShortDateOrNull(content, "due_date"));
		result.setTracker(JsonInput.getObjectOrNull(content, "tracker",
				TRACKER_PARSER));
		result.setDescription(JsonInput.getStringOrNull(content, "description"));
		result.setCreatedOn(getDateOrNull(content, "created_on"));
		result.setUpdatedOn(getDateOrNull(content, "updated_on"));
		final JsonObject statusObject = JsonInput.getObjectOrNull(content,
				"status");
		if (statusObject != null) {
			result.setStatusName(JsonInput
					.getStringOrNull(statusObject, "name"));
			result.setStatusId(JsonInput.getIntOrNull(statusObject, "id"));
		}

		result.setCustomFields(JsonInput.getListOrNull(content,
				"custom_fields", CUSTOM_FIELD_PARSER));
		result.setNotes(JsonInput.getStringOrNull(content, "notes"));
		result.setJournals(JsonInput.getListOrEmpty(content, "journals",
				JOURNAL_PARSER));
		result.getAttachments().addAll(
				JsonInput.getListOrEmpty(content, "attachements",
						ATTACHMENT_PARSER));
		result.getRelations()
				.addAll(JsonInput.getListOrEmpty(content, "relations",
						RELATION_PARSER));
		result.setTargetVersion(JsonInput.getObjectOrNull(content,
				"fixed_version", VERSION_PARSER));
		result.setCategory(JsonInput.getObjectOrNull(content, "category",
				CATEGORY_PARSER));
		return result;
	}

	public static IssueCategory parseCategory(JsonObject content)
			throws JsonFormatException {
		final IssueCategory result = new IssueCategory();
		result.setId(JsonInput.getInt(content, "id"));
		result.setName(JsonInput.getStringOrNull(content, "name"));
		result.setProject(JsonInput.getObjectOrNull(content, "project",
				PROJECT_PARSER));
		result.setAssignee(JsonInput.getObjectOrNull(content, "assigned_to",
				USER_PARSER));
		return result;
	}

	public static Version parseVersion(JsonObject content)
			throws JsonFormatException {
		final Version result = new Version();
		result.setId(JsonInput.getIntOrNull(content, "id"));
		result.setProject(JsonInput.getObjectOrNull(content, "project",
				PROJECT_PARSER));
		result.setName(JsonInput.getStringOrNull(content, "name"));
		result.setDescription(JsonInput.getStringOrNull(content, "description"));
		result.setStatus(JsonInput.getStringOrNull(content, "status"));
		result.setDueDate(getShortDateOrNull(content, "due_date"));
		result.setCreatedOn(getDateOrNull(content, "created_on"));
		result.setUpdatedOn(getDateOrNull(content, "updated_on"));
		return result;
	}

	public static IssueRelation parseRelation(JsonObject content)
			throws JsonFormatException {
		final IssueRelation result = new IssueRelation();
		result.setId(JsonInput.getIntOrNull(content, "id"));
		result.setIssueId(JsonInput.getIntOrNull(content, "issue_id"));
		result.setIssueToId(JsonInput.getIntOrNull(content, "issue_to_id"));
		result.setType(JsonInput.getStringOrNull(content, "relation_type"));
		result.setDelay(JsonInput.getIntOrNull(content, "delay"));
		return result;
	}

	public static Attachment parseAttachments(JsonObject content)
			throws JsonFormatException {
		final Attachment result = new Attachment();
		result.setId(JsonInput.getIntOrNull(content, "id"));
		result.setFileName(JsonInput.getStringOrNull(content, "filename"));
		result.setFileSize(JsonInput.getLong(content, "filesize"));
		result.setContentType(JsonInput
				.getStringOrNull(content, "content_type"));
		result.setContentURL(JsonInput.getStringOrNull(content, "content_url"));
		result.setDescription(JsonInput.getStringOrNull(content, "description"));
		result.setCreatedOn(getDateOrNull(content, "created_on"));
		result.setAuthor(JsonInput.getObjectOrNull(content, "author",
				USER_PARSER));
		return result;
	}

	public static CustomField parseCustomField(JsonObject content)
			throws JsonFormatException {
		final CustomField result = new CustomField();
		result.setId(JsonInput.getInt(content, "id"));
		result.setName(JsonInput.getStringOrNull(content, "name"));
		result.setValue(JsonInput.getStringOrNull(content, "value"));
		return result;
	}

	public static Journal parseJournal(JsonObject content)
			throws JsonFormatException {
		final Journal result = new Journal();
		result.setId(JsonInput.getInt(content, "id"));
		result.setCreatedOn(getDateOrNull(content, "created_on"));
		result.setNotes(JsonInput.getStringOrNull(content, "notes"));
		result.setUser(JsonInput.getObjectOrNull(content, "user", USER_PARSER));
		return result;
	}

	public static User parseUser(JsonObject content) throws JsonFormatException {
		final User result = new User();
		result.setId(JsonInput.getIntOrNull(content, "id"));
		result.setLogin(JsonInput.getStringOrNull(content, "login"));
		result.setPassword(JsonInput.getStringOrNull(content, "password"));
		result.setFirstName(JsonInput.getStringOrNull(content, "firstname"));
		result.setLastName(JsonInput.getStringOrNull(content, "lastname"));
		result.setMail(JsonInput.getStringOrNull(content, "mail"));
		result.setCreatedOn(getDateOrNull(content, "created_on"));
		result.setLastLoginOn(getDateOrNull(content, "last_login_on"));
		result.setCustomFields(JsonInput.getListOrNull(content,
				"custom_fields", CUSTOM_FIELD_PARSER));
		final String name = JsonInput.getStringOrNull(content, "name");
		if (name != null)
			result.setFullName(name);
		return result;
	}

	/**
	 * @param responseBody
	 */
	public static List<String> parseErrors(String responseBody)
			throws JsonFormatException {
		final JsonObject body = getResponce(responseBody);
		return JsonInput.getListNotNull(body, "errors", ERROR_PARSER);
	}

	/**
	 * Fetches an optional date from an object.
	 * 
	 * @param obj
	 *            object to get a field from.
	 * @param field
	 *            field to get a value from.
	 * @throws RedmineFormatException
	 *             if value is not valid
	 */
	private static Date getDateOrNull(JsonObject obj, String field)
			throws JsonFormatException {
		final SimpleDateFormat dateFormat = RedmineDateUtils.FULL_DATE_FORMAT
				.get();
		return JsonInput.getDateOrNull(obj, field, dateFormat);
	}

	/**
	 * Fetches an optional date from an object.
	 * 
	 * @param obj
	 *            object to get a field from.
	 * @param field
	 *            field to get a value from.
	 * @throws RedmineFormatException
	 *             if value is not valid
	 */
	private static Date getShortDateOrNull(JsonObject obj, String field)
			throws JsonFormatException {
		final SimpleDateFormat dateFormat = RedmineDateUtils.SHORT_DATE_FORMAT
				.get();
		return JsonInput.getDateOrNull(obj, field, dateFormat);
	}

	public static JsonObject getResponceSingleObject(String body, String key)
			throws JsonFormatException {
		try {
			final JsonObject bodyJson = JsonInput.toObject(new JsonParser()
					.parse(body));
			final JsonObject contentJSon = JsonInput.getObjectNotNull(bodyJson,
					key);
			return contentJSon;
		} catch (JsonParseException e) {
			throw new JsonFormatException(e);
		}
	}

	public static JsonObject getResponce(String body)
			throws JsonFormatException {
		try {
			final JsonObject bodyJson = JsonInput.toObject(new JsonParser()
					.parse(body));
			return bodyJson;
		} catch (JsonParseException e) {
			throw new JsonFormatException(e);
		}
	}

	public static <T> T parseObject(Class<T> clazz, String body) {
		// determine key for objects list in Redmine response from map
		String key = redmineSingleResponseKeys.get(clazz);
		if (key == null) {
			throw new UnsupportedOperationException(
					"Parsing Redmine object from JSON is presently not supported for class "
							+ clazz);
		}
		// fetch JSON object list from body by key
		JsonObject jsonResponseObject = (JsonObject) jsonParser.parse(body);
		JsonElement jsonElement = jsonResponseObject.get(key);
		// parse
		return gson.fromJson(jsonElement, clazz);
	}

	public static <T> List<T> parseObjects(Class<T> clazz, String body) {
		// determine key for objects list in Redmine response from map
		String key = redmineListResponseKeys.get(clazz);
		if (key == null) {
			throw new UnsupportedOperationException(
					"Parsing Redmine objects from JSON is not supported for class "
							+ clazz);
		}
		// fetch JSON objects list from body by key
		JsonObject jsonResponseObject = (JsonObject) jsonParser.parse(body);
		JsonArray projectsJsonArray = jsonResponseObject.getAsJsonArray(key);
		// parse objects from JSON objects list
		// we can not use a generic collection solution here as GSON is not able
		// to resolve the correct generic type. Hence, we traverse the list of
		// JSON elements.
		List<T> result = new ArrayList<T>(projectsJsonArray.size());
		Iterator<JsonElement> iterator = projectsJsonArray.iterator();
		while (iterator.hasNext()) {
			result.add(gson.fromJson(iterator.next(), clazz));
		}
		return result;
	}

	public static <T> int parseObjectsTotalCount(Class<T> clazz, String body) {
		JsonObject jsonResponseObject = (JsonObject) jsonParser.parse(body);
		return jsonResponseObject.getAsJsonPrimitive(KEY_TOTAL_COUNT)
				.getAsInt();
	}

}
