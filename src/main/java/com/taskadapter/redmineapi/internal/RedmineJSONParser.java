package com.taskadapter.redmineapi.internal;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.taskadapter.redmineapi.RedmineFormatException;
import com.taskadapter.redmineapi.bean.Attachment;
import com.taskadapter.redmineapi.bean.Changeset;
import com.taskadapter.redmineapi.bean.CustomField;
import com.taskadapter.redmineapi.bean.Group;
import com.taskadapter.redmineapi.bean.Issue;
import com.taskadapter.redmineapi.bean.IssueCategory;
import com.taskadapter.redmineapi.bean.IssueRelation;
import com.taskadapter.redmineapi.bean.IssueStatus;
import com.taskadapter.redmineapi.bean.Journal;
import com.taskadapter.redmineapi.bean.JournalDetail;
import com.taskadapter.redmineapi.bean.Membership;
import com.taskadapter.redmineapi.bean.News;
import com.taskadapter.redmineapi.bean.IssuePriority;
import com.taskadapter.redmineapi.bean.Project;
import com.taskadapter.redmineapi.bean.Role;
import com.taskadapter.redmineapi.bean.SavedQuery;
import com.taskadapter.redmineapi.bean.TimeEntry;
import com.taskadapter.redmineapi.bean.TimeEntryActivity;
import com.taskadapter.redmineapi.bean.Tracker;
import com.taskadapter.redmineapi.bean.User;
import com.taskadapter.redmineapi.bean.Version;
import com.taskadapter.redmineapi.bean.Watcher;
import com.taskadapter.redmineapi.internal.json.JsonInput;
import com.taskadapter.redmineapi.internal.json.JsonObjectParser;

/**
 * A parser for JSON items sent by Redmine.
 */
public class RedmineJSONParser {

	public static final JsonObjectParser<Tracker> TRACKER_PARSER = new JsonObjectParser<Tracker>() {
		@Override
		public Tracker parse(JSONObject input) throws JSONException {
			return parseTracker(input);
		}
	};

	public static final JsonObjectParser<IssueStatus> STATUS_PARSER = new JsonObjectParser<IssueStatus>() {
		@Override
		public IssueStatus parse(JSONObject input) throws JSONException {
			return parseStatus(input);
		}
	};

	public static final JsonObjectParser<Project> MINIMAL_PROJECT_PARSER = new JsonObjectParser<Project>() {
		@Override
		public Project parse(JSONObject input) throws JSONException {
			return parseMinimalProject(input);
		}
	};

	public static final JsonObjectParser<Project> PROJECT_PARSER = new JsonObjectParser<Project>() {
		@Override
		public Project parse(JSONObject input) throws JSONException {
			return parseProject(input);
		}
	};

	public static final JsonObjectParser<Issue> ISSUE_PARSER = new JsonObjectParser<Issue>() {
		@Override
		public Issue parse(JSONObject input) throws JSONException {
			return parseIssue(input);
		}
	};

	public static final JsonObjectParser<User> USER_PARSER = new JsonObjectParser<User>() {
		@Override
		public User parse(JSONObject input) throws JSONException {
			return parseUser(input);
		}
	};
	
	public static final JsonObjectParser<Group> GROUP_PARSER = new JsonObjectParser<Group>() {
		@Override
		public Group parse(JSONObject input) throws JSONException {
			return parseGroup(input);
		}
	};

	public static final JsonObjectParser<CustomField> CUSTOM_FIELD_PARSER = new JsonObjectParser<CustomField>() {
		@Override
		public CustomField parse(JSONObject input) throws JSONException {
			return parseCustomField(input);
		}
	};

	public static final JsonObjectParser<Journal> JOURNAL_PARSER = new JsonObjectParser<Journal>() {
		@Override
		public Journal parse(JSONObject input) throws JSONException {
			return parseJournal(input);
		}
	};

    public static final JsonObjectParser<JournalDetail> JOURNAL_DETAIL_PARSER = new JsonObjectParser<JournalDetail>() {
        @Override
        public JournalDetail parse(JSONObject input) throws JSONException {
            return parseJournalDetail(input);
        }
    };
    
	public static final JsonObjectParser<Attachment> ATTACHMENT_PARSER = new JsonObjectParser<Attachment>() {
		@Override
		public Attachment parse(JSONObject input) throws JSONException {
			return parseAttachments(input);
		}
	};

	public static final JsonObjectParser<IssueRelation> RELATION_PARSER = new JsonObjectParser<IssueRelation>() {
		@Override
		public IssueRelation parse(JSONObject input) throws JSONException {
			return parseRelation(input);
		}
	};

	public static final JsonObjectParser<News> NEWS_PARSER = new JsonObjectParser<News>() {
		@Override
		public News parse(JSONObject input) throws JSONException {
			return parseNews(input);
		}
	};

	public static final JsonObjectParser<Version> VERSION_PARSER = new JsonObjectParser<Version>() {
		@Override
		public Version parse(JSONObject input) throws JSONException {
			return parseVersion(input);
		}
	};

	public static final JsonObjectParser<IssueCategory> CATEGORY_PARSER = new JsonObjectParser<IssueCategory>() {
		@Override
		public IssueCategory parse(JSONObject input) throws JSONException {
			return parseCategory(input);
		}
	};

	public static final JsonObjectParser<TimeEntry> TIME_ENTRY_PARSER = new JsonObjectParser<TimeEntry>() {
		@Override
		public TimeEntry parse(JSONObject input) throws JSONException {
			return parseTimeEntry(input);
		}
	};

	public static final JsonObjectParser<SavedQuery> QUERY_PARSER = new JsonObjectParser<SavedQuery>() {
		@Override
		public SavedQuery parse(JSONObject input) throws JSONException {
			return parseSavedQuery(input);
		}
	};

	/**
	 * Parser for upload tokens.
	 */
	public static final JsonObjectParser<String> UPLOAD_TOKEN_PARSER = new JsonObjectParser<String>() {
		@Override
		public String parse(JSONObject input) throws JSONException {
			return JsonInput.getStringNotNull(input, "token");
		}
	};

	public static final JsonObjectParser<Role> ROLE_PARSER = new JsonObjectParser<Role>() {
		@Override
		public Role parse(JSONObject input) throws JSONException {
			return parseRole(input);
		}
	};

	public static final JsonObjectParser<Membership> MEMBERSHIP_PARSER = new JsonObjectParser<Membership>() {
		@Override
		public Membership parse(JSONObject input) throws JSONException {
			return parseMembership(input);
		}
	};

	public static final JsonObjectParser<String> ERROR_PARSER = new JsonObjectParser<String>() {
		@Override
		public String parse(JSONObject input) throws JSONException {
			return input.toString();
		}
	};

	public static final JsonObjectParser<Changeset> CHANGESET_PARSER = new JsonObjectParser<Changeset>() {
		@Override
		public Changeset parse(JSONObject input) throws JSONException {
			return parseChangeset(input);
		}
	};

	public static final JsonObjectParser<Watcher> WATCHER_PARSER = new JsonObjectParser<Watcher>() {
		@Override
		public Watcher parse(JSONObject input) throws JSONException {
			return parseWatcher(input);
		}
	};

    public static final JsonObjectParser<IssuePriority> ISSUE_PRIORITY_PARSER = new JsonObjectParser<IssuePriority>() {
        @Override
        public IssuePriority parse(JSONObject input) throws JSONException {
            return parseIssuePriority(input);
        }
    };
    
    public static final JsonObjectParser<TimeEntryActivity> TIME_ENTRY_ACTIVITY_PARSER = new JsonObjectParser<TimeEntryActivity>() {
        @Override
        public TimeEntryActivity parse(JSONObject input) throws JSONException {
            return parseTimeEntryActivity(input);
        }
    };
    
	/**
	 * Parses a tracker.
	 * 
	 * @param object
	 *            object to parse.
	 * @return parsed tracker.
	 * @throws RedmineFormatException
	 *             if object is not a valid tracker.
	 */
	public static Tracker parseTracker(JSONObject object) throws JSONException {
		final int id = JsonInput.getInt(object, "id");
		final String name = JsonInput.getStringNotNull(object, "name");
		return new Tracker(id, name);
	}

	/**
	 * Parses a status.
	 * 
	 * @param object
	 *            object to parse.
	 * @return parsed tracker.
	 * @throws RedmineFormatException
	 *             if object is not a valid tracker.
	 */
	public static IssueStatus parseStatus(JSONObject object)
			throws JSONException {
		final int id = JsonInput.getInt(object, "id");
		final String name = JsonInput.getStringNotNull(object, "name");
		final IssueStatus result = new IssueStatus(id, name);
		if (object.has("is_default"))
			result.setDefaultStatus(JsonInput.getOptionalBool(object,
					"is_default"));
		if (object.has("is_closed"))
			result.setClosed(JsonInput.getOptionalBool(object, "is_closed"));
		return result;
	}

	public static SavedQuery parseSavedQuery(JSONObject object)
			throws JSONException {
		final SavedQuery result = new SavedQuery();
		result.setId(JsonInput.getIntOrNull(object, "id"));
		result.setName(JsonInput.getStringOrNull(object, "name"));
		result.setPublicQuery(JsonInput.getOptionalBool(object, "is_public"));
		result.setProjectId(JsonInput.getIntOrNull(object, "project_id"));
		return result;
	}

	public static News parseNews(JSONObject object) throws JSONException {
		final News result = new News();
		result.setId(JsonInput.getIntOrNull(object, "id"));
		result.setProject(JsonInput.getObjectOrNull(object, "project",
				MINIMAL_PROJECT_PARSER));
		result.setUser(JsonInput.getObjectOrNull(object, "author", USER_PARSER));
		result.setTitle(JsonInput.getStringOrNull(object, "title"));
		result.setDescription(JsonInput.getStringOrNull(object, "description"));
		result.setCreatedOn(getDateOrNull(object, "created_on"));
		result.setLink(JsonInput.getStringOrNull(object, "link"));
		return result;
	}

	public static TimeEntry parseTimeEntry(JSONObject object)
			throws JSONException {
		/**
		 * JsonOutput.addIfNotNull(writer, "hours", timeEntry.getHours());
		 * JsonOutput.addIfNotNull(writer, "comment", timeEntry.getComment());
		 * addIfNotNullShort(writer, "spent_on", timeEntry.getSpentOn());
		 * addIfNotNullFull(writer, "created_on", timeEntry.getSpentOn());
		 * addIfNotNullFull(writer, "updated_on", timeEntry.getSpentOn());
		 */
		final TimeEntry result = new TimeEntry();
		result.setId(JsonInput.getIntOrNull(object, "id"));
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
		result.setSpentOn(getShortDateOrNull(object, "spent_on"));
		result.setCreatedOn(getDateOrNull(object, "created_on"));
		result.setUpdatedOn(getDateOrNull(object, "updated_on"));
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
		final Project result = new Project();
		result.setId(JsonInput.getInt(content, "id"));
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
		final Project result = new Project();
		result.setId(JsonInput.getInt(content, "id"));
		result.setIdentifier(JsonInput.getStringOrNull(content, "identifier"));
		result.setName(JsonInput.getStringNotNull(content, "name"));
		result.setDescription(JsonInput
				.getStringOrEmpty(content, "description"));
		result.setHomepage(JsonInput.getStringOrEmpty(content, "homepage"));
		result.setCreatedOn(getDateOrNull(content, "created_on"));
		result.setUpdatedOn(getDateOrNull(content, "updated_on"));
		final JSONObject parentProject = JsonInput.getObjectOrNull(content,
				"parent");
		if (parentProject != null)
			result.setParentId(JsonInput.getInt(parentProject, "id"));
		result.setTrackers(JsonInput.getListOrNull(content, "trackers",
				TRACKER_PARSER));
		return result;
	}

	@SuppressWarnings("deprecation")
	public static Issue parseIssue(JSONObject content) throws JSONException {
		final Issue result = new Issue();
		result.setId(JsonInput.getIntOrNull(content, "id"));
		result.setSubject(JsonInput.getStringOrNull(content, "subject"));
		final JSONObject parentIssueObject = JsonInput.getObjectOrNull(content,
				"parent");
		if (parentIssueObject != null)
			result.setParentId(JsonInput.getInt(parentIssueObject, "id"));
		result.setEstimatedHours(JsonInput.getFloatOrNull(content,
				"estimated_hours"));
		result.setSpentHours(JsonInput.getFloatOrNull(content, "spent_hours"));
		result.setAssignee(JsonInput.getObjectOrNull(content, "assigned_to",
				USER_PARSER));

		final JSONObject priorityObject = JsonInput.getObjectOrNull(content,
				"priority");
		if (priorityObject != null) {
			result.setPriorityText(JsonInput.getStringOrNull(priorityObject,
					"name"));
			result.setPriorityId(JsonInput.getIntOrNull(priorityObject, "id"));
		}

		result.setDoneRatio(JsonInput.getIntOrNull(content, "done_ratio"));
		result.setProject(JsonInput.getObjectOrNull(content, "project",
				MINIMAL_PROJECT_PARSER));
		result.setAuthor(JsonInput.getObjectOrNull(content, "author",
				USER_PARSER));
		result.setStartDate(getShortDateOrNull(content, "start_date"));
		result.setDueDate(getShortDateOrNull(content, "due_date"));
		result.setTracker(JsonInput.getObjectOrNull(content, "tracker",
				TRACKER_PARSER));
		result.setDescription(JsonInput
				.getStringOrEmpty(content, "description"));
		result.setCreatedOn(getDateOrNull(content, "created_on"));
		result.setUpdatedOn(getDateOrNull(content, "updated_on"));
		final JSONObject statusObject = JsonInput.getObjectOrNull(content,
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
				JsonInput.getListOrEmpty(content, "attachments",
						ATTACHMENT_PARSER));
		result.getRelations()
				.addAll(JsonInput.getListOrEmpty(content, "relations",
						RELATION_PARSER));
		result.setTargetVersion(JsonInput.getObjectOrNull(content,
				"fixed_version", VERSION_PARSER));
		result.setCategory(JsonInput.getObjectOrNull(content, "category",
				CATEGORY_PARSER));
		result.setChangesets(JsonInput.getListOrEmpty(content, "changesets",
				CHANGESET_PARSER));
		result.setWatchers(JsonInput.getListOrEmpty(content, "watchers",
				WATCHER_PARSER));
		return result;
	}

	public static IssueCategory parseCategory(JSONObject content)
			throws JSONException {
		final IssueCategory result = new IssueCategory();
		result.setId(JsonInput.getInt(content, "id"));
		result.setName(JsonInput.getStringOrNull(content, "name"));
		result.setProject(JsonInput.getObjectOrNull(content, "project",
				MINIMAL_PROJECT_PARSER));
		result.setAssignee(JsonInput.getObjectOrNull(content, "assigned_to",
				USER_PARSER));
		return result;
	}

	public static Version parseVersion(JSONObject content) throws JSONException {
		final Version result = new Version();
		result.setId(JsonInput.getIntOrNull(content, "id"));
		result.setProject(JsonInput.getObjectOrNull(content, "project",
				MINIMAL_PROJECT_PARSER));
		result.setName(JsonInput.getStringOrNull(content, "name"));
		result.setDescription(JsonInput.getStringOrNull(content, "description"));
		result.setStatus(JsonInput.getStringOrNull(content, "status"));
		result.setDueDate(getShortDateOrNull(content, "due_date"));
		result.setCreatedOn(getDateOrNull(content, "created_on"));
		result.setUpdatedOn(getDateOrNull(content, "updated_on"));
		return result;
	}

	public static IssueRelation parseRelation(JSONObject content)
			throws JSONException {
		final IssueRelation result = new IssueRelation();
		result.setId(JsonInput.getIntOrNull(content, "id"));
		result.setIssueId(JsonInput.getIntOrNull(content, "issue_id"));
		result.setIssueToId(JsonInput.getIntOrNull(content, "issue_to_id"));
		result.setType(JsonInput.getStringOrNull(content, "relation_type"));
		result.setDelay(JsonInput.getInt(content, "delay", 0));
		return result;
	}

	public static Attachment parseAttachments(JSONObject content)
			throws JSONException {
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

	public static CustomField parseCustomField(JSONObject content)
			throws JSONException {
		final CustomField result = new CustomField();
		result.setId(JsonInput.getInt(content, "id"));
		result.setName(JsonInput.getStringOrNull(content, "name"));

		if (!content.has("multiple"))
			result.setValue(JsonInput.getStringOrNull(content, "value"));
		else {
			JSONArray tmp = JsonInput.getArrayOrNull(content, "value");
			ArrayList<String> strings = new ArrayList<String>();
			for (int i = 0; i < tmp.length(); i++) {
				strings.add(String.valueOf(tmp.get(i)));
			}
			result.setValues(strings);
		}

		return result;
	}

	public static Journal parseJournal(JSONObject content) throws JSONException {
		final Journal result = new Journal();
		result.setId(JsonInput.getInt(content, "id"));
		result.setCreatedOn(getDateOrNull(content, "created_on"));
		result.setNotes(JsonInput.getStringOrNull(content, "notes"));
		result.setUser(JsonInput.getObjectOrNull(content, "user", USER_PARSER));
		result.setDetails(JsonInput.getListOrEmpty(content, "details", JOURNAL_DETAIL_PARSER));
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
		result.setUser(JsonInput.getObjectOrNull(content, "user", USER_PARSER));
		result.setComments(JsonInput.getStringOrNull(content, "comments"));
		result.setCommitedOn(getDateOrNull(content, "committed_on"));
		return result;
	}

	public static User parseUser(JSONObject content) throws JSONException {
		final User result = new User();
		result.setId(JsonInput.getIntOrNull(content, "id"));
		result.setLogin(JsonInput.getStringOrNull(content, "login"));
		result.setPassword(JsonInput.getStringOrNull(content, "password"));
		result.setFirstName(JsonInput.getStringOrNull(content, "firstname"));
		result.setLastName(JsonInput.getStringOrNull(content, "lastname"));
		result.setMail(JsonInput.getStringOrNull(content, "mail"));
		result.setCreatedOn(getDateOrNull(content, "created_on"));
		result.setLastLoginOn(getDateOrNull(content, "last_login_on"));
		result.setCustomFields(JsonInput.getListOrEmpty(content,
				"custom_fields", CUSTOM_FIELD_PARSER));
		final String name = JsonInput.getStringOrNull(content, "name");
		if (name != null)
			result.setFullName(name);
		result.setMemberships(JsonInput.getListOrEmpty(content, "memberships",
				MEMBERSHIP_PARSER));

		/* Fix user for membership */
		for (Membership m : result.getMemberships())
			m.setUser(result);

		return result;
	}
	
	public static Group parseGroup(JSONObject content) throws JSONException {
		final Group result = new Group();
		result.setId(JsonInput.getIntOrNull(content, "id"));
		result.setName(JsonInput.getStringOrNull(content, "name"));
		return result;
	}

	public static Role parseRole(JSONObject content) throws JSONException {
		final Role role = new Role();
		role.setId(JsonInput.getIntOrNull(content, "id"));
		role.setName(JsonInput.getStringOrNull(content, "name"));
		role.setInherited(content.has("inherited")
				&& content.getBoolean("inherited"));
		if (content.has("permissions")) {
		    final JSONArray perms = content.getJSONArray("permissions");
		    final Set<String> permSet = new HashSet<String>();
		    for (int i = 0; i < perms.length(); i++)
		        permSet.add(perms.getString(i));
		    role.setPermissions(permSet);
		}
		return role;
	}

	public static Membership parseMembership(JSONObject content)
			throws JSONException {
		final Membership result = new Membership();
		result.setId(JsonInput.getIntOrNull(content, "id"));
		result.setProject(JsonInput.getObjectOrNull(content, "project",
				MINIMAL_PROJECT_PARSER));
		result.setUser(JsonInput.getObjectOrNull(content, "user", USER_PARSER));
		result.setRoles(JsonInput.getListOrEmpty(content, "roles", ROLE_PARSER));
		return result;
	}
	
    public static IssuePriority parseIssuePriority(JSONObject content)
            throws JSONException {
        final IssuePriority result = new IssuePriority();
        result.setId(JsonInput.getInt(content, "id"));
        result.setName(JsonInput.getStringNotNull(content, "name"));
        result.setDefault(JsonInput.getOptionalBool(content, "is_default"));
        return result;
    }

    public static TimeEntryActivity parseTimeEntryActivity(JSONObject content)
            throws JSONException {
        final TimeEntryActivity result = new TimeEntryActivity();
        result.setId(JsonInput.getInt(content, "id"));
        result.setName(JsonInput.getStringNotNull(content, "name"));
        result.setDefault(JsonInput.getOptionalBool(content, "is_default"));
        return result;
    }

	public static Watcher parseWatcher(JSONObject content) throws JSONException {
		final Watcher result = new Watcher();
		result.setId(JsonInput.getIntOrNull(content, "id"));
		result.setName(JsonInput.getStringOrNull(content, "name"));
		return result;
	}
    
	/**
	 * @param responseBody
	 */
	public static List<String> parseErrors(String responseBody)
			throws JSONException {
		final JSONObject body = getResponse(responseBody);
		final JSONArray errorsList = JsonInput.getArrayNotNull(body, "errors");
		final List<String> result = new ArrayList<String>(errorsList.length());
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
	 * @throws RedmineFormatException
	 *             if value is not valid
	 */
	private static Date getDateOrNull(JSONObject obj, String field)
			throws JSONException {
		String dateStr = JsonInput.getStringOrNull(obj, field);
		if (dateStr == null)
			return null;
		try {
			if (dateStr.length() >= 5 && dateStr.charAt(4) == '/') {
				return RedmineDateUtils.FULL_DATE_FORMAT.get().parse(dateStr);
			}
			if (dateStr.endsWith("Z")) {
				dateStr = dateStr.substring(0, dateStr.length() - 1)
						+ "GMT-00:00";
			} else {
				final int inset = 6;
				if (dateStr.length() <= inset)
					throw new JSONException("Bad date value " + dateStr);
				String s0 = dateStr.substring(0, dateStr.length() - inset);
				String s1 = dateStr.substring(dateStr.length() - inset,
						dateStr.length());
				dateStr = s0 + "GMT" + s1;
			}
			return RedmineDateUtils.FULL_DATE_FORMAT_V2.get().parse(dateStr);
		} catch (ParseException e) {
			throw new JSONException("Bad date value " + dateStr);
		}
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
	private static Date getShortDateOrNull(JSONObject obj, String field)
			throws JSONException {
        final String dateStr = JsonInput.getStringOrNull(obj, field);
        if (dateStr == null) {
            return null;
        }
        final SimpleDateFormat dateFormat; 
        if (dateStr.length() >= 5 && dateStr.charAt(4) == '/')
            dateFormat = RedmineDateUtils.SHORT_DATE_FORMAT.get();
        else
            dateFormat = RedmineDateUtils.SHORT_DATE_FORMAT_V2.get();

        try {
            return dateFormat.parse(dateStr);
        } catch (ParseException e) {
            throw new JSONException("Bad date " + dateStr);
        }
	}

	public static JSONObject getResponseSingleObject(String body, String key)
			throws JSONException {
		final JSONObject bodyJson = new JSONObject(body);
		final JSONObject contentJSon = JsonInput
				.getObjectNotNull(bodyJson, key);
		return contentJSon;
	}

	public static JSONObject getResponse(String body) throws JSONException {
		return new JSONObject(body);
	}
}
