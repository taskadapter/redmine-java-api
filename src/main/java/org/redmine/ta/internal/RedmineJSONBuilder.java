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

	public static JsonObjectWriter<Project> CREATE_PROJECT_WRITER = new JsonObjectWriter<Project>() {
		@Override
		public void write(JsonWriter writer, Project object) throws IOException {
			writeCreateRequest(writer, object);
		}
	};

	/**
	 * Writes a "create project" request.
	 * 
	 * @param writer
	 *            project writer.
	 * @param project
	 *            project to create.
	 * @throws IllegalArgumentException
	 *             if some project fields are not configured.
	 * @throws IOException
	 *             if IO error occurs.
	 */
	public static void writeCreateRequest(JsonWriter writer, Project project)
			throws IllegalArgumentException, IOException {
		/* Validate project */
		if (project.getName() == null)
			throw new IllegalArgumentException(
					"Project name must be set to create a new project");
		if (project.getIdentifier() == null)
			throw new IllegalArgumentException(
					"Project identifier must be set to create a new project");

		writeProject(project, writer);
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
		writer.name("id").value(tracker.getId());
		writer.name("name").value(tracker.getName());
	}

	/**
	 * Converts object to a "simple" json.
	 * 
	 * @param tag
	 *            object tag.
	 * @param object
	 *            object to convert.
	 * @param writer
	 *            object writer.
	 * @return object String representation.
	 * @throws RedmineInternalError
	 *             if conversion fails.
	 */
	public static <T> String toSimpleJSON(String tag, T object,
			JsonObjectWriter<T> writer) throws RedmineInternalError {
		final StringWriter swriter = new StringWriter();
		final JsonWriter jsWriter = new JsonWriter(swriter);
		try {
			jsWriter.beginObject();
			jsWriter.name(tag);
			jsWriter.beginObject();
			writer.write(jsWriter, object);
			jsWriter.endObject();
			jsWriter.endObject();
		} catch (IOException e) {
			throw new RedmineInternalError("Unexpected IOException", e);
		}

		return swriter.toString();
	}

	public static void writeProject(Project project, final JsonWriter writer)
			throws IOException {
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
		for (T item : items) {
			writer.beginObject();
			objWriter.write(writer, item);
			writer.endObject();
		}
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
