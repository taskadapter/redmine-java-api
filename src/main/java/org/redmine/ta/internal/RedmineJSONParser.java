package org.redmine.ta.internal;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.redmine.ta.RedmineFormatException;
import org.redmine.ta.beans.Issue;
import org.redmine.ta.beans.Project;
import org.redmine.ta.beans.Tracker;
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
		public Tracker parse(JsonElement input) throws RedmineFormatException {
			return parseTracker(JsonInput.toObject(input));
		}
	};

	public static final JsonObjectParser<Project> PROJECT_PARSER = new JsonObjectParser<Project>() {
		@Override
		public Project parse(JsonElement input) throws RedmineFormatException {
			return parseProject(JsonInput.toObject(input));
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
			throws RedmineFormatException {
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
			throws RedmineFormatException {
		final Project result = new Project();
		result.setId(JsonInput.getInt(content, "id"));
		result.setIdentifier(JsonInput.getStringNotNull(content, "identifier"));
		result.setName(JsonInput.getStringNotNull(content, "name"));
		result.setDescription(JsonInput.getStringOrNull(content, "description"));
		result.setHomepage(JsonInput.getStringOrNull(content, "homepage"));
		result.setCreatedOn(getDateOrNull(content, "created_on"));
		result.setUpdatedOn(getDateOrNull(content, "updated_on"));
		final JsonObject parentProject = JsonInput.getObjectOrNull(content, "parent");
		if (parentProject != null)
			result.setParentId(JsonInput.getInt(parentProject, "id"));
		result.setTrackers(JsonInput.getListOrNull(content, "trackers", TRACKER_PARSER));
		return result;
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
			throws RedmineFormatException {
		final SimpleDateFormat dateFormat = RedmineDateUtils.FULL_DATE_FORMAT
				.get();
		return JsonInput.getDateOrNull(obj, field, dateFormat);
	}

	public static JsonObject getResponceSingleObject(String body, String key)
			throws RedmineFormatException {
		try {
			final JsonObject bodyJson = JsonInput.toObject(new JsonParser().parse(body));
			final JsonObject contentJSon = JsonInput.getObjectNotNull(bodyJson, key);
			return contentJSon;
		} catch (JsonParseException e) {
			throw new RedmineFormatException(e);
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
