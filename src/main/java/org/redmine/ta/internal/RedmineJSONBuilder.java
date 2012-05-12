package org.redmine.ta.internal;

import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.redmine.ta.RedmineInternalError;
import org.redmine.ta.beans.CustomField;
import org.redmine.ta.beans.Issue;
import org.redmine.ta.beans.Project;
import org.redmine.ta.beans.Tracker;
import org.redmine.ta.beans.User;
import org.redmine.ta.internal.json.JsonObjectWriter;
import org.redmine.ta.internal.json.JsonOutput;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.stream.JsonWriter;

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

	public static JsonObjectWriter<Project> PROJECT_WRITER = new JsonObjectWriter<Project>() {
		@Override
		public void write(JsonWriter writer, Project object) throws IOException {
			writeProject(writer, object);
		}
	};

	public static JsonObjectWriter<Issue> ISSUE_WRITER = new JsonObjectWriter<Issue>() {
		@Override
		public void write(JsonWriter writer, Issue object) throws IOException {
			writeIssue(object, writer);
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
	public static void writeProject(JsonWriter writer, Project project)
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
		JsonOutput.addIfNotNull(writer, "id", project.getId());
		JsonOutput.addIfNotNull(writer, "identifier", project.getIdentifier());
		JsonOutput.addIfNotNull(writer, "name", project.getName());
		JsonOutput
				.addIfNotNull(writer, "description", project.getDescription());
		JsonOutput.addIfNotNull(writer, "homepage", project.getHomepage());
		addIfNotNullFull(writer, "created_on", project.getCreatedOn());
		addIfNotNullFull(writer, "updated_on", project.getUpdatedOn());
		JsonOutput.addIfNotNull(writer, "parent_id", project.getParentId());
		JsonOutput.addArrayIfNotNull(writer, "trackers", project.getTrackers(),
				TRACKER_WRITER);
	}

	public static void writeIssue(Issue issue, final JsonWriter writer)
			throws IOException {
		JsonOutput.addIfNotNull(writer, "id", issue.getId());
		JsonOutput.addIfNotNull(writer, "subject", issue.getSubject());
		JsonOutput.addIfNotNull(writer, "parent_issue_id", issue.getParentId());
		JsonOutput.addIfNotNull(writer, "estimated_hours",
				issue.getEstimatedHours());
		JsonOutput.addIfNotNull(writer, "spent_hours", issue.getSpentHours());
		if (issue.getAssignee() != null)
			JsonOutput.addIfNotNull(writer, "assigned_to_id", issue
					.getAssignee().getId());
		JsonOutput.addIfNotNull(writer, "priority_id", issue.getPriorityId());
		JsonOutput.addIfNotNull(writer, "done_ratio", issue.getDoneRatio());
		if (issue.getProject() != null)
			JsonOutput.addIfNotNull(writer, "project_id", issue.getProject()
					.getIdentifier());
		if (issue.getAuthor() != null)
			JsonOutput.addIfNotNull(writer, "author_id", issue.getAuthor()
					.getId());
		addIfNotNullFull(writer, "start_date", issue.getStartDate());
		addIfNotNullFull(writer, "due_date", issue.getDueDate());
		if (issue.getTracker() != null)
			JsonOutput.addIfNotNull(writer, "tracker_id", issue.getTracker()
					.getId());
		JsonOutput.addIfNotNull(writer, "description", issue.getDescription());

		addIfNotNullFull(writer, "created_on", issue.getCreatedOn());
		addIfNotNullFull(writer, "updated_on", issue.getUpdatedOn());
		JsonOutput.addIfNotNull(writer, "status_id", issue.getStatusId());
		if (issue.getTargetVersion() != null)
			JsonOutput.addIfNotNull(writer, "fixed_version_id", issue
					.getTargetVersion().getId());
		if (issue.getCategory() != null)
			JsonOutput.addIfNotNull(writer, "category_id", issue.getCategory()
					.getId());
		JsonOutput.addIfNotNull(writer, "notes", issue.getNotes());
		writeCustomFields(writer, issue.getCustomFields());
	}

	private static void writeCustomFields(JsonWriter writer,
			List<CustomField> customFields) throws IOException {
		if (customFields == null || customFields.isEmpty())
			return;
		writer.name("custom_field_values").beginObject();
		for (CustomField field : customFields)
			writer.name(Integer.toString(field.getId()))
					.value(field.getValue());
		writer.endObject();
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
	public static void addIfNotNullFull(JsonWriter writer, String field,
			Date value) throws IOException {
		final SimpleDateFormat format = RedmineDateUtils.FULL_DATE_FORMAT.get();
		JsonOutput.addIfNotNull(writer, field, value, format);
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
