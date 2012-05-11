package org.redmine.ta.internal;

import com.google.gson.*;
import com.google.gson.stream.JsonWriter;

import org.redmine.ta.RedmineInternalError;
import org.redmine.ta.beans.Issue;
import org.redmine.ta.beans.Project;
import org.redmine.ta.beans.Tracker;
import org.redmine.ta.beans.User;

import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Date;

/**
 * Builder for requests to Redmine in JSON format. TODO use maps for keys common
 * to builder and parser
 */
public class RedmineJSONBuilder {

	private static JsonObjectWriter<Tracker> TRACKER_WRITER = new JsonObjectWriter<Tracker>() {
		@Override
		public void write(JsonWriter writer, Tracker object) throws IOException {
			writeTracker(writer, object);
		}
	};

	/**
	 * Converts a project to a "create project" request.
	 * 
	 * @param project
	 *            project to create.
	 * @return project string representation.
	 * @throws IllegalArgumentException
	 *             if some project fields are not configured.
	 */
	public static String toCreateRequest(Project project)
			throws IllegalArgumentException {
		/* Validate project */
		if (project.getName() == null)
			throw new IllegalArgumentException(
					"Project name must be set to create a new project");
		if (project.getIdentifier() == null)
			throw new IllegalArgumentException(
					"Project identifier must be set to create a new project");

		return toJSON(project);
	}

	/**
	 * Writes a tracker.
	 * 
	 * @param writer
	 *            used writer.
	 * @param tracker
	 *            tracker to writer.
	 * @throws IOException
	 *             if error occurs.
	 */
	static void writeTracker(JsonWriter writer, Tracker tracker)
			throws IOException {
		writer.beginObject();
		writer.name("id").value(tracker.getId());
		writer.name("name").value(tracker.getName());
		writer.endObject();
	}

	private static String toJSON(Project project) throws RedmineInternalError {
		final StringWriter swriter = new StringWriter();
		final JsonWriter writer = new JsonWriter(swriter);
		try {
			writer.beginObject();
			writer.name("project");
			writer.beginObject();
			addIfNotNull(writer, "id", project.getId());
			addIfNotNull(writer, "identifier", project.getIdentifier());
			addIfNotNull(writer, "name", project.getName());
			addIfNotNull(writer, "description", project.getDescription());
			addIfNotNull(writer, "homepage", project.getHomepage());
			addIfNotNullFull(writer, "created_on", project.getCreatedOn());
			addIfNotNullFull(writer, "updated_on", project.getUpdatedOn());
			addIfNotNull(writer, "parent_id", project.getParentId());
			addArrayIfNotNull(writer, "trackers", project.getTrackers(),
					TRACKER_WRITER);
			writer.endObject();
			writer.endObject();
		} catch (IOException e) {
			throw new RedmineInternalError("Unexpected IOException", e);
		}

		return swriter.toString();
	}

	/**
	 * Adds a list.
	 * 
	 * @param writer
	 *            used writer.
	 * @param field
	 *            field to write.
	 * @param items
	 *            used items.
	 * @param objWriter
	 *            single object writer.
	 */
	private static <T> void addArrayIfNotNull(JsonWriter writer, String field,
			Collection<T> items, JsonObjectWriter<T> objWriter)
			throws IOException {
		if (items == null)
			return;
		writer.name(field).beginArray();
		for (T item : items)
			objWriter.write(writer, item);
		writer.endArray();
	}

	/**
	 * Adds a value to a writer if value is not <code>null</code>.
	 * 
	 * @param writer
	 *            writer to add object to.
	 * @param field
	 *            field name to set.
	 * @param value
	 *            field value.
	 * @throws IOException
	 *             if io error occurs.
	 */
	private static void addIfNotNullFull(JsonWriter writer, String field,
			Date value) throws IOException {
		if (value == null)
			return;
		writer.name(field).value(
				RedmineDateUtils.FULL_DATE_FORMAT.get().format(value));
	}

	/**
	 * Adds a value to a writer if value is not <code>null</code>.
	 * 
	 * @param writer
	 *            writer to add object to.
	 * @param field
	 *            field name to set.
	 * @param value
	 *            field value.
	 * @throws IOException
	 *             if io error occurs.
	 */
	private static void addIfNotNull(JsonWriter writer, String field,
			Integer value) throws IOException {
		if (value == null)
			return;
		writer.name(field).value(value);
	}

	/**
	 * Adds a value to a writer if value is not <code>null</code>.
	 * 
	 * @param writer
	 *            writer to add object to.
	 * @param field
	 *            field name to set.
	 * @param value
	 *            field value.
	 * @throws IOException
	 *             if io error occurs.
	 */
	private static void addIfNotNull(JsonWriter writer, String field,
			String value) throws IOException {
		if (value == null)
			return;
		writer.name(field).value(value);
	}

	/**
	 * Creates a Redmine-compatible JSON representation of a {@link Issue}.
	 * 
	 * @param projectKey
	 *            the ID of the {@link Project}
	 * @param issue
	 *            the {@link Issue}
	 * @return the Redmine-compatible JSON representation of the {@link Issue}
	 */
	public static String toJSON(String projectKey, Issue issue) {
		// TODO we need IDs here for the member entities (project, etc)
		// Another possibility would be to create the JSONObject "manually" here
		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.registerTypeAdapter(User.class, new UserIDSerializer());
		Gson gson = gsonBuilder.create();
		JsonObject jsonObject = gson.toJsonTree(issue).getAsJsonObject();
		jsonObject.add("project_id", new JsonPrimitive(projectKey));
		return wrapJSONProperty("issue", gson.toJson(jsonObject));
	}

	private static String wrapJSONProperty(String property, String jsonEntity) {
		// TODO is there a better way to wrap the entity in a property?
		StringBuilder jsonPropertyWrapper = new StringBuilder();
		jsonPropertyWrapper.append("{\"").append(property).append("\":")
				.append(jsonEntity).append("}");
		return jsonPropertyWrapper.toString();
	}

	static class UserIDSerializer implements JsonSerializer<User> {
		public JsonElement serialize(User user, Type type,
				JsonSerializationContext context) {
			return new JsonPrimitive(user.getId());
		}
	}
}
