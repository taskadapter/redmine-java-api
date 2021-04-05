package com.taskadapter.redmineapi;

import com.taskadapter.redmineapi.bean.*;
import com.taskadapter.redmineapi.internal.Transport;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class WikiManagerIT {

    private static RedmineManager redmineManager;
    private static Project project;
    private static String projectKey;

    private static WikiManager manager;
    private static User currentUser;
    private static Transport transport;

    @BeforeClass
    public static void beforeClass() throws RedmineException {
        redmineManager = IntegrationTestHelper.createRedmineManager();
        transport = redmineManager.getTransport();
        project = IntegrationTestHelper.createProject(transport);
        projectKey = project.getIdentifier();
        manager = redmineManager.getWikiManager();
        UserManager userManager = redmineManager.getUserManager();
        currentUser = userManager.getCurrentUser();
    }

    @AfterClass
    public static void oneTimeTearDown() {
        IntegrationTestHelper.deleteProject(transport, project.getIdentifier());
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
    public void wikiPageWithWeirdSymbolsCanBeLoaded() throws Exception {
        var wikiPageDetail = new WikiPageDetail(transport)
                .setTitle("title " + System.currentTimeMillis() + "ä ö ü")
                .setText("some text here")
                .setProjectKey(projectKey);
        wikiPageDetail.create();

        var loaded = manager.getWikiPageDetailByProjectAndTitle(projectKey, wikiPageDetail.getTitle());
        String title = wikiPageDetail.getTitle();
        String urlSafeTitleIsExpected = URLEncoder.encode(title, StandardCharsets.UTF_8.name());
        assertThat(loaded.getTitle()).isEqualToIgnoringCase(urlSafeTitleIsExpected);
    }

    @Test
    public void getWikiPagesIndexByProject() throws Exception {
        createSomeWikiPage();
        createSomeWikiPage();
        List<WikiPage> wikiPages = manager.getWikiPagesByProject(projectKey);
        assertThat(wikiPages.size()).isGreaterThan(1);
    }

    @Test
    public void wikiPageComplexTest() throws RedmineException, URISyntaxException, IOException {
        Path attachmentPath = Paths.get(getClass().getClassLoader().getResource("invalid_page.txt").toURI());
        Attachment attachment = redmineManager.getAttachmentManager()
                .uploadAttachment(Files.probeContentType(attachmentPath), attachmentPath.toFile());

        String pageTitle = "title " + System.currentTimeMillis();
        WikiPageDetail wikiPage = new WikiPageDetail(transport)
                .setTitle(pageTitle)
                .setText("some text here")
                .setVersion(1)
                .setCreatedOn(new Date())
                .setAttachments(Arrays.asList(attachment))
                .setProjectKey(projectKey);

        wikiPage.update();

        WikiPageDetail actualWikiPage = manager.getWikiPageDetailByProjectAndTitle(projectKey, pageTitle);
        String urlSafeTitleIsExpected = URLEncoder.encode(wikiPage.getTitle(), StandardCharsets.UTF_8.name());
        assertTrue(urlSafeTitleIsExpected.equalsIgnoreCase(actualWikiPage.getTitle()));
        assertEquals(wikiPage.getText(), actualWikiPage.getText());
        assertEquals(wikiPage.getVersion(), actualWikiPage.getVersion());
        assertThat(actualWikiPage.getCreatedOn()).isNotNull();

        Attachment actualAttachment = actualWikiPage.getAttachments().get(0);
        assertEquals(attachment.getFileName(), actualAttachment.getFileName());
        assertEquals(attachment.getContentType(), actualAttachment.getContentType());
        assertEquals(attachmentPath.toFile().length(), actualAttachment.getFileSize().longValue());
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
        specificPage.setText(newText)
                .update();

        WikiPageDetail updatedPage = manager.getWikiPageDetailByProjectAndTitle(projectKey, specificPage.getTitle());
        assertThat(updatedPage.getText()).isEqualTo(newText);
    }

    private WikiPageDetail createSomeWikiPage() throws RedmineException {
        WikiPageDetail wikiPageDetail = new WikiPageDetail(transport)
                .setTitle("title " + System.currentTimeMillis())
                .setText("some text here")
                .setProjectKey(projectKey);
        wikiPageDetail.create();
        return wikiPageDetail;
    }
}