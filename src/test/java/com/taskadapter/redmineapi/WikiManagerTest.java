package com.taskadapter.redmineapi;

import com.taskadapter.redmineapi.bean.Attachment;
import com.taskadapter.redmineapi.bean.WikiPage;
import com.taskadapter.redmineapi.bean.WikiPageDetail;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class WikiManagerTest {

    private WikiManager manager;

    @Before
    public void beforeEachTest() {
        RedmineManager redmineManager = IntegrationTestHelper.createRedmineManager();
        manager = redmineManager.getWikiManager();
    }

    @Ignore("requires manual configuration, see the source code.")
    @Test
    public void getWikiPagesIndexByProject() throws Exception {
        // I created this project and some wiki pages manually because
        // Redmine's REST API for creating and updating Wiki pages is broken: http://www.redmine.org/issues/16992
        String projectKey = "projkey1410979585758";
        List<WikiPage> wikiPages = manager.getWikiPagesByProject(projectKey);
        assertThat(wikiPages.size()).isEqualTo(2);
    }

    @Ignore("requires manual configuration, see the source code.")
    @Test
    public void getSpecificWikiPageByProject() throws Exception {
        WikiPageDetail specificPage = manager.getWikiPageDetailByProjectAndTitle("projkey1410979585758", "Another");

        assertThat(specificPage.getTitle()).isEqualTo("Another");

        assertThat(specificPage.getText()).isEqualTo("this is a page too");
        assertThat(specificPage.getParent().getTitle()).isEqualTo("Wiki");
        assertThat(specificPage.getUser().getId()).isEqualTo(1);
        assertThat(specificPage.getVersion()).isEqualTo(2);
        assertThat(specificPage.getCreatedOn()).isNotNull();
        assertThat(specificPage.getUpdatedOn()).isNotNull();
        assertThat(specificPage.getAttachments()).isNotNull();
        assertThat(specificPage.getAttachments().size()).isEqualTo(1);

        Attachment attachment = specificPage.getAttachments().get(0);

        assertThat(attachment.getFileName()).isEqualTo("happy_penguin.jpg");
        assertThat(attachment.getId()).isEqualTo(8);
        assertThat(attachment.getFileSize()).isEqualTo(72158);
        assertThat(attachment.getAuthor().getLogin()).isEqualTo("Redmine Admin");
        assertThat(attachment.getContentURL()).isEqualTo("http://76.126.10.142/redmine/attachments/download/8/happy_penguin.jpg");
    }
}