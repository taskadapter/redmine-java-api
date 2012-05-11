package org.redmine.ta.internal;

import java.text.ParseException;
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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

/**
 * A parser for JSON items sent by Redmine. * TODO use maps for keys common to
 * builder and parser
 */
public class RedmineJSONParser {

	private static final String KEY_TOTAL_COUNT = "total_count";

	public static final JsonObjectParser<Tracker> TRACKER_PARSER = new JsonObjectParser<Tracker>() {
		@Override
		public Tracker parse(JsonElement input) throws RedmineFormatException {
			return parseTracker(toObject(input));
		}
	};

	public static final JsonObjectParser<Project> PROJECT_PARSER = new JsonObjectParser<Project>() {
		@Override
		public Project parse(JsonElement input) throws RedmineFormatException {
			return parseProject(toObject(input));
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
		final int id = getInt(object, "id");
		final String name = getStringNotNull(object, "name");
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
		result.setId(getInt(content, "id"));
		result.setIdentifier(getStringNotNull(content, "identifier"));
		result.setName(getStringNotNull(content, "name"));
		result.setDescription(getStringOrNull(content, "description"));
		result.setHomepage(getStringOrNull(content, "homepage"));
		result.setCreatedOn(getDateOrNull(content, "created_on"));
		result.setUpdatedOn(getDateOrNull(content, "updated_on"));
		final JsonObject parentProject = getObjectOrNull(content, "parent");
		if (parentProject != null)
			result.setParentId(getInt(parentProject, "id"));
		result.setTrackers(getListOrNull(content, "trackers", TRACKER_PARSER));
		return result;
	}

	/**
	 * Parses optional item list.
	 * 
	 * @param obj
	 *            object to extract a list from.
	 * @param field
	 *            field to parse.
	 * @param parser
	 *            single item parser.
	 * @return parsed objects.
	 * @throws RedmineFormatException
	 *             if format is invalid.
	 */
	private static <T> List<T> getListOrNull(JsonObject obj, String field,
			JsonObjectParser<T> parser) throws RedmineFormatException {
		final JsonArray items = getArrayOrNull(obj, field);
		if (items == null)
			return null;
		final int length = items.size();
		final List<T> result = new ArrayList<T>(length);
		for (int i = 0; i < length; i++)
			result.add(parser.parse(items.get(i)));
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
		final JsonPrimitive guess = getPrimitiveNotNull(obj, field);
		if (guess == null)
			return null;
		try {
			return RedmineDateUtils.FULL_DATE_FORMAT.get().parse(
					guess.getAsString());
		} catch (ParseException e) {
			throw new RedmineFormatException("Bad date value " + guess);
		}
	}

	/**
	 * Fetches an optional string from an object.
	 * 
	 * @param obj
	 *            object to get a field from.
	 * @param field
	 *            field to get a value from.
	 * @throws RedmineFormatException
	 *             if value is not valid
	 */
	private static String getStringOrNull(JsonObject obj, String field)
			throws RedmineFormatException {
		final JsonPrimitive guess = getPrimitiveOrNull(obj, field);
		if (guess == null)
			return null;
		return guess.getAsString();
	}

	/**
	 * Fetches a string from an object.
	 * 
	 * @param obj
	 *            object to get a field from.
	 * @param field
	 *            field to get a value from.
	 * @throws RedmineFormatException
	 *             if value is not valid, not exists, etc...
	 */
	private static String getStringNotNull(JsonObject obj, String field)
			throws RedmineFormatException {
		return getPrimitiveNotNull(obj, field).getAsString();
	}

	/**
	 * Fetches an int from an object.
	 * 
	 * @param obj
	 *            object to get a field from.
	 * @param field
	 *            field to get a value from.
	 * @throws RedmineFormatException
	 *             if value is not valid, not exists, etc...
	 */
	private static int getInt(JsonObject obj, String field)
			throws RedmineFormatException {
		final JsonPrimitive primitive = getPrimitiveNotNull(obj, field);
		try {
			return primitive.getAsInt();
		} catch (NumberFormatException e) {
			throw new RedmineFormatException("Bad integer value " + primitive);
		}
	}

	/**
	 * Fetches an optional int from an object.
	 * 
	 * @param obj
	 *            object to get a field from.
	 * @param field
	 *            field to get a value from.
	 * @throws RedmineFormatException
	 *             if value is not valid, not exists, etc...
	 */
	private static Integer getIntOrNull(JsonObject obj, String field)
			throws RedmineFormatException {
		final JsonPrimitive primitive = getPrimitiveOrNull(obj, field);
		if (primitive == null)
			return null;
		try {
			return primitive.getAsInt();
		} catch (NumberFormatException e) {
			throw new RedmineFormatException("Bad integer value " + primitive);
		}
	}

	/**
	 * Returns a json array as "not-null" value.
	 * 
	 * @param obj
	 *            object to get a value from.
	 * @param field
	 *            field to get a value from.
	 * @return json array.
	 */
	private static JsonArray getArrayOrNull(JsonObject obj, String field)
			throws RedmineFormatException {
		return toArray(obj.get(field));
	}

	/**
	 * Returns a json array as "not-null" value.
	 * 
	 * @param obj
	 *            object to get a value from.
	 * @param field
	 *            field to get a value from.
	 * @return json array.
	 */
	private static JsonArray getArrayNotNull(JsonObject obj, String field)
			throws RedmineFormatException {
		return toArray(getNotNull(obj, field));
	}

	/**
	 * Returns a json primitive as "maybe-null" value.
	 * 
	 * @param obj
	 *            object to get a value from.
	 * @param field
	 *            field to get a value from.
	 * @return json primitive.
	 */
	private static JsonPrimitive getPrimitiveOrNull(JsonObject obj, String field)
			throws RedmineFormatException {
		return toPrimitive(obj.get(field));
	}

	/**
	 * Returns a json primitive as "not-null" value.
	 * 
	 * @param obj
	 *            object to get a value from.
	 * @param field
	 *            field to get a value from.
	 * @return json primitive.
	 */
	private static JsonPrimitive getPrimitiveNotNull(JsonObject obj,
			String field) throws RedmineFormatException {
		return toPrimitive(getNotNull(obj, field));
	}

	/**
	 * Returns an object as non-null value.
	 * 
	 * @param obj
	 *            object to get a field from.
	 * @param field
	 *            field to get a value.
	 * @return json value.
	 * @throws RedmineFormatException
	 *             if field is not present.
	 */
	private static JsonElement getNotNull(JsonObject obj, String field)
			throws RedmineFormatException {
		final JsonElement resultElt = obj.get(field);
		if (!obj.has(field))
			throw new RedmineFormatException("Missing required field " + field
					+ " in " + obj);
		if (resultElt.isJsonNull())
			throw new RedmineFormatException("Missing required field " + field
					+ " in " + obj);
		return resultElt;
	}

	public static JsonObject getResponceSingleObject(String body, String key)
			throws RedmineFormatException {
		try {
			final JsonObject bodyJson = toObject(new JsonParser().parse(body));
			final JsonObject contentJSon = getObjectNotNull(bodyJson, key);
			return contentJSon;
		} catch (JsonParseException e) {
			throw new RedmineFormatException(e);
		}
	}

	/**
	 * Returns a json object field for a specified object.
	 * 
	 * @param obj
	 *            object to get a field from.
	 * @param field
	 *            returned field.
	 * @return object field.
	 * @throws RedmineFormatException
	 *             if target field is not an object.
	 */
	private static JsonObject getObjectNotNull(JsonObject obj, String field)
			throws RedmineFormatException {
		return toObject(getNotNull(obj, field));
	}

	/**
	 * Returns a json object field for a specified object.
	 * 
	 * @param obj
	 *            object to get a field from.
	 * @param field
	 *            returned field.
	 * @return object field.
	 * @throws RedmineFormatException
	 *             if target field is not an object.
	 */
	private static JsonObject getObjectOrNull(JsonObject obj, String field)
			throws RedmineFormatException {
		final JsonElement elt = obj.get(field);
		return toObject(elt);
	}

	/**
	 * Converts element to object.
	 * 
	 * @param elt
	 *            element to convert.
	 * @return element as object value.
	 * @throws RedmineFormatException
	 *             if element is not an object value.
	 */
	static JsonObject toObject(JsonElement elt) throws RedmineFormatException {
		if (elt == null || elt.isJsonNull())
			return null;
		if (elt.isJsonObject())
			return elt.getAsJsonObject();
		throw new RedmineFormatException("Expected object but got "
				+ elt.getClass() + " in content " + elt);
	}

	/**
	 * Converts element to array.
	 * 
	 * @param elt
	 *            element to convert.
	 * @return element as object value.
	 * @throws RedmineFormatException
	 *             if element is not an object value.
	 */
	private static JsonArray toArray(JsonElement elt)
			throws RedmineFormatException {
		if (elt == null || elt.isJsonNull())
			return null;
		if (elt.isJsonArray())
			return elt.getAsJsonArray();
		throw new RedmineFormatException("Expected array but got "
				+ elt.getClass() + " in content " + elt);
	}

	/**
	 * Converts element to primitive.
	 * 
	 * @param elt
	 *            element to convert.
	 * @return element as a primitive value.
	 * @throws RedmineFormatException
	 *             if element is not an primitive value.
	 */
	private static JsonPrimitive toPrimitive(JsonElement elt)
			throws RedmineFormatException {
		if (elt == null || elt.isJsonNull())
			return null;
		if (elt.isJsonPrimitive())
			return elt.getAsJsonPrimitive();
		throw new RedmineFormatException("Expected primitive but got "
				+ elt.getClass() + " in content " + elt);
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
