package com.taskadapter.redmineapi.bean;

import com.taskadapter.redmineapi.RedmineException;
import com.taskadapter.redmineapi.internal.Transport;

import java.util.Date;

/**
 * File Attachment for a Redmine issue
 */
public class Attachment implements Identifiable, FluentStyle {

    private final PropertyStorage storage = new PropertyStorage();

    /**
     * database numeric Id
     */
    public final static Property<Integer> DATABASE_ID = new Property<>(Integer.class, "id");
    public final static Property<String> FILE_NAME = new Property<>(String.class, "fileName");
    public final static Property<Long> FILE_SIZE = new Property<>(Long.class, "fileSize");
    public final static Property<String> CONTENT_TYPE = new Property<>(String.class, "contentType");
    public final static Property<String> CONTENT_URL = new Property<>(String.class, "contentURL");
    public final static Property<String> DESCRIPTION = new Property<>(String.class, "description");
    public final static Property<Date> CREATED_ON = new Property<>(Date.class, "createdOn");
    public final static Property<User> AUTHOR = new Property<>(User.class, "author");
    public final static Property<String> TOKEN = new Property<>(String.class, "token");
    private Transport transport;

    public Attachment(Transport transport) {
        setTransport(transport);
    }

    public Attachment setId(Integer id) {
        storage.set(DATABASE_ID, id);
        return this;
    }

    /**
     * @return id. NULL for attachments not added to Redmine yet.
     */
    @Override
    public Integer getId() {
        return storage.get(DATABASE_ID);
    }

    public String getContentType() {
        return storage.get(CONTENT_TYPE);
    }

    public Attachment setContentType(String contentType) {
        storage.set(CONTENT_TYPE, contentType);
        return this;
    }

    public String getContentURL() {
        return storage.get(CONTENT_URL);
    }

    public Attachment setContentURL(String contentURL) {
        storage.set(CONTENT_URL, contentURL);
        return this;
    }

    /**
     * Description is empty by default, not NULL.
     */
    public String getDescription() {
        return storage.get(DESCRIPTION);
    }

    public Attachment setDescription(String description) {
        storage.set(DESCRIPTION, description);
        return this;
    }

    public Date getCreatedOn() {
        return storage.get(CREATED_ON);
    }

    public Attachment setCreatedOn(Date createdOn) {
        storage.set(CREATED_ON, createdOn);
        return this;
    }

    public User getAuthor() {
        return storage.get(AUTHOR);
    }

    public Attachment setAuthor(User author) {
        storage.set(AUTHOR, author);
        return this;
    }

    public String getFileName() {
        return storage.get(FILE_NAME);
    }

    public Attachment setFileName(String fileName) {
        storage.set(FILE_NAME, fileName);
        return this;
    }

    public Long getFileSize() {
        return storage.get(FILE_SIZE);
    }

    public Attachment setFileSize(Long fileSize) {
        storage.set(FILE_SIZE, fileSize);
        return this;
    }

    public String getToken() {
        return storage.get(TOKEN);
    }

    public Attachment setToken(String token) {
        storage.set(TOKEN, token);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Attachment that = (Attachment) o;

        if (getId() != null ? !getId().equals(that.getId()) : that.getId() != null) return false;
        if (getToken() != null ? !getToken().equals(that.getToken()) : that.getToken() != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 3 * hash + (getId() != null ? getId().hashCode() : 0);
        hash = 3 * hash + (getToken() != null ? getToken().hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        return "Attachment{" +
                "id=" + getId() +
                ", fileName='" + getFileName() + '\'' +
                ", fileSize=" + getFileSize() +
                ", contentType='" + getContentType() + '\'' +
                ", contentURL='" + getContentURL() + '\'' +
                ", description='" + getDescription() + '\'' +
                ", createdOn=" + getCreatedOn() +
                ", author=" + getAuthor() +
                ", token=" + getToken() +
                '}';
    }

    public PropertyStorage getStorage() {
        return storage;
    }

    /**
     * delete the attachment with pre-configured ID from the server.
     * <br>
     * see http://www.redmine.org/issues/14828
     *
     * @since Redmine 3.3.0
     */
    public void delete() throws RedmineException {
        transport.deleteObject(Attachment.class, Integer.toString(getId()));
    }

    @Override
    public void setTransport(Transport transport) {
        this.transport = transport;
        PropertyStorageUtil.updateCollections(storage, transport);
    }
}
