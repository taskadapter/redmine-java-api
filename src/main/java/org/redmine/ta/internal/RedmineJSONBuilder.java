package org.redmine.ta.internal;

import java.io.IOException;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.redmine.ta.RedmineInternalError;
import org.redmine.ta.beans.CustomField;
import org.redmine.ta.beans.Issue;
import org.redmine.ta.beans.IssueCategory;
import org.redmine.ta.beans.IssueRelation;
import org.redmine.ta.beans.Project;
import org.redmine.ta.beans.TimeEntry;
import org.redmine.ta.beans.Tracker;
import org.redmine.ta.beans.User;
import org.redmine.ta.beans.Version;
import org.redmine.ta.internal.json.JsonObjectWriter;
import org.redmine.ta.internal.json.JsonOutput;

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

	public static JsonObjectWriter<User> USER_WRITER = new JsonObjectWriter<User>() {
		@Override
		public void write(JsonWriter writer, User object) throws IOException {
			writeUser(object, writer);
		}
	};

	public static JsonObjectWriter<IssueRelation> RELATION_WRITER = new JsonObjectWriter<IssueRelation>() {
		@Override
		public void write(JsonWriter writer, IssueRelation object)
				throws IOException {
			writeRelation(writer, object);
		}
	};

	public static JsonObjectWriter<IssueCategory> CATEGORY_WRITER = new JsonObjectWriter<IssueCategory>() {
		@Override
		public void write(JsonWriter writer, IssueCategory object)
				throws IOException {
			writeCategory(object, writer);
		}
	};

	public static JsonObjectWriter<Version> VERSION_WRITER = new JsonObjectWriter<Version>() {
		@Override
		public void write(JsonWriter writer, Version object) throws IOException {
			writeVersion(writer, object);
		}
	};

	public static JsonObjectWriter<TimeEntry> TIME_ENTRY_WRITER = new JsonObjectWriter<TimeEntry>() {
		@Override
		public void write(JsonWriter writer, TimeEntry object)
				throws IOException {
			writeTimeEntry(writer, object);
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

	static void writeTimeEntry(JsonWriter writer, TimeEntry timeEntry)
			throws IOException {
		JsonOutput.addIfNotNull(writer, "id", timeEntry.getId());
		JsonOutput.addIfNotNull(writer, "project_id", timeEntry.getProjectId());
		JsonOutput.addIfNotNull(writer, "issue_id", timeEntry.getIssueId());
		JsonOutput.addIfNotNull(writer, "user_id", timeEntry.getUserId());
		JsonOutput.addIfNotNull(writer, "activity_id",
				timeEntry.getActivityId());
		JsonOutput.addIfNotNull(writer, "hours", timeEntry.getHours());
		JsonOutput.addIfNotNull(writer, "comment", timeEntry.getComment());
		addIfNotNullShort(writer, "spent_on", timeEntry.getSpentOn());
		addIfNotNullFull(writer, "created_on", timeEntry.getSpentOn());
		addIfNotNullFull(writer, "updated_on", timeEntry.getSpentOn());
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

	static void writeRelation(JsonWriter writer, IssueRelation relation)
			throws IOException {
		JsonOutput.addIfNotNull(writer, "issue_to_id", relation.getIssueToId());
		JsonOutput.addIfNotNull(writer, "relation_type", relation.getType());
		JsonOutput.addIfNotNull(writer, "delay", relation.getDelay());
	}

	static void writeVersion(JsonWriter writer, Version version)
			throws IOException {
		JsonOutput.addIfNotNull(writer, "id", version.getId());
		if (version.getProject() != null)
			JsonOutput.addIfNotNull(writer, "project_id", version.getProject()
					.getId());
		JsonOutput.addIfNotNull(writer, "name", version.getName());
		JsonOutput
				.addIfNotNull(writer, "description", version.getDescription());
		JsonOutput.addIfNotNull(writer, "status", version.getStatus());
		addIfNotNullShort(writer, "due_date", version.getDueDate());
		addIfNotNullFull(writer, "created_on", version.getCreatedOn());
		addIfNotNullFull(writer, "updated_on", version.getUpdatedOn());
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

	public static void writeCategory(IssueCategory category,
			final JsonWriter writer) throws IOException {
		writer.name("id").value(category.getId());
		JsonOutput.addIfNotNull(writer, "name", category.getName());
		if (category.getProject() != null)
			JsonOutput.addIfNotNull(writer, "project_id", category.getProject()
					.getId());
		if (category.getAssignee() != null)
			JsonOutput.addIfNotNull(writer, "assigned_to_id", category
					.getAssignee().getId());
	}

	public static void writeUser(User user, final JsonWriter writer)
			throws IOException {
		JsonOutput.addIfNotNull(writer, "id", user.getId());
		JsonOutput.addIfNotNull(writer, "login", user.getLogin());
		JsonOutput.addIfNotNull(writer, "password", user.getPassword());
		JsonOutput.addIfNotNull(writer, "firstname", user.getFirstName());
		JsonOutput.addIfNotNull(writer, "lastname", user.getLastName());
		JsonOutput.addIfNotNull(writer, "name", user.getFullName());
		JsonOutput.addIfNotNull(writer, "mail", user.getMail());
		addIfNotNullFull(writer, "created_on", user.getCreatedOn());
		addIfNotNullFull(writer, "last_login_on", user.getLastLoginOn());
		writeCustomFields(writer, user.getCustomFields());

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
		addFull(writer, "start_date", issue.getStartDate());
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

		/*
		 * Journals and Relations cannot be set for an issue during creation or
		 * updates.
		 */
		/*
		 * Attachement creation is supported in API 1.4, but actual beans does
		 * not carry sufficient information to add an attachement now (token
		 * required but not provided).
		 */
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
	 * Adds a value to a writer.
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
	public static void addFull(JsonWriter writer, String field,
			Date value) throws IOException {
		final SimpleDateFormat format = RedmineDateUtils.FULL_DATE_FORMAT.get();
		JsonOutput.add(writer, field, value, format);
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
	public static void addIfNotNullShort(JsonWriter writer, String field,
			Date value) throws IOException {
		final SimpleDateFormat format = RedmineDateUtils.SHORT_DATE_FORMAT
				.get();
		JsonOutput.addIfNotNull(writer, field, value, format);
	}
}
