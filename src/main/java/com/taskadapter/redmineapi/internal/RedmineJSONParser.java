package com.taskadapter.redmineapi.internal;

import com.taskadapter.redmineapi.bean.Attachment;
import com.taskadapter.redmineapi.bean.Changeset;
import com.taskadapter.redmineapi.bean.CustomField;
import com.taskadapter.redmineapi.bean.CustomFieldDefinition;
import com.taskadapter.redmineapi.bean.File;
import com.taskadapter.redmineapi.bean.Group;
import com.taskadapter.redmineapi.bean.Issue;
import com.taskadapter.redmineapi.bean.IssueCategory;
import com.taskadapter.redmineapi.bean.IssuePriority;
import com.taskadapter.redmineapi.bean.IssueRelation;
import com.taskadapter.redmineapi.bean.IssueStatus;
import com.taskadapter.redmineapi.bean.Journal;
import com.taskadapter.redmineapi.bean.JournalDetail;
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
import com.taskadapter.redmineapi.internal.json.JsonInput;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A parser for JSON items sent by Redmine.
 */
public final class RedmineJSONParser {

	/**
	 * Parses a tracker.
	 *
	 * @param object
	 *            object to parse.
	 * @return parsed tracker.
	 */
	public static Tracker parseTracker(JSONObject object) throws JSONException {
		final int id = JsonInput.getInt(object, "id");
		final String name = JsonInput.getStringNotNull(object, "name");
		return new Tracker().setId(id).setName(name);
	}

	/**
	 * Parses a status.
	 *
	 * @param object
	 *            object to parse.
	 * @return parsed tracker.
	 */
	public static IssueStatus parseStatus(JSONObject object)
			throws JSONException {
		final int id = JsonInput.getInt(object, "id");
		final String name = JsonInput.getStringNotNull(object, "name");
		final IssueStatus result = new IssueStatus().setId(id).setName(name);
		if (object.has("is_default"))
			result.setDefaultStatus(JsonInput.getOptionalBool(object,
					"is_default"));
		if (object.has("is_closed"))
			result.setClosed(JsonInput.getOptionalBool(object, "is_closed"));
		return result;
	}

	public static SavedQuery parseSavedQuery(JSONObject object)
			throws JSONException {
		return new SavedQuery().setId(JsonInput.getIntOrNull(object, "id"))
				.setName(JsonInput.getStringOrNull(object, "name"))
				.setPublicQuery(JsonInput.getOptionalBool(object, "is_public"))
				.setProjectId(JsonInput.getIntOrNull(object, "project_id"));
	}

	public static News parseNews(JSONObject object) throws JSONException {
		final News result = new News().setId(JsonInput.getIntOrNull(object, "id"));
		result.setProject(JsonInput.getObjectOrNull(object, "project", RedmineJSONParser::parseMinimalProject));
		result.setUser(JsonInput.getObjectOrNull(object, "author", RedmineJSONParser::parseUser));
		result.setTitle(JsonInput.getStringOrNull(object, "title"));
		result.setDescription(JsonInput.getStringOrNull(object, "description"));
		result.setCreatedOn(getDateOrNull(object, "created_on"));
		result.setLink(JsonInput.getStringOrNull(object, "link"));
		return result;
	}

	public static TimeEntry parseTimeEntry(JSONObject object)
			throws JSONException {
		TimeEntry result = new TimeEntry(null).setId(JsonInput.getIntOrNull(object, "id"));
		final JSONObject issueObject = JsonInput.getObjectOrNull(object,
				"issue");
		if (issueObject != null)
			result.setIssueId(JsonInput.getIntOrNull(issueObject, "id"));
		final JSONObject projectObject = JsonInput.getObjectOrNull(object,
				"project");
		if (projectObject != null) {
			result.setProjectId(JsonInput.getIntOrNull(projectObject, "id"));
			result.setProjectName(JsonInput.getStringOrNull(projectObject,
					"name"));
		}
		final JSONObject user = JsonInput.getObjectOrNull(object, "user");
		if (user != null) {
			result.setUserId(JsonInput.getIntOrNull(user, "id"));
			result.setUserName(JsonInput.getStringOrNull(user, "name"));
		}
		final JSONObject activity = JsonInput.getObjectOrNull(object,
				"activity");
		if (activity != null) {
			result.setActivityId(JsonInput.getIntOrNull(activity, "id"));
			result.setActivityName(JsonInput.getStringOrNull(activity, "name"));
		}
		result.setHours(JsonInput.getFloatOrNull(object, "hours"));
		result.setComment(JsonInput.getStringOrEmpty(object, "comments"));
		result.setSpentOn(getDateOrNull(object, "spent_on"));
		result.setCreatedOn(getDateOrNull(object, "created_on"));
		result.setUpdatedOn(getDateOrNull(object, "updated_on"));
		result.addCustomFields(JsonInput.getListOrEmpty(object, "custom_fields", RedmineJSONParser::parseCustomField));
		return result;
	}

	/**
	 * Parses a "minimal" version of a project.
	 *
	 * @param content
	 *            content to parse.
	 * @return parsed project.
	 */
	public static Project parseMinimalProject(JSONObject content)
			throws JSONException {
		final Project result = new Project(null).setId(JsonInput.getInt(content, "id"));
		result.setIdentifier(JsonInput.getStringOrNull(content, "identifier"));
		result.setName(JsonInput.getStringNotNull(content, "name"));
		return result;
	}

	/**
	 * Parses a project.
	 *
	 * @param content
	 *            content to parse.
	 * @return parsed project.
	 */
	public static Project parseProject(JSONObject content) throws JSONException {
		final Project result = new Project(null).setId(JsonInput.getInt(content, "id"));
		result.setIdentifier(JsonInput.getStringOrNull(content, "identifier"));
		result.setName(JsonInput.getStringNotNull(content, "name"));
		result.setProjectPublic(JsonInput.getOptionalBool(content, "is_public"));
		result.setDescription(JsonInput
				.getStringOrEmpty(content, "description"));
		result.setHomepage(JsonInput.getStringOrEmpty(content, "homepage"));
		result.setCreatedOn(getDateOrNull(content, "created_on"));
		result.setUpdatedOn(getDateOrNull(content, "updated_on"));
		final JSONObject parentProject = JsonInput.getObjectOrNull(content,
				"parent");
		if (parentProject != null)
			result.setParentId(JsonInput.getInt(parentProject, "id"));
		result.setStatus(JsonInput.getIntOrNull(content, "status"));
		result.addTrackers(JsonInput.getListOrEmpty(content, "trackers", RedmineJSONParser::parseTracker));
        result.addCustomFields(JsonInput.getListOrEmpty(content, "custom_fields", RedmineJSONParser::parseCustomField));
		return result;
	}

	@SuppressWarnings("deprecation")
	public static Issue parseIssue(JSONObject content) throws JSONException {
		final Issue result = new Issue().setId(JsonInput.getIntOrNull(content, "id"))
			.setSubject(JsonInput.getStringOrNull(content, "subject"));
		final JSONObject parentIssueObject = JsonInput.getObjectOrNull(content,
				"parent");
		if (parentIssueObject != null)
			result.setParentId(JsonInput.getInt(parentIssueObject, "id"));
		result.setEstimatedHours(JsonInput.getFloatOrNull(content,
				"estimated_hours"));
		result.setSpentHours(JsonInput.getFloatOrNull(content, "spent_hours"));
		JSONObject assignedToObject = JsonInput.getObjectOrNull(content, "assigned_to");
		if (assignedToObject != null) {
			result.setAssigneeId(JsonInput.getIntOrNull(assignedToObject, "id"));
			result.setAssigneeName(JsonInput.getStringNotNull(assignedToObject, "name"));
		}

		final JSONObject priorityObject = JsonInput.getObjectOrNull(content,
				"priority");
		if (priorityObject != null) {
			result.setPriorityText(JsonInput.getStringOrNull(priorityObject,
					"name"));
			result.setPriorityId(JsonInput.getIntOrNull(priorityObject, "id"));
		}

		result.setDoneRatio(JsonInput.getIntOrNull(content, "done_ratio"));
		final Project project = JsonInput.getObjectOrNull(content, "project", RedmineJSONParser::parseMinimalProject);
		if (project != null) {
			result.setProjectId(project.getId())
					.setProjectName(project.getName());
		}
		final User author = JsonInput.getObjectOrNull(content, "author", RedmineJSONParser::parseUser);
		if (author != null) {
			result.setAuthorId(author.getId());
			result.setAuthorName(author.getFullName());
		}
		result.setStartDate(getDateOrNull(content, "start_date"));
		result.setDueDate(getDateOrNull(content, "due_date"));
		result.setTracker(JsonInput.getObjectOrNull(content, "tracker", RedmineJSONParser::parseTracker));
		result.setDescription(JsonInput.getStringOrNull(content, "description"));
		result.setCreatedOn(getDateOrNull(content, "created_on"));
		result.setUpdatedOn(getDateOrNull(content, "updated_on"));
		result.setClosedOn(getDateOrNull(content, "closed_on"));
		final JSONObject statusObject = JsonInput.getObjectOrNull(content,
				"status");
		if (statusObject != null) {
			result.setStatusName(JsonInput
					.getStringOrNull(statusObject, "name"));
			result.setStatusId(JsonInput.getIntOrNull(statusObject, "id"));
		}

		result.setPrivateIssue(JsonInput.getOptionalBool(content, "is_private"));

		result.addCustomFields(JsonInput.getListOrEmpty(content,
				"custom_fields", RedmineJSONParser::parseCustomField));
		result.setNotes(JsonInput.getStringOrNull(content, "notes"));
		result.addJournals(JsonInput.getListOrEmpty(content, "journals", RedmineJSONParser::parseJournal));
		result.addAttachments(
				JsonInput.getListOrEmpty(content, "attachments",
						RedmineJSONParser::parseAttachments));
		result.addRelations(JsonInput.getListOrEmpty(content, "relations", RedmineJSONParser::parseRelation));
		result.setTargetVersion(JsonInput.getObjectOrNull(content, "fixed_version", RedmineJSONParser::parseVersion));
		result.setCategory(JsonInput.getObjectOrNull(content, "category",
				RedmineJSONParser::parseCategory));
		result.addChangesets(JsonInput.getListOrEmpty(content, "changesets", RedmineJSONParser::parseChangeset));
		result.addWatchers(JsonInput.getListOrEmpty(content, "watchers", RedmineJSONParser::parseWatcher));
		result.addChildren(JsonInput.getListOrEmpty(content, "children", RedmineJSONParser::parseIssue));
		return result;
	}

	public static IssueCategory parseCategory(JSONObject content)
			throws JSONException {
		final IssueCategory result = new IssueCategory(null).setId(JsonInput.getInt(content, "id"));
		result.setName(JsonInput.getStringOrNull(content, "name"));
		final Project project = JsonInput.getObjectOrNull(content, "project", RedmineJSONParser::parseMinimalProject);
		if (project != null) {
			result.setProjectId(project.getId());
		}
		JSONObject assignedToObject = JsonInput.getObjectOrNull(content, "assigned_to");
		if (assignedToObject != null) {
			result.setAssigneeId(JsonInput.getIntOrNull(assignedToObject, "id"));
			result.setAssigneeName(JsonInput.getStringOrNull(assignedToObject, "name"));
		}
		return result;
	}

	public static Version parseVersion(JSONObject content) throws JSONException {
		Version result = new Version().setId(JsonInput.getIntOrNull(content, "id"));
		Project project = JsonInput.getObjectOrNull(content, "project", RedmineJSONParser::parseMinimalProject);
		if (project != null) {
			result.setProjectId(project.getId());
			result.setProjectName(project.getName());
		}
		result.setName(JsonInput.getStringOrNull(content, "name"));
		result.setDescription(JsonInput.getStringOrNull(content, "description"));
		result.setSharing(JsonInput.getStringOrNull(content, "sharing"));
		result.setStatus(JsonInput.getStringOrNull(content, "status"));
		result.setDueDate(getDateOrNull(content, "due_date"));
		result.setCreatedOn(getDateOrNull(content, "created_on"));
		result.setUpdatedOn(getDateOrNull(content, "updated_on"));
		result.addCustomFields(JsonInput.getListOrEmpty(content, "custom_fields", RedmineJSONParser::parseCustomField));
		return result;
	}

	public static IssueRelation parseRelation(JSONObject content)
			throws JSONException {
		final IssueRelation result = new IssueRelation(null).setId(JsonInput.getIntOrNull(content, "id"));
		result.setIssueId(JsonInput.getIntOrNull(content, "issue_id"));
		result.addIssueToId(JsonInput.getIntOrNull(content, "issue_to_id"));
		result.setType(JsonInput.getStringOrNull(content, "relation_type"));
		result.setDelay(JsonInput.getInt(content, "delay", 0));
		return result;
	}

	public static Attachment parseAttachments(JSONObject content)
			throws JSONException {
		return new Attachment(null).setId(JsonInput.getIntOrNull(content, "id"))
				.setFileName(JsonInput.getStringOrNull(content, "filename"))
				.setFileSize(JsonInput.getLong(content, "filesize"))
				.setContentType(JsonInput.getStringOrNull(content, "content_type"))
				.setContentURL(JsonInput.getStringOrNull(content, "content_url"))
				.setDescription(JsonInput.getStringOrNull(content, "description"))
				.setCreatedOn(getDateOrNull(content, "created_on"))
				.setAuthor(JsonInput.getObjectOrNull(content, "author", RedmineJSONParser::parseUser));
	}

	public static CustomField parseCustomField(JSONObject content)
			throws JSONException {
		final CustomField result = new CustomField().setId(JsonInput.getInt(content, "id"));
		result.setName(JsonInput.getStringOrNull(content, "name"));

		if (!content.has("multiple")) {
			result.setValue(JsonInput.getStringOrNull(content, "value"));
                } else {
                        ArrayList<String> strings = new ArrayList<>();
                        Object value = content.get("value");
                        if(value instanceof JSONArray) {
                            JSONArray tmp = (JSONArray) value;
                            for (int i = 0; i < tmp.length(); i++) {
                                    strings.add(String.valueOf(tmp.get(i)));
                            }
                        } else {
                            // Known issue: Under the condition:
                            // - issue is newly created
                            // - custom_field is multi-field
                            // - custom_field is set to an empty list or null
                            // Then:
                            // - the return structure has "multiple" set
                            // - but the value is the empty string
                        }
                        result.setValues(strings);
		}

		return result;
	}

	public static Journal parseJournal(JSONObject content) throws JSONException {
		final Journal result = new Journal().setId(JsonInput.getInt(content, "id"));
		result.setCreatedOn(getDateOrNull(content, "created_on"));
		result.setNotes(JsonInput.getStringOrNull(content, "notes"));
		result.setUser(JsonInput.getObjectOrNull(content, "user", RedmineJSONParser::parseUser));
		result.addDetails(JsonInput.getListOrEmpty(content, "details", RedmineJSONParser::parseJournalDetail));
		return result;
	}

	public static JournalDetail parseJournalDetail(JSONObject content) throws JSONException {
	    final JournalDetail result = new JournalDetail();
	    result.setNewValue(JsonInput.getStringOrNull(content, "new_value"));
        result.setOldValue(JsonInput.getStringOrNull(content, "old_value"));
        result.setName(JsonInput.getStringOrNull(content, "name"));
        result.setProperty(JsonInput.getStringOrNull(content, "property"));
        return result;
	}

	public static Changeset parseChangeset(JSONObject content)
			throws JSONException {
		final Changeset result = new Changeset();
		result.setRevision(JsonInput.getStringOrNull(content, "revision"));
		result.setUser(JsonInput.getObjectOrNull(content, "user", RedmineJSONParser::parseUser));
		result.setComments(JsonInput.getStringOrNull(content, "comments"));
		result.setCommittedOn(getDateOrNull(content, "committed_on"));
		return result;
	}

	public static User parseUser(JSONObject content) throws JSONException {
		final User result = new User(null).setId(JsonInput.getIntOrNull(content, "id"));
		result.setLogin(JsonInput.getStringOrNull(content, "login"));
		result.setPassword(JsonInput.getStringOrNull(content, "password"));
		result.setFirstName(JsonInput.getStringOrNull(content, "firstname"));
		result.setLastName(JsonInput.getStringOrNull(content, "lastname"));
		result.setMail(JsonInput.getStringOrNull(content, "mail"));
		result.setAuthSourceId(JsonInput.getIntOrNull(content, "auth_source_id"));
		result.setCreatedOn(getDateOrNull(content, "created_on"));
		result.setLastLoginOn(getDateOrNull(content, "last_login_on"));
                result.setApiKey(JsonInput.getStringOrNull(content, "api_key"));
		result.addCustomFields(JsonInput.getListOrEmpty(content,
				"custom_fields", RedmineJSONParser::parseCustomField));
		result.setStatus(JsonInput.getIntOrNull(content, "status"));
		final String name = JsonInput.getStringOrNull(content, "name");
		if (name != null)
			result.setFullName(name);
		result.addMemberships(JsonInput.getListOrEmpty(content, "memberships",
				RedmineJSONParser::parseMembership));
		result.addGroups(JsonInput.getListOrEmpty(content, "groups",
				RedmineJSONParser::parseGroup));
		/* Fix user for membership */
		for (Membership m : result.getMemberships())
			m.setUserId(result.getId());

		return result;
	}

	public static Group parseGroup(JSONObject content) throws JSONException {
		final Group result = new Group(null).setId(JsonInput.getIntOrNull(content, "id"));
		result.setName(JsonInput.getStringOrNull(content, "name"));
		return result;
	}

	public static Role parseRole(JSONObject content) throws JSONException {
		final Role role = new Role().setId(JsonInput.getIntOrNull(content, "id"));
		role.setName(JsonInput.getStringOrNull(content, "name"));
		role.setInherited(content.has("inherited")
				&& content.getBoolean("inherited"));
		if (content.has("permissions")) {
		    final JSONArray perms = content.getJSONArray("permissions");
		    final Set<String> permSet = new HashSet<>();
		    for (int i = 0; i < perms.length(); i++)
		        permSet.add(perms.getString(i));
		    role.addPermissions(permSet);
		}
		return role;
	}

	public static Membership parseMembership(JSONObject content)
			throws JSONException {
		final Membership result = new Membership(null).setId(JsonInput.getIntOrNull(content, "id"));
		result.setProject(JsonInput.getObjectOrNull(content, "project",
				RedmineJSONParser::parseMinimalProject));
		final User user = JsonInput.getObjectOrNull(content, "user", RedmineJSONParser::parseUser);
		if (user != null) {
			result.setUserId(user.getId());
                        result.setUserName(user.getFullName());
		}
		final Group group = JsonInput.getObjectOrNull(content, "group", RedmineJSONParser::parseGroup);
		if (group != null) {
			result.setGroupId(group.getId());
                        result.setGroupName(group.getName());
		}
		result.addRoles(JsonInput.getListOrEmpty(content, "roles", RedmineJSONParser::parseRole));
		return result;
	}

    public static IssuePriority parseIssuePriority(JSONObject content)
            throws JSONException {
        final IssuePriority result = new IssuePriority().setId(JsonInput.getInt(content, "id"));
        result.setName(JsonInput.getStringNotNull(content, "name"));
        result.setDefault(JsonInput.getOptionalBool(content, "is_default"));
        return result;
    }

	public static TimeEntryActivity parseTimeEntryActivity(JSONObject content)
			throws JSONException {
		return new TimeEntryActivity()
				.setId(JsonInput.getInt(content, "id"))
				.setName(JsonInput.getStringNotNull(content, "name"))
				.setDefault(JsonInput.getOptionalBool(content, "is_default"));
	}

	public static Watcher parseWatcher(JSONObject content) throws JSONException {
		final Watcher result = new Watcher().setId(JsonInput.getIntOrNull(content, "id"));
		result.setName(JsonInput.getStringOrNull(content, "name"));
		return result;
	}
        
    public static WikiPage parseWikiPage(JSONObject object) throws JSONException {
        WikiPage wikiPage = new WikiPage().setTitle(JsonInput.getStringNotNull(object, "title"));
        wikiPage.setVersion(JsonInput.getIntOrNull(object, "version"));
        wikiPage.setCreatedOn(getDateOrNull(object, "created_on"));
        wikiPage.setUpdatedOn(getDateOrNull(object, "updated_on"));
        return wikiPage;
    }

    public static WikiPageDetail parseWikiPageDetail(JSONObject object) throws JSONException {
        WikiPageDetail wikiPage = new WikiPageDetail(null);

        wikiPage.setTitle(JsonInput.getStringOrEmpty(object, "title"));
        wikiPage.setText(JsonInput.getStringOrEmpty(object, "text"));
        wikiPage.setParent(JsonInput.getObjectOrNull(object, "parent", RedmineJSONParser::parseWikiPageDetail));
        wikiPage.setUser(JsonInput.getObjectOrNull(object, "author", RedmineJSONParser::parseUser));
        wikiPage.setVersion(JsonInput.getIntOrNull(object, "version"));
        wikiPage.setCreatedOn(getDateOrNull(object, "created_on"));
        wikiPage.setUpdatedOn(getDateOrNull(object, "updated_on"));
		wikiPage.setComments(JsonInput.getStringOrEmpty(object, "comments"));
        wikiPage.setAttachments(JsonInput.getListOrNull(object, "attachments", RedmineJSONParser::parseAttachments));

        return wikiPage;
    }

	public static List<String> parseErrors(String responseBody) throws JSONException {
		final JSONObject body = getResponse(responseBody);
		final JSONArray errorsList = JsonInput.getArrayNotNull(body, "errors");
		final List<String> result = new ArrayList<>(errorsList.length());
		for (int i = 0; i < errorsList.length(); i++) {
			result.add(errorsList.get(i).toString());
		}
		return result;
	}

	/**
	 * Fetches an optional date from an object.
	 *
	 * @param obj
	 *            object to get a field from.
	 * @param field
	 *            field to get a value from.
	 */
	private static Date getDateOrNull(JSONObject obj, String field) throws JSONException {
		String dateStr = JsonInput.getStringOrNull(obj, field);
		if (dateStr == null) {
            return null;
        }
		try {
			return RedmineDateParser.parse(dateStr);
		} catch (ParseException e) {
			throw new JSONException("Cannot parse this date: " + dateStr);
		}
	}

	public static JSONObject getResponseSingleObject(String body, String key) throws JSONException {
		final JSONObject bodyJson = new JSONObject(body);
        return JsonInput.getObjectNotNull(bodyJson, key);
	}

	public static JSONObject getResponse(String body) throws JSONException {
		return new JSONObject(body);
	}

        public static CustomFieldDefinition parseCustomFieldDefinition(JSONObject content)
                throws JSONException {
            final CustomFieldDefinition result = new CustomFieldDefinition()
					.setId(JsonInput.getInt(content, "id"));
            result.setName(JsonInput.getStringOrNull(content, "name"));
            result.setCustomizedType(JsonInput.getStringNotNull(content, "customized_type"));
            result.setFieldFormat(JsonInput.getStringNotNull(content, "field_format"));
            result.setRegexp(JsonInput.getStringOrEmpty(content, "regexp"));
            result.setMinLength(JsonInput.getIntOrNull(content, "min_length"));
            result.setMaxLength(JsonInput.getIntOrNull(content, "max_length"));
            result.setRequired(content.optBoolean("is_required"));
            result.setFilter(content.optBoolean("is_filter"));
            result.setSearchable(content.optBoolean("searchable"));
            result.setMultiple(content.optBoolean("multiple"));
            result.setDefaultValue(JsonInput.getStringOrEmpty(content, "default_value"));
            result.setVisible(content.optBoolean("visible"));
            if (content.has("possible_values")) {
                JSONArray possible_values = content.getJSONArray("possible_values");
                for (int i = 0; i < possible_values.length(); i++) {
                    JSONObject valueObject = possible_values.getJSONObject(i);
                    result.getPossibleValues().add(valueObject.getString("value"));
                }
            }
            if (content.has("trackers")) {
                JSONArray possible_values = content.getJSONArray("trackers");
                for (int i = 0; i < possible_values.length(); i++) {
                    JSONObject valueObject = possible_values.getJSONObject(i);
                    int id = valueObject.getInt("id");
                    String name = valueObject.getString("name");
                    result.getTrackers().add(
                    		new Tracker().setId(id).setName(name)
					);
                }
            }
            if (content.has("roles")) {
                JSONArray possible_values = content.getJSONArray("roles");
                for (int i = 0; i < possible_values.length(); i++) {
                    JSONObject valueObject = possible_values.getJSONObject(i);
                    int id = valueObject.getInt("id");
                    String name = valueObject.getString("name");
					Role role = new Role().setId(id)
							.setName(name);
					result.getRoles().add(role);
                }
            }
            return result;
        }

  public static File parseFiles(JSONObject content) {
    return new File(null)
        .setId(content.getInt("id"))
        .setFileName(content.getString("filename"))
        .setFileSize(content.getLong("filesize"))
        .setContentType(JsonInput.getStringOrNull(content,"content_type"))
        .setDescription(content.optString("description"))
        .setContentURL(content.getString("content_url"))
        .setAuthor(JsonInput.getObjectOrNull(content, "author", RedmineJSONParser::parseUser))
        .setCreatedOn(getDateOrNull(content, "created_on"))
        .setVersion(JsonInput.getObjectOrNull(content, "version", RedmineJSONParser::parseVersion))
        .setDigest(content.getString("digest"))
        .setDownloads(content.optInt("downloads"));
  }
}
