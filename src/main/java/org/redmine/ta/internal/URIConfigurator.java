package org.redmine.ta.internal;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.redmine.ta.RedmineInternalError;
import org.redmine.ta.beans.*;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class URIConfigurator {
    private static final String XML_URL_POSTFIX = ".xml";

    private static final Map<Class<?>, String> urls = new HashMap<Class<?>, String>();
    
    static {
        urls.put(User.class, "users");
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
    }

    private final URL baseURL;
    private final String apiAccessKey;

    public URIConfigurator(String host, String apiAccessKey) {
        if (host == null || host.isEmpty()) {
            throw new IllegalArgumentException("The host parameter is NULL or empty");
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
     * @param query e.g. "/issues.xml"
     * @return URI with auth parameter "key" if not in "basic auth mode.
     */
    public URI createURI(String query, List<NameValuePair> params) {
        if (apiAccessKey != null) {
        	/* NEVER modify passed parameters list! It may be unmodifiable list.*/
        	params = new ArrayList<NameValuePair>(params);
            params.add(new BasicNameValuePair("key", apiAccessKey));
        }
        URI uri;
        try {
            URL url = baseURL;
            String path = url.getPath();
            if (!query.isEmpty()) {
                path += "/" + query;
            }
            uri = URIUtils.createURI(url.getProtocol(), url.getHost(), url.getPort(), path,
                    URLEncodedUtils.format(params, "UTF-8"), null);
        } catch (URISyntaxException e) {
            throw new RedmineInternalError(e);
        }
        return uri;
    }

    private String getClassUrl(Class<?> zz) {
   		final String result = urls.get(zz);
   		if (result == null) {
   			throw new IllegalArgumentException("Unsupported class " + zz.getName());
        }
   		return result;
   	}

    public URI getCreateURI(Class<?> zz) {
        String query = getClassUrl(zz) + XML_URL_POSTFIX;
        return createURI(query);
    }

    public URI getUpdateURI(Class<?> zz, String id) {
        String query = getClassUrl(zz) + "/" + id + XML_URL_POSTFIX;
        return createURI(query);
    }

    public URI getUpdateURI(Class<?> zz, String id, NameValuePair... param) {
        String query = getClassUrl(zz) + "/" + id + XML_URL_POSTFIX;
        return createURI(query, param);
    }

    public URI getCreateURIIssueCategory(Integer projectID) {
        return createURI("projects/" + projectID + "/issue_categories.xml");
    }

    public URI getRetrieveObjectsListURI(Class<?> className, List<NameValuePair> param) {
        String query = getClassUrl(className) + XML_URL_POSTFIX;
        return createURI(query, param);
    }

    public URI getRetrieveObjectURI(Class<?> className, Integer id, List<NameValuePair> param) {
        String query = getClassUrl(className) + "/" + id + XML_URL_POSTFIX;
        return createURI(query, param);
    }
}
