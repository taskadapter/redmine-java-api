package com.taskadapter.redmineapi;

import com.taskadapter.redmineapi.bean.Attachment;
import com.taskadapter.redmineapi.bean.Issue;
import com.taskadapter.redmineapi.internal.CopyBytesHandler;
import com.taskadapter.redmineapi.internal.Transport;
import com.taskadapter.redmineapi.internal.io.MarkedIOException;
import com.taskadapter.redmineapi.internal.io.MarkedInputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Works with Attachments (files).
 * <p>Obtain it via RedmineManager:
 * <pre>
       RedmineManager redmineManager = RedmineManagerFactory.createWithUserAuth(redmineURI, login, password);
       AttachmentManager attachmentManager = redmineManager.getAttachmentManager();
 * </pre>
 *
 * <p>Sample usage:
 * <pre>
 File file = ...
 attachmentManager.addAttachmentToIssue(issueId, file, ContentType.TEXT_PLAIN.getMimeType());
 * </pre>
 *
 * @see RedmineManager#getAttachmentManager()
 */
public class AttachmentManager {
    private final Transport transport;

    AttachmentManager(Transport transport) {
        this.transport = transport;
    }


    /**
     *
     * @param issueId database ID of the Issue
     * @param attachmentFile the file to upload
     * @param contentType MIME type. depending on this parameter, the file will be recognized by the server as
     *                    text or image or binary. see http://en.wikipedia.org/wiki/Internet_media_type for possible MIME types.
     *                    sample value: ContentType.TEXT_PLAIN.getMimeType()
     * @return the created attachment object.
     */
    public Attachment addAttachmentToIssue(Integer issueId, File attachmentFile, String contentType) throws RedmineException, IOException {
        final Attachment attach = uploadAttachment(contentType, attachmentFile);
        new Issue(transport, -1).setId(issueId)
                .addAttachment(attach)
                .update();

        return attach;
    }

    /**
     * Uploads an attachment.
     *
     * @param fileName
     *            file name of the attachment.
     * @param contentType
     *            content type of the attachment.
     * @param content
     *            attachment content stream.
     * @return attachment content.
     * @throws RedmineException
     *             if something goes wrong.
     * @throws java.io.IOException
     *             if input cannot be read.
     */
    public Attachment uploadAttachment(String fileName, String contentType,
                                       byte[] content) throws RedmineException, IOException {
        final InputStream is = new ByteArrayInputStream(content);
        try {
            return uploadAttachment(fileName, contentType, is, content.length);
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                throw new RedmineInternalError("Unexpected exception", e);
            }
        }
    }

    /**
     * Uploads an attachment.
     *
     * @param contentType
     *            content type of the attachment.
     * @param content
     *            attachment content stream.
     * @return attachment content.
     * @throws RedmineException
     *             if something goes wrong.
     * @throws IOException
     *             if input cannot be read.
     */
    public Attachment uploadAttachment(String contentType, File content)
            throws RedmineException, IOException {
        try (InputStream is = new FileInputStream(content)) {
            return uploadAttachment(content.getName(), contentType, is, content.length());
        }
    }

    /**
     * Uploads an attachment.
     *
     * @param fileName
     *            file name of the attachment.
     * @param contentType
     *            content type of the attachment.
     * @param content
     *            attachment content stream.
     * @return attachment content.
     * @throws RedmineException if something goes wrong.
     * @throws IOException
     *             if input cannot be read. This exception cannot be thrown yet
     *             (I am not sure if http client can distinguish "network"
     *             errors and local errors) but is will be good to distinguish
     *             reading errors and transport errors.
     */
    public Attachment uploadAttachment(String fileName, String contentType,
                                       InputStream content) throws RedmineException, IOException {
        return uploadAttachment(fileName, contentType, content, -1);
    }

    /**
     * Uploads an attachment passing total length.
     *
     * @param fileName
     *            file name of the attachment.
     * @param contentType
     *            content type of the attachment.
     * @param content
     *            attachment content stream.
     * @param contentLength
     *            attachment length. Use -1 to enable Transfer-encoding: chunked. Pass exact length to avoid that
     *            as some web servers (like lighthttpd do not support it)
     * @return attachment content.
     * @throws RedmineException if something goes wrong.
     * @throws IOException
     *             if input cannot be read. This exception cannot be thrown yet
     *             (I am not sure if http client can distinguish "network"
     *             errors and local errors) but is will be good to distinguish
     *             reading errors and transport errors.
     */
    public Attachment uploadAttachment(String fileName, String contentType, InputStream content, long contentLength) throws RedmineException, IOException {
        final InputStream wrapper = new MarkedInputStream(content,
                "uploadStream");
        final String token;
        try {
            token = transport.upload(wrapper, contentLength);
            return new Attachment(transport)
                    .setToken(token)
                    .setContentType(contentType)
                    .setFileName(fileName);
        } catch (RedmineException e) {
            unwrapException(e, "uploadStream");
            throw e;
        }
    }

    /**
     * Delivers an {@link com.taskadapter.redmineapi.bean.Attachment} by its ID.
     *
     * @param attachmentID the ID
     * @return the {@link com.taskadapter.redmineapi.bean.Attachment}
     * @throws RedmineAuthenticationException thrown in case something went wrong while trying to login
     * @throws RedmineException        thrown in case something went wrong in Redmine
     * @throws NotFoundException       thrown in case an object can not be found
     */
    public Attachment getAttachmentById(int attachmentID) throws RedmineException {
        return transport.getObject(Attachment.class, attachmentID);
    }

    public void downloadAttachmentContent(Attachment issueAttachment,
                                          OutputStream stream) throws RedmineException {
        transport.download(issueAttachment.getContentURL(),
                new CopyBytesHandler(stream));
    }

    /**
     * Downloads the content of an {@link com.taskadapter.redmineapi.bean.Attachment} from the Redmine server.
     *
     * @param issueAttachment the {@link com.taskadapter.redmineapi.bean.Attachment}
     * @return the content of the attachment as a byte[] array
     * @throws RedmineCommunicationException thrown in case the download fails
     */
    public byte[] downloadAttachmentContent(Attachment issueAttachment)
            throws RedmineException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        downloadAttachmentContent(issueAttachment, baos);
        try {
            baos.close();
        } catch (IOException e) {
            throw new RedmineInternalError();
        }
        return baos.toByteArray();
    }

    @Deprecated
    public void delete(int attachmentId) throws RedmineException {
        new Attachment(transport).setId(attachmentId).delete();
    }

    /**
     * @param exception
     *            exception to unwrap.
     * @param tag
     *            target tag.
     */
    private static void unwrapException(RedmineException exception, String tag) throws IOException {
        Throwable e = exception;
        while (e != null) {
            if (e instanceof MarkedIOException) {
                final MarkedIOException marked = (MarkedIOException) e;
                if (tag.equals(marked.getTag()))
                    throw marked.getIOException();
            }
            e = e.getCause();
        }
    }

}
