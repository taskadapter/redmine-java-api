package org.redmine.ta.internal;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.redmine.ta.beans.*;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class URIConfigurator {
    private static final String XML_URL_POSTFIX = ".xml";

    private static final Map<Class, String> urls = new HashMap<Class, String>() {
        private static final long serialVersionUID = 1L;
        {
            put(User.class, "users");
            put(Issue.class, "issues");
            put(Project.class, "projects");
            put(TimeEntry.class, "time_entries");
            put(SavedQuery.class, "queries");
            put(IssueStatus.class, "issue_statuses");
            put(Version.class, "versions");
            put(IssueCategory.class, "issue_categories");
            put(Tracker.class, "trackers");
            put(Attachment.class, "attachments");
            put(News.class, "news");
        }
    };

    private final String host;
    private String apiAccessKey;

    public URIConfigurator(String host) {
        this.host = host;
    }

    public URI createURI(String query) {
        return createURI(query, new ArrayList<NameValuePair>());
    }

    public URI createURI(String query, NameValuePair... param) {
        List<NameValuePair> list = new ArrayList<NameValuePair>();
        for (NameValuePair p : param) {
            list.add(p);
        }
        return createURI(query, list);
    }

    /**
     * @param query e.g. "/issues.xml"
     * @return URI with auth parameter "key" if not in "basic auth mode.
     */
    public URI createURI(String query, List<NameValuePair> params) {
        if (apiAccessKey != null) {
            params.add(new BasicNameValuePair("key", apiAccessKey));
        }
        URI uri;
        try {
            URL url = new URL(host);
            String path = url.getPath();
            if (!query.isEmpty()) {
                path += "/" + query;
            }
            uri = URIUtils.createURI(url.getProtocol(), url.getHost(), url.getPort(), path,
                    URLEncodedUtils.format(params, "UTF-8"), null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return uri;
    }

    public URI getCreateURI(Class zz) throws MalformedURLException {
        String query = urls.get(zz) + XML_URL_POSTFIX;
        return createURI(query);
    }

    public URI getUpdateURI(Class zz, String id) {
        String query = urls.get(zz) + "/" + id + XML_URL_POSTFIX;
        return createURI(query);
    }

    public URI getUpdateURI(Class zz, String id, NameValuePair... param) {
        String query = urls.get(zz) + "/" + id + XML_URL_POSTFIX;
        return createURI(query, param);
    }

    public URI getCreateURIIssueCategory(Integer projectID) {
        return createURI("projects/" + projectID + "/issue_categories.xml");
    }

    public void setApiAccessKey(String apiAccessKey) {
        this.apiAccessKey = apiAccessKey;
    }
    
    public URI getRetrieveObjectsListURI(Class className, List<NameValuePair> param) {
        String query = urls.get(className) + XML_URL_POSTFIX;
        return createURI(query, param);
    }

    public URI getRetrieveObjectURI(Class className, Integer id, List<NameValuePair> param) {
        String query = urls.get(className) + "/" + id + XML_URL_POSTFIX;
        return createURI(query, param);
    }
}
