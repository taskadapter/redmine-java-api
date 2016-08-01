package com.taskadapter.redmineapi.internal;

import com.taskadapter.redmineapi.RedmineInternalError;
import com.taskadapter.redmineapi.bean.Attachment;
import com.taskadapter.redmineapi.bean.CustomField;
import com.taskadapter.redmineapi.bean.Group;
import com.taskadapter.redmineapi.bean.Issue;
import com.taskadapter.redmineapi.bean.IssueCategory;
import com.taskadapter.redmineapi.bean.IssueRelation;
import com.taskadapter.redmineapi.bean.Membership;
import com.taskadapter.redmineapi.bean.Project;
import com.taskadapter.redmineapi.bean.Property;
import com.taskadapter.redmineapi.bean.PropertyStorage;
import com.taskadapter.redmineapi.bean.Role;
import com.taskadapter.redmineapi.bean.TimeEntry;
import com.taskadapter.redmineapi.bean.Tracker;
import com.taskadapter.redmineapi.bean.User;
import com.taskadapter.redmineapi.bean.Version;
import com.taskadapter.redmineapi.bean.Watcher;
import com.taskadapter.redmineapi.bean.WikiPageDetail;
import com.taskadapter.redmineapi.internal.json.JsonObjectWriter;
import com.taskadapter.redmineapi.internal.json.JsonOutput;
import org.json.JSONException;
import org.json.JSONWriter;

import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Converts Redmine objects to JSon format.
 */
public class RedmineJSONBuilder {

    /**
	 * Writes a "create project" request.
	 * 
	 * @param writer
	 *            project writer.
	 * @param project
	 *            project to create.
	 * @throws IllegalArgumentException
	 *             if some project fields are not configured.
	 * @throws JSONException
	 *             if IO error occurs.
	 */
	public static void writeProject(JSONWriter writer, Project project)
			throws IllegalArgumentException, JSONException {
		/* Validate project */
		if (project.getName() == null)
			throw new IllegalArgumentException(
					"Project name must be set to create a new project");
		if (project.getIdentifier() == null)
			throw new IllegalArgumentException(
					"Project identifier must be set to create a new project");

		writeProject(project, writer);
	}

	static void writeTimeEntry(JSONWriter writer, TimeEntry timeEntry)
			throws JSONException {
		JsonOutput.addIfNotNull(writer, "id", timeEntry.getId());
		JsonOutput.addIfNotNull(writer, "project_id", timeEntry.getProjectId());
		JsonOutput.addIfNotNull(writer, "issue_id", timeEntry.getIssueId());
		JsonOutput.addIfNotNull(writer, "user_id", timeEntry.getUserId());
		JsonOutput.addIfNotNull(writer, "activity_id",
				timeEntry.getActivityId());
		JsonOutput.addIfNotNull(writer, "hours", timeEntry.getHours());
		JsonOutput.addIfNotNull(writer, "comments", timeEntry.getComment());
		addIfNotNullShort2(writer, "spent_on", timeEntry.getSpentOn());
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
	 * @throws JSONException
	 *             if error occurs.
	 */
	static void writeTracker(JSONWriter writer, Tracker tracker)
			throws JSONException {
		writer.key("id");
		writer.value(tracker.getId());
		writer.key("name");
		writer.value(tracker.getName());
	}

	static void writeRelation(JSONWriter writer, IssueRelation relation)
			throws JSONException {
		JsonOutput.addIfNotNull(writer, "issue_to_id", relation.getIssueToId());
		JsonOutput.addIfNotNull(writer, "relation_type", relation.getType());
		JsonOutput.addIfNotNull(writer, "delay", relation.getDelay());
	}

	static void writeVersion(JSONWriter writer, Version version)
			throws JSONException {
		JsonOutput.addIfNotNull(writer, "id", version.getId());
		if (version.getProject() != null)
			JsonOutput.addIfNotNull(writer, "project_id", version.getProject()
					.getId());
		JsonOutput.addIfNotNull(writer, "name", version.getName());
		JsonOutput
				.addIfNotNull(writer, "description", version.getDescription());
		JsonOutput.addIfNotNull(writer, "sharing", version.getSharing());
		JsonOutput.addIfNotNull(writer, "status", version.getStatus());
		addIfNotNullShort2(writer, "due_date", version.getDueDate());
		addIfNotNullFull(writer, "created_on", version.getCreatedOn());
		addIfNotNullFull(writer, "updated_on", version.getUpdatedOn());
		writeCustomFields(writer, version.getCustomFields());
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
		final JSONWriter jsWriter = new JSONWriter(swriter);
		try {
			jsWriter.object();
			jsWriter.key(tag);
			jsWriter.object();
			writer.write(jsWriter, object);
			jsWriter.endObject();
			jsWriter.endObject();
		} catch (JSONException e) {
			throw new RedmineInternalError("Unexpected JSONException", e);
		}

		return swriter.toString();
	}

	public static void writeProject(Project project, final JSONWriter writer)
			throws JSONException {
		PropertyStorage storage = project.getStorage();
		addIfSet(writer, "id", storage, Project.DATABASE_ID);
		addIfSet(writer, "identifier", storage, Project.STRING_IDENTIFIER);
		addIfSet(writer, "name", storage, Project.NAME);
		addIfSet(writer, "description", storage, Project.DESCRIPTION);
		addIfSet(writer, "homepage", storage, Project.HOMEPAGE);
		addIfNotNullFull(writer, "created_on", project.getCreatedOn());
		addIfNotNullFull(writer, "updated_on", project.getUpdatedOn());
		writeCustomFields(writer, project.getCustomFields());
		addIfSet(writer, "parent_id", storage, Project.PARENT_DATABASE_ID);
		addIfSet(writer, "is_public", storage, Project.PUBLIC);
		JsonOutput.addArrayIfNotNull(writer, "trackers", project.getTrackers(), RedmineJSONBuilder::writeTracker);
	}

	public static void writeCategory(final JSONWriter writer, IssueCategory category) throws JSONException {
		writer.key("id");
		writer.value(category.getId());
		JsonOutput.addIfNotNull(writer, "name", category.getName());
		if (category.getProject() != null) {
			JsonOutput.addIfNotNull(writer, "project_id", category.getProject().getId());
		}
		JsonOutput.addIfNotNull(writer, "assigned_to_id", category.getAssigneeId());
	}

	public static void writeUser(final JSONWriter writer, User user)
			throws JSONException {
		PropertyStorage storage = user.getStorage();
		addIfSet(writer, "id", storage, User.ID);
		addIfSet(writer, "login", storage, User.LOGIN);
		addIfSet(writer, "password", storage, User.PASSWORD);
		addIfSet(writer, "firstname", storage, User.FIRST_NAME);
		addIfSet(writer, "lastname", storage, User.LAST_NAME);
		// TODO I don't think this "name" is required... check this.
//		addIfSet(writer, "name", storage, User.FULL_NAME);
		addIfSet(writer, "mail", storage, User.MAIL);
		addIfSet(writer, "auth_source_id", storage, User.AUTH_SOURCE_ID);
		addIfSet(writer, "status", storage, User.STATUS);
		addIfNotNullFull(writer, "created_on", user.getCreatedOn());
		addIfNotNullFull(writer, "last_login_on", user.getLastLoginOn());
		writeCustomFields(writer, user.getCustomFields());

	}

    public static void writeGroup(final JSONWriter writer, Group group) throws JSONException {
		PropertyStorage storage = group.getStorage();
		addIfSet(writer, "id", storage, Group.ID);
		addIfSet(writer, "name", storage, Group.NAME);
	}

	public static void writeIssue(final JSONWriter writer, Issue issue) throws JSONException {
        PropertyStorage storage = issue.getStorage();
		addIfSet(writer, "id", storage, Issue.DATABASE_ID);
		addIfSet(writer, "subject", storage, Issue.SUBJECT);
		addIfSet(writer, "parent_issue_id", storage, Issue.PARENT_ID);
		addIfSet(writer, "estimated_hours", storage, Issue.ESTIMATED_HOURS);
		addIfSet(writer, "spent_hours", storage, Issue.SPENT_HOURS);
		addIfSet(writer, "assigned_to_id", storage, Issue.ASSIGNEE_ID);
		addIfSet(writer, "priority_id", storage, Issue.PRIORITY_ID);
        addIfSet(writer, "done_ratio", storage, Issue.DONE_RATIO);
		addIfSet(writer, "is_private", storage, Issue.PRIVATE_ISSUE);
        if (issue.getProject() != null) {
            // Checked in Redmine 2.6.0: updating issues based on
            // identifier fails and only using the project id works.
            // As the identifier usage is used in several places, this
            // case selection is introduced. The identifier is
            // used, if no real ID is provided
            if (issue.getProject().getId() != null) {
                JsonOutput.addIfNotNull(writer, "project_id", issue.getProject()
                        .getId());
            } else {
				throw new IllegalArgumentException("Project ID must be set on issue. " +
						"You can use a factory method to create Issue object in memory: IssueFactory.create(projectId, subject)");
            }
        }
        if (issue.getAuthor() != null)
			JsonOutput.addIfNotNull(writer, "author_id", issue.getAuthor().getId());
		addIfSet(writer, "start_date", storage, Issue.START_DATE, RedmineDateParser.SHORT_DATE_FORMAT_V2.get());
		addIfSet(writer, "due_date", storage, Issue.DUE_DATE, RedmineDateParser.SHORT_DATE_FORMAT_V2.get());
		if (issue.getTracker() != null)
			JsonOutput.addIfNotNull(writer, "tracker_id", issue.getTracker().getId());
		addIfSet(writer, "description", storage, Issue.DESCRIPTION);

		addIfNotNullFull(writer, "created_on", issue.getCreatedOn());
		addIfNotNullFull(writer, "updated_on", issue.getUpdatedOn());
		JsonOutput.addIfNotNull(writer, "status_id", issue.getStatusId());
        if (issue.getTargetVersion() != null)
            JsonOutput.addIfNotNull(writer, "fixed_version_id", issue
                    .getTargetVersion().getId());
        if (issue.getCategory() != null)
            JsonOutput.addIfNotNull(writer, "category_id", issue.getCategory().getId());
        addIfSet(writer, "notes", storage, Issue.NOTES);
		writeCustomFields(writer, issue.getCustomFields());

        Collection<Watcher> issueWatchers = issue.getWatchers();
        if (issueWatchers != null && !issueWatchers.isEmpty()) {
            writeWatchers(writer, issueWatchers);
        }

        final List<Attachment> uploads = issue.getAttachments()
				.stream()
				.filter(attachment -> attachment.getToken() != null)
				.collect(Collectors.toList());

		JsonOutput.addArrayIfNotEmpty(writer, "uploads", uploads, RedmineJSONBuilder::writeUpload);

		/*
		 * Journals and Relations cannot be set for an issue during creation or
		 * updates.
		 */
	}

	private static void addIfSet(JSONWriter writer, String jsonKeyName, PropertyStorage storage, Property<?> property) throws JSONException {
		if (storage.isPropertySet(property)) {
			writer.key(jsonKeyName);
			writer.value(storage.get(property));
		}
	}

	private static void addIfSet(JSONWriter writer, String jsonKeyName, PropertyStorage storage, Property<Date> property, SimpleDateFormat format) throws JSONException {
		if (storage.isPropertySet(property)) {
			JsonOutput.add(writer, jsonKeyName, storage.get(property), format);
		}
	}

	public static void writeUpload(JSONWriter writer, Attachment attachment)
			throws JSONException {
		JsonOutput.addIfNotNull(writer, "token", attachment.getToken());
		JsonOutput.addIfNotNull(writer, "filename", attachment.getFileName());
		JsonOutput.addIfNotNull(writer, "content_type",
				attachment.getContentType());
		JsonOutput.addIfNotNull(writer, "description", attachment.getDescription());
	}

	public static void writeMembership(JSONWriter writer, Membership membership)
			throws JSONException {
		if (membership.getUser() != null) {
            JsonOutput.addIfNotNull(writer, "user_id", membership.getUser().getId());
        }
        if (membership.getGroup() != null) {
            JsonOutput.addIfNotNull(writer, "group_id", membership.getGroup().getId());
        }
		if (membership.getRoles() != null) {
			writer.key("role_ids");
			writer.array();
			for (Role role : membership.getRoles()) {
				writer.value(role.getId().longValue());
			}
			writer.endArray();
		}
	}

	private static void writeCustomFields(JSONWriter writer,
			Collection<CustomField> customFields) throws JSONException {
		if (customFields == null || customFields.isEmpty()) {
            return;
        }
		writer.key("custom_field_values").object();
		for (CustomField field : customFields) {
            // see https://github.com/taskadapter/redmine-java-api/issues/54
            Object valueToWrite;
            if (field.isMultiple()) {
                valueToWrite = field.getValues();
            } else {
			    valueToWrite = field.getValue();
            }
            writer.key(Integer.toString(field.getId())).value(valueToWrite);
		}
		writer.endObject();
	}

        public static void writeWatchers(JSONWriter writer, Collection<Watcher> watchers)
			throws JSONException {
            if (watchers == null || watchers.isEmpty()) {
                return;
            }

            writer.key("watcher_user_ids");
            writer.array();
            for (Watcher watcher : watchers) {
                if (watcher.getId() != null) {
                    writer.value(watcher.getId().longValue());
                }
            }
            writer.endArray();
	}

	public static void writeWikiPageDetail(JSONWriter writer, WikiPageDetail detail) throws JSONException {
		JsonOutput.addIfNotNull(writer, "text", detail.getText());
		JsonOutput.addIfNotNull(writer, "comments", detail.getComments());
		JsonOutput.addIfNotNull(writer, "version", detail.getVersion());
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
	 * @throws JSONException
	 *             if io error occurs.
	 */
	public static void addIfNotNullFull(JSONWriter writer, String field,
			Date value) throws JSONException {
		final SimpleDateFormat format = RedmineDateParser.FULL_DATE_FORMAT.get();
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
	 * @throws JSONException
	 *             if io error occurs.
	 */
	public static void addFull(JSONWriter writer, String field, Date value)
			throws JSONException {
		final SimpleDateFormat format = RedmineDateParser.FULL_DATE_FORMAT.get();
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
	 * @throws JSONException
	 *             if io error occurs.
	 */
	public static void addIfNotNullShort(JSONWriter writer, String field,
			Date value) throws JSONException {
		final SimpleDateFormat format = RedmineDateParser.SHORT_DATE_FORMAT
				.get();
		JsonOutput.addIfNotNull(writer, field, value, format);
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
     * @throws JSONException
     *             if io error occurs.
     */
    public static void addIfNotNullShort2(JSONWriter writer, String field,
            Date value) throws JSONException {
        final SimpleDateFormat format = RedmineDateParser.SHORT_DATE_FORMAT_V2
                .get();
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
     * @throws JSONException
     *             if io error occurs.
     */
    public static void addShort2(JSONWriter writer, String field, Date value)
            throws JSONException {
        final SimpleDateFormat format = RedmineDateParser.SHORT_DATE_FORMAT_V2.get();
        JsonOutput.add(writer, field, value, format);
    }
}
