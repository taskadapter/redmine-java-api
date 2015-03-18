package com.taskadapter.redmineapi;

import com.taskadapter.redmineapi.bean.Project;
import com.taskadapter.redmineapi.bean.WikiPage;
import com.taskadapter.redmineapi.bean.WikiPageDetail;
import com.taskadapter.redmineapi.internal.Transport;
import org.apache.http.message.BasicNameValuePair;

import java.util.List;

/**
 * Works with Wiki entries (read-only at this moment).
 * <p>Obtain it via RedmineManager:
 * <pre>
 RedmineManager redmineManager = RedmineManagerFactory.createWithUserAuth(redmineURI, login, password);
 WikiManager wikiManager = redmineManager.getWikiManager();
 * </pre>
 *
 * <p>Sample usage:
 * <pre>
 String projectKey = "projkey1410979585758";
 wikiPages = manager.getWikiPagesByProject(projectKey);
 * </pre>
 *
 * @see RedmineManager
 */
public class WikiManager {
    private final Transport transport;

    WikiManager(Transport transport) {
        this.transport = transport;
    }

    /**
     * @param projectKey the key of the project (like "TEST-12") we want the wiki pages from
     *
     * @return a list of all wiki pages for the project.
     *
     * @since Redmine 2.2
     */
    public List<WikiPage> getWikiPagesByProject(final String projectKey) throws RedmineException {
        return transport.getChildEntries(Project.class, projectKey, WikiPage.class);
    }

    /**
     * @param projectKey the key of the project (like "TEST-12") we want the wiki page from
     * @param pageTitle   The name of the page
     *
     * @return the wiki page titled with the name passed as parameter
     *
     * @since Redmine 2.2
     */
    public WikiPageDetail getWikiPageDetailByProjectAndTitle(String projectKey, String pageTitle) throws RedmineException {
        return transport.getChildEntry(Project.class, projectKey, WikiPageDetail.class, pageTitle, new BasicNameValuePair("include", "attachments"));
    }
}
