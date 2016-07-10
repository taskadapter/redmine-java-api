package com.taskadapter.redmineapi;

import com.taskadapter.redmineapi.bean.Attachment;
import com.taskadapter.redmineapi.bean.Project;
import com.taskadapter.redmineapi.bean.User;
import com.taskadapter.redmineapi.bean.WikiPage;
import com.taskadapter.redmineapi.bean.WikiPageDetail;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class WikiManagerIT {

    private static RedmineManager redmineManager;
    private static Project project;
    private static String projectKey;

    private static WikiManager manager;
    private static User currentUser;

    @BeforeClass
    public static void beforeClass() throws RedmineException {
        redmineManager = IntegrationTestHelper.createRedmineManager();
        project = IntegrationTestHelper.createProject(redmineManager);
        projectKey = project.getIdentifier();
        manager = redmineManager.getWikiManager();
        UserManager userManager = redmineManager.getUserManager();
        currentUser = userManager.getCurrentUser();
    }

    @AfterClass
    public static void oneTimeTearDown() {
        IntegrationTestHelper.deleteProject(redmineManager, project.getIdentifier());
    }

    @Test
    public void wikiPageIsCreated() throws Exception {
        WikiPageDetail wikiPageDetail = createSomeWikiPage();
        WikiPageDetail loaded = manager.getWikiPageDetailByProjectAndTitle(projectKey, wikiPageDetail.getTitle());
        String title = wikiPageDetail.getTitle();
        String urlSafeTitleIsExpected = URLEncoder.encode(title, StandardCharsets.UTF_8.name());
        assertThat(loaded.getTitle()).isEqualToIgnoringCase(urlSafeTitleIsExpected);
        assertThat(loaded.getText()).isEqualTo(wikiPageDetail.getText());
        assertThat(loaded.getUser().getId()).isEqualTo(currentUser.getId());
        assertThat(loaded.getCreatedOn()).isNotNull();
        assertThat(loaded.getUpdatedOn()).isNotNull();
    }

    @Test
    public void getWikiPagesIndexByProject() throws Exception {
        createSomeWikiPage();
        createSomeWikiPage();
        List<WikiPage> wikiPages = manager.getWikiPagesByProject(projectKey);
        assertThat(wikiPages.size()).isGreaterThan(1);
    }

    @Ignore("requires manual configuration, see the source code.")
    @Test
    public void getSpecificWikiPageByProject() throws Exception {
        WikiPageDetail specificPage = manager.getWikiPageDetailByProjectAndTitle("test", "Wiki");

        assertThat(specificPage.getParent().getTitle()).isEqualTo("Wiki");
        assertThat(specificPage.getVersion()).isEqualTo(2);
        assertThat(specificPage.getAttachments()).isNotNull();
        assertThat(specificPage.getAttachments().size()).isEqualTo(1);

        Attachment attachment = specificPage.getAttachments().get(0);
        assertThat(attachment.getFileName()).isEqualTo("happy_penguin.jpg");
        assertThat(attachment.getId()).isEqualTo(8);
        assertThat(attachment.getFileSize()).isEqualTo(72158);
        assertThat(attachment.getAuthor().getFullName()).isEqualTo("Redmine Admin");
        assertThat(attachment.getContentURL()).isEqualTo("http://76.126.10.142/redmine/attachments/download/8/happy_penguin.jpg");
    }

    @Test
    public void wikiPageIsUpdated() throws Exception {
        WikiPageDetail specificPage = createSomeWikiPage();
        String newText = "updated text";
        specificPage.setText(newText);
        manager.update(projectKey, specificPage);

        WikiPageDetail updatedPage = manager.getWikiPageDetailByProjectAndTitle(projectKey, specificPage.getTitle());
        assertThat(updatedPage.getText()).isEqualTo(newText);
    }

    private WikiPageDetail createSomeWikiPage() throws RedmineException {
        WikiPageDetail wikiPageDetail = new WikiPageDetail();
        String title = "title " + System.currentTimeMillis();
        wikiPageDetail.setTitle(title);
        String text = "some text here";
        wikiPageDetail.setText(text);
        manager.update(projectKey, wikiPageDetail);
        return wikiPageDetail;
    }
}