package com.taskadapter.redmineapi;

import com.taskadapter.redmineapi.bean.Attachment;
import com.taskadapter.redmineapi.bean.Issue;
import com.taskadapter.redmineapi.bean.IssueFactory;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNotNull;

public class AttachmentIntegrationTest {

    private static RedmineManager mgr;
    private static String projectKey;

    @BeforeClass
    public static void oneTimeSetup() {
        mgr = IntegrationTestHelper.createRedmineManager();
        projectKey = IntegrationTestHelper.createProject(mgr);
    }

    @AfterClass
    public static void oneTimeTearDown() {
        IntegrationTestHelper.deleteProject(mgr, projectKey);
    }

    @Test
    public void uploadAttachment() throws RedmineException, IOException {
        final byte[] content = new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        final Attachment attach1 = mgr.uploadAttachment("test.bin",
                "application/ternary", content);
        final Issue testIssue = IssueFactory.createWithSubject("This is upload ticket!");
        testIssue.addAttachment(attach1);
        final Issue createdIssue = mgr.createIssue(projectKey, testIssue);
        try {
            final Collection<Attachment> attachments = createdIssue.getAttachments();
            assertThat(attachments.size()).isEqualTo(1);
            final Attachment added = attachments.iterator().next();
            assertThat(added.getFileName()).isEqualTo("test.bin");
            assertThat(added.getContentType()).isEqualTo("application/ternary");
            final byte[] receivedContent = mgr.downloadAttachmentContent(added);
            assertArrayEquals(content, receivedContent);

            Issue issueById = mgr.getIssueById(createdIssue.getId(), RedmineManager.INCLUDE.attachments);
            assertThat(issueById.getAttachments().size()).isEqualTo(1);
        } finally {
            mgr.deleteIssue(createdIssue.getId());
        }
    }

    @Test(expected = IOException.class)
    public void testUploadException() throws RedmineException, IOException {
        final InputStream content = new InputStream() {
            @Override
            public int read() throws IOException {
                throw new IOException("Unsupported read!");
            }
        };
        mgr.uploadAttachment("test.bin", "application/ternary", content);
    }

    /**
     * Tests the retrieval of an {@link com.taskadapter.redmineapi.bean.Attachment} by ID.
     *
     * @throws RedmineException               thrown in case something went wrong in Redmine
     * @throws IOException                    thrown in case something went wrong while performing I/O
     *                                        operations
     * @throws RedmineAuthenticationException thrown in case something went wrong while trying to login
     * @throws NotFoundException              thrown in case the objects requested for could not be found
     */
    // TODO reactivate once Redmine REST API allows for creating attachments
    @Ignore
    @Test
    public void testGetAttachmentById() throws RedmineException {
        // TODO where do we get a valid attachment number from? We can't create
        // an attachment by our own for the test as the Redmine REST API does
        // not support that.
        int attachmentID = 1;
        Attachment attachment = mgr.getAttachmentById(attachmentID);
        assertNotNull("Attachment retrieved by ID " + attachmentID
                + " should not be null", attachment);
        assertNotNull("Content URL of attachment retrieved by ID "
                + attachmentID + " should not be null",
                attachment.getContentURL());
        // TODO more asserts on the attachment once this delivers an attachment
    }

    /**
     * Tests the download of the content of an
     * {@link com.taskadapter.redmineapi.bean.Attachment}.
     *
     * @throws RedmineException               thrown in case something went wrong in Redmine
     * @throws IOException                    thrown in case something went wrong while performing I/O
     *                                        operations
     * @throws RedmineAuthenticationException thrown in case something went wrong while trying to login
     * @throws NotFoundException              thrown in case the objects requested for could not be found
     */
    // TODO reactivate once Redmine REST API allows for creating attachments
    @Ignore
    @Test
    public void testDownloadAttachmentContent() throws RedmineException {
        // TODO where do we get a valid attachment number from? We can't create
        // an attachment by our own for the test as the Redmine REST API does
        // not support that.
        int attachmentID = 1;
        // retrieve issue attachment
        Attachment attachment = mgr.getAttachmentById(attachmentID);
        // download attachment content
        byte[] attachmentContent = mgr.downloadAttachmentContent(attachment);
        assertNotNull("Download of content of attachment with content URL " + attachment.getContentURL()
                + " should not be null", attachmentContent);
    }

    /**
     * Tests the retrieval of an {@link Issue}, inlcuding the
     * {@link com.taskadapter.redmineapi.bean.Attachment}s.
     *
     * @throws RedmineException               thrown in case something went wrong in Redmine
     * @throws IOException                    thrown in case something went wrong while performing I/O
     *                                        operations
     * @throws RedmineAuthenticationException thrown in case something went wrong while trying to login
     * @throws NotFoundException              thrown in case the objects requested for could not be found
     */
    @Test
    public void testGetIssueWithAttachments() throws RedmineException {
        Issue newIssue = null;
        try {
            // create at least 1 issue
            Issue issueToCreate = IssueFactory.createWithSubject("testGetIssueAttachment_"
                    + UUID.randomUUID());
            newIssue = mgr.createIssue(projectKey, issueToCreate);
            // TODO create test attachments for the issue once the Redmine REST
            // API allows for it
            // retrieve issue attachments
            Issue retrievedIssue = mgr.getIssueById(newIssue.getId(),
                    RedmineManager.INCLUDE.attachments);
            assertNotNull("List of attachments retrieved for issue "
                    + newIssue.getId()
                    + " delivered by Redmine Java API should not be null",
                    retrievedIssue.getAttachments());
            // TODO assert attachments once we actually receive ones for our
            // test issue
        } finally {
            // scrub test issue
            if (newIssue != null) {
                mgr.deleteIssue(newIssue.getId());
            }
        }
    }
}