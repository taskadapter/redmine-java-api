package com.taskadapter.redmineapi.internal;

import com.taskadapter.redmineapi.RedmineInternalError;
import com.taskadapter.redmineapi.bean.Attachment;
import com.taskadapter.redmineapi.bean.CustomField;
import com.taskadapter.redmineapi.bean.Group;
import com.taskadapter.redmineapi.bean.Identifiable;
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
import com.taskadapter.redmineapi.bean.WikiPage;
import com.taskadapter.redmineapi.bean.WikiPageDetail;
import com.taskadapter.redmineapi.internal.json.JsonObjectWriter;
import com.taskadapter.redmineapi.internal.json.JsonOutput;
import org.json.JSONException;
import org.json.JSONWriter;

import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
		PropertyStorage storage = timeEntry.getStorage();
		addIfSet(writer, "id", storage, TimeEntry.DATABASE_ID);
		addIfSet(writer, "project_id", storage, TimeEntry.PROJECT_ID);
		addIfSet(writer, "issue_id", storage, TimeEntry.ISSUE_ID);
		addIfSet(writer, "user_id", storage, TimeEntry.USER_ID);
		addIfSet(writer, "activity_id",storage, TimeEntry.ACTIVITY_ID);
		addIfSet(writer, "hours", storage, TimeEntry.HOURS);
		addIfSet(writer, "comments", storage, TimeEntry.COMMENT);
		addIfSetShort2(writer, "spent_on", storage, TimeEntry.SPENT_ON);
		addIfSetFullDate(writer, "created_on", storage, TimeEntry.SPENT_ON);
		addIfSetFullDate(writer, "updated_on", storage, TimeEntry.SPENT_ON);
		writeCustomFields(writer, timeEntry.getCustomFields());
	}

	static void writeRelation(JSONWriter writer, IssueRelation relation)
			throws JSONException {
		PropertyStorage storage = relation.getStorage();
		if (storage.get(IssueRelation.ISSUE_TO_ID).isEmpty()) {
			throw new IllegalArgumentException("cannot create a relation object with no target issues defined.");
		}

		addIfSet(writer, "relation_type", storage, IssueRelation.RELATION_TYPE);
		addIfSet(writer, "delay", storage, IssueRelation.DELAY);

		// custom mapping for "issue_to_id" field
		writer.key("issue_to_id");
		// convert number to string to support Redmine 4.x REST API, which brought a backward incompatible change
		// in the way issue_to_id works.
		var value = storage.get(IssueRelation.ISSUE_TO_ID).stream()
				.map(number -> number+"")
				.collect(Collectors.joining(","));
		writer.value(value);
	}

	static void writeVersion(JSONWriter writer, Version version)
			throws JSONException {
		PropertyStorage storage = version.getStorage();
		addIfSet(writer, "id", storage, Version.DATABASE_ID);
		addIfSet(writer, "project_id", storage, Version.PROJECT_ID);
		addIfSet(writer, "name", storage, Version.NAME);
		addIfSet(writer, "description", storage, Version.DESCRIPTION);
		addIfSet(writer, "sharing", storage, Version.SHARING);
		addIfSet(writer, "status", storage, Version.STATUS);
		addIfSetShort2(writer, "due_date", storage, Version.DUE_DATE);
		addIfSetFullDate(writer, "created_on", storage, Version.CREATED_ON);
		addIfSetFullDate(writer, "updated_on", storage, Version.UPDATED_ON);
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
		addIfSetFullDate(writer, "created_on", storage, Project.CREATED_ON);
		addIfSetFullDate(writer, "updated_on", storage, Project.UPDATED_ON);
		writeCustomFields(writer, project.getCustomFields());
		addIfSet(writer, "parent_id", storage, Project.PARENT_DATABASE_ID);
		addIfSet(writer, "status", storage, Project.STATUS);
		addIfSet(writer, "is_public", storage, Project.PUBLIC);
		addIfSet(writer, "inherit_members", storage, Project.INHERIT_MEMBERS);
		writeProjectTrackers(writer, project);
	}
	private static void writeProjectTrackers(JSONWriter writer, Project project) throws JSONException {
		//skip if storage is not already set to allow new projects get the redmine system default trackers
		PropertyStorage storage = project.getStorage();
		if (storage.isPropertySet(Project.TRACKERS)) {
			Collection<Integer> trackerIds=new ArrayList<>();
			for (Tracker tracker : project.getTrackers())
				trackerIds.add(tracker.getId());
			JsonOutput.addScalarArray(writer, "tracker_ids", trackerIds, RedmineJSONBuilder::writeScalarValue);			
		}
	}

	static void writeScalarValue(JSONWriter writer, Object object) throws JSONException {
		writer.value(object);
	}

	public static void writeCategory(final JSONWriter writer, IssueCategory category) throws JSONException {
		PropertyStorage storage = category.getStorage();
		writer.key("id");
		writer.value(category.getId());
		addIfSet(writer, "name", storage, IssueCategory.NAME);
		addIfSet(writer, "project_id", storage, IssueCategory.PROJECT_ID);
		addIfSet(writer, "assigned_to_id", storage, IssueCategory.ASSIGNEE_ID);
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
		addIfSetFullDate(writer, "created_on", storage, User.CREATED_ON);
		addIfSetFullDate(writer, "last_login_on", storage, User.LAST_LOGIN_ON);
		addIfSet(writer, "mail_notification", storage, User.MAIL_NOTIFICATION);
		addIfSet(writer, "must_change_passwd", storage, User.MUST_CHANGE_PASSWD);
		addIfSet(writer, "generate_password", storage, User.GENERATE_PASSWORD);
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
		addIfSet(writer, "project_id", storage, Issue.PROJECT_ID);
		addIfSet(writer, "author_id", storage, Issue.AUTHOR_ID);
		addIfSet(writer, "start_date", storage, Issue.START_DATE, RedmineDateParser.SHORT_DATE_FORMAT_V2.get());
		addIfSet(writer, "due_date", storage, Issue.DUE_DATE, RedmineDateParser.SHORT_DATE_FORMAT_V2.get());
		addIfSet(writer, "actual_start_date", storage, Issue.ACTUAL_START_DATE,
				RedmineDateParser.SHORT_DATE_FORMAT_V2.get());
		addIfSet(writer, "actual_due_date", storage, Issue.ACTUAL_DUE_DATE,
				RedmineDateParser.SHORT_DATE_FORMAT_V2.get());
                addIfSetIdentifiable(writer, "tracker_id", storage, Issue.TRACKER);
		addIfSet(writer, "description", storage, Issue.DESCRIPTION);

		addIfSetFullDate(writer, "created_on", storage, Issue.CREATED_ON);
		addIfSetFullDate(writer, "updated_on", storage, Issue.UPDATED_ON);
		addIfSet(writer, "status_id", storage, Issue.STATUS_ID);
                addIfSetIdentifiable(writer, "fixed_version_id", storage, Issue.TARGET_VERSION);
                addIfSetIdentifiable(writer, "category_id", storage, Issue.ISSUE_CATEGORY);
                addIfSet(writer, "notes", storage, Issue.NOTES);
		addIfSet(writer, "private_notes", storage, Issue.PRIVATE_NOTES);
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

	public static void addIfSetShort2(JSONWriter writer, String jsonKeyName, PropertyStorage storage, Property<Date> property) throws JSONException {
		final SimpleDateFormat format = RedmineDateParser.SHORT_DATE_FORMAT_V2.get();
		addIfSet(writer, jsonKeyName, storage, property, format);
	}

	private static void addIfSetFullDate(JSONWriter writer, String jsonKeyName, PropertyStorage storage, Property<Date> property) throws JSONException {
		final SimpleDateFormat format = RedmineDateParser.FULL_DATE_FORMAT.get();
		addIfSet(writer, jsonKeyName, storage, property, format);
	}
        
        private static void addIfSetIdentifiable(JSONWriter writer, String jsonKeyName, PropertyStorage storage, Property<? extends Identifiable> property) throws JSONException {
                if (storage.isPropertySet(property)) {
                        final Identifiable propertyValue = storage.get(property);
                        writer.key(jsonKeyName);
                        if(propertyValue != null) {
                            writer.value(propertyValue.getId());
                        } else {
                            writer.value(null);
                        }
                } 
        }

	private static void addIfSet(JSONWriter writer, String jsonKeyName, PropertyStorage storage, Property<Date> property, SimpleDateFormat format) throws JSONException {
		if (storage.isPropertySet(property)) {
			JsonOutput.add(writer, jsonKeyName, storage.get(property), format);
		}
	}

	public static void writeUpload(JSONWriter writer, Attachment attachment) throws JSONException {
		PropertyStorage storage = attachment.getStorage();
		addIfSet(writer, "token", storage, Attachment.TOKEN);
		addIfSet(writer, "filename", storage, Attachment.FILE_NAME);
		addIfSet(writer, "content_type", storage, Attachment.CONTENT_TYPE);
		addIfSet(writer, "description", storage, Attachment.DESCRIPTION);
	}

	public static void writeMembership(JSONWriter writer, Membership membership)
			throws JSONException {
		final PropertyStorage storage = membership.getStorage();
		addIfSet(writer, "user_id", storage, Membership.USER_ID);
		addIfSet(writer, "group_id", storage, Membership.GROUP_ID);
		if (membership.getRoles() != null) {
			writer.key("role_ids");
			writer.array();
			for (Role role : membership.getRoles()) {
				writer.value(role.getId().longValue());
			}
			writer.endArray();
		}
	}

	private static void writeCustomFields(JSONWriter writer, Collection<CustomField> customFields) throws JSONException {
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
		PropertyStorage storage = detail.getStorage();
		addIfSet(writer, "text", storage, WikiPageDetail.TEXT);
		addIfSet(writer, "comments", storage, WikiPageDetail.COMMENTS);
		addIfSet(writer, "version", storage, WikiPage.VERSION);
		JsonOutput.addArrayIfNotEmpty(writer, "uploads", detail.getAttachments(), RedmineJSONBuilder::writeUpload);
	}
}
