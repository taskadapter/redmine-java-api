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
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.message.BasicNameValuePair;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class URIConfigurator {
    private static final String URL_POSTFIX = ".json";

    private static final Map<Class<?>, String> urls = new HashMap<>();

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
        return createURI(query, new ArrayList<>());
    }

    public URI createURI(String query, RequestParam... param) {
        return createURI(query, Arrays.asList(param));
    }

    /**
     * @param query e.g. "/issues.xml"
     * @return URI with auth parameter "key" if not in "basic auth mode.
     */
    private URI createURI(String query,
                          Collection<? extends RequestParam> origParams) {
        Collection<? extends NameValuePair> nameValueParams = toNameValue(origParams);
        try {
            final URIBuilder builder = new URIBuilder(baseURL.toURI());
            builder.addParameters(new ArrayList<>(nameValueParams));
            //extra List creation needed because addParameters doesn't accept Collection<? extends NameValuePair>
            if (apiAccessKey != null) {
                builder.addParameter("key", apiAccessKey);
            }
            if (!query.isEmpty()) {
                builder.setPath((builder.getPath() == null? "" : builder.getPath()) + "/" + query);
            }
            return builder.build();
        } catch (URISyntaxException e) {
            throw new RedmineInternalError(e);
        }
    }

    static Collection<? extends NameValuePair> toNameValue(Collection<? extends RequestParam> origParams) {
        return origParams
                .stream()
                .filter(Objects::nonNull)
                .map(p -> new BasicNameValuePair(p.getName(), p.getValue()))
                .collect(Collectors.toSet());
    }

    public URI getChildObjectsURI(Class<?> parent, String parentId,
                                  Class<?> child, RequestParam... args) {
        final String base = getConfig(parent);
        final String detal = getConfig(child);
        return createURI(base + "/" + parentId + "/" + detal + URL_POSTFIX,
                args);
    }

    public URI getChildIdURI(Class<?> parent, String parentId,
                             Class<?> child, int value, RequestParam... params) {
        return this.getChildIdURI(parent, parentId, child, String.valueOf(value), params);
    }

    public URI getChildIdURI(Class<?> parent, String parentId,
                             Class<?> child, String value, RequestParam... params) {
        final String base = getConfig(parent);
        final String detal = getConfig(child);
        return createURI(base + "/" + parentId + "/" + detal +
                "/" + value + URL_POSTFIX, params);
    }

    public URI getObjectsURI(Class<?> child, RequestParam... params) {
        final String detal = getConfig(child);
        return createURI(detal + URL_POSTFIX, params);
    }

    public URI getObjectsURI(Class<?> child,
                             Collection<? extends RequestParam> args) {
        final String detal = getConfig(child);
        return createURI(detal + URL_POSTFIX, args);
    }

    public URI getObjectURI(Class<?> object, String id, RequestParam... params) {
        final String detal = getConfig(object);
        return createURI(detal + "/" + id + URL_POSTFIX, params);
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

    /**
     * Adds API key to URI, if the key is specified
     *
     * @param uri Original URI string
     * @return URI with API key added
     */
    public URI addAPIKey(String uri) {
        try {
            final URIBuilder builder = new URIBuilder(uri);
            if (apiAccessKey != null) {
                builder.setParameter("key", apiAccessKey);
            }
            return builder.build();
        } catch (URISyntaxException e) {
            throw new RedmineInternalError(e);
        }
    }
}
