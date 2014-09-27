package com.taskadapter.redmineapi;

import com.taskadapter.redmineapi.bean.Project;
import com.taskadapter.redmineapi.bean.WikiPage;
import com.taskadapter.redmineapi.bean.WikiPageDetail;
import com.taskadapter.redmineapi.internal.Transport;
import org.apache.http.message.BasicNameValuePair;

import java.util.List;

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
     * @param @param projectKey the key of the project (like "TEST-12") we want the wiki page from
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
