package com.taskadapter.redmineapi.bean;

import java.util.Date;

/**
 * File Attachment for a Redmine issue
 */
public class Attachment implements Identifiable {

    private final PropertyStorage storage;

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

    /**
     * Use AttachmentFactory to create instances of this class.
     *
     * @param id database ID.
     * @see com.taskadapter.redmineapi.bean.AttachmentFactory
     */
    Attachment(Integer id) {
        storage = new PropertyStorage();
        storage.set(DATABASE_ID, id);
    }

    @Override
    /**
     * @return id. NULL for attachments not added to Redmine yet.
     */
    public Integer getId() {
        return storage.get(DATABASE_ID);
    }

    public String getContentType() {
        return storage.get(CONTENT_TYPE);
    }

    public void setContentType(String contentType) {
        storage.set(CONTENT_TYPE, contentType);
    }

    public String getContentURL() {
        return storage.get(CONTENT_URL);
    }

    public void setContentURL(String contentURL) {
        storage.set(CONTENT_URL, contentURL);
    }

    /**
     * Description is empty by default, not NULL.
     */
    public String getDescription() {
        return storage.get(DESCRIPTION);
    }

    public void setDescription(String description) {
        storage.set(DESCRIPTION, description);
    }

    public Date getCreatedOn() {
        return storage.get(CREATED_ON);
    }

    public void setCreatedOn(Date createdOn) {
        storage.set(CREATED_ON, createdOn);
    }

    public User getAuthor() {
        return storage.get(AUTHOR);
    }

    public void setAuthor(User author) {
        storage.set(AUTHOR, author);
    }

    public String getFileName() {
        return storage.get(FILE_NAME);
    }

    public void setFileName(String fileName) {
        storage.set(FILE_NAME, fileName);
    }

    public Long getFileSize() {
        return storage.get(FILE_SIZE);
    }

    public void setFileSize(Long fileSize) {
        storage.set(FILE_SIZE, fileSize);
    }

    public String getToken() {
        return storage.get(TOKEN);
    }

    public void setToken(String token) {
        storage.set(TOKEN, token);
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
}
