package com.taskadapter.redmineapi.internal;

import com.taskadapter.redmineapi.RedmineInternalError;
import com.taskadapter.redmineapi.bean.Attachment;
import com.taskadapter.redmineapi.bean.CustomFieldDefinition;
import com.taskadapter.redmineapi.bean.Group;
import com.taskadapter.redmineapi.bean.Issue;
import com.taskadapter.redmineapi.bean.IssueCategory;
import com.taskadapter.redmineapi.bean.IssuePriority;
import com.taskadapter.redmineapi.bean.IssueRelation;
import com.taskadapter.redmineapi.bean.IssueStatus;
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
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class URIConfigurator {
	private static final String URL_POSTFIX = ".json";

	private static final Map<Class<?>, String> urls = new HashMap<Class<?>, String>();

	static {
		urls.put(User.class, "users");
		urls.put(Group.class, "groups");
		urls.put(Issue.class, "issues");
		urls.put(Project.class, "projects");
		urls.put(TimeEntry.class, "time_entries");
		urls.put(SavedQuery.class, "queries");
		urls.put(IssueStatus.class, "issue_statuses");
		urls.put(Version.class, "versions");
		urls.put(IssueCategory.class, "issue_categories");
		urls.put(Tracker.class, "trackers");
		urls.put(Attachment.class, "attachments");
		urls.put(News.class, "news");
		urls.put(IssueRelation.class, "relations");
		urls.put(Role.class, "roles");
		urls.put(Membership.class, "memberships");
		urls.put(IssuePriority.class, "enumerations/issue_priorities");
        urls.put(TimeEntryActivity.class, "enumerations/time_entry_activities");
		urls.put(Watcher.class, "watchers");
        urls.put(WikiPage.class, "wiki/index");
        urls.put(WikiPageDetail.class, "wiki");
                urls.put(CustomFieldDefinition.class, "custom_fields");
	}

	private final URL baseURL;
	private final String apiAccessKey;

	public URIConfigurator(String host, String apiAccessKey) {
		if (host == null || host.isEmpty()) {
			throw new IllegalArgumentException(
					"The host parameter is NULL or empty");
		}
		try {
			this.baseURL = new URL(host);
		} catch (MalformedURLException e) {
			throw new IllegalArgumentException("Illegal host URL " + host, e);
		}
		this.apiAccessKey = apiAccessKey;
	}

	public URI createURI(String query) {
		return createURI(query, new ArrayList<NameValuePair>());
	}

	public URI createURI(String query, NameValuePair... param) {
		return createURI(query, Arrays.asList(param));
	}

	/**
	 * @param query
	 *            e.g. "/issues.xml"
	 * @return URI with auth parameter "key" if not in "basic auth mode.
	 */
	private URI createURI(String query,
			Collection<? extends NameValuePair> origParams) {
		final List<NameValuePair> params = new ArrayList<NameValuePair>(
				origParams);
		if (apiAccessKey != null) {
			params.add(new BasicNameValuePair("key", apiAccessKey));
		}
		URI uri;
		try {
			URL url = baseURL;
			String path = url.getPath();
			if (!query.isEmpty()) {
				path += "/" + query;
			}
			uri = URIUtils.createURI(url.getProtocol(), url.getHost(),
					url.getPort(), path,
					URLEncodedUtils.format(params, "UTF-8"), null);
		} catch (URISyntaxException e) {
			throw new RedmineInternalError(e);
		}
		return uri;
	}

	public URI getChildObjectsURI(Class<?> parent, String parentId,
			Class<?> child, NameValuePair... args) {
		final String base = getConfig(parent);
		final String detal = getConfig(child);
		return createURI(base + "/" + parentId + "/" + detal + URL_POSTFIX,
				args);
	}

    public URI getChildIdURI(Class<?> parent, String parentId,
                             Class<?> child, int value, NameValuePair... params) {
        return this.getChildIdURI(parent, parentId, child, String.valueOf(value), params);
    }

    public URI getChildIdURI(Class<?> parent, String parentId,
                             Class<?> child, String value, NameValuePair... params) {
        final String base = getConfig(parent);
        final String detal = getConfig(child);
        return createURI(base + "/" + parentId + "/" + detal +
                "/" + value + URL_POSTFIX, params);
    }

	public URI getObjectsURI(Class<?> child, NameValuePair... args) {
		final String detal = getConfig(child);
		return createURI(detal + URL_POSTFIX, args);
	}

	public URI getObjectsURI(Class<?> child,
			Collection<? extends NameValuePair> args) {
		final String detal = getConfig(child);
		return createURI(detal + URL_POSTFIX, args);
	}

	public URI getObjectURI(Class<?> object, String id, NameValuePair... args) {
		final String detal = getConfig(object);
		return createURI(detal + "/" + id + URL_POSTFIX, args);
	}

	private String getConfig(Class<?> item) {
		final String guess = urls.get(item);
		if (guess == null)
			throw new RedmineInternalError("Unsupported item class "
					+ item.getCanonicalName());
		return guess;
	}

	public URI getUploadURI() {
		return createURI("uploads" + URL_POSTFIX);
	}
}
