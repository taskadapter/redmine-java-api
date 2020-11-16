package com.taskadapter.redmineapi.bean;

import com.taskadapter.redmineapi.internal.Transport;

import java.util.Date;

public class File implements Identifiable, FluentStyle {

  private final PropertyStorage storage = new PropertyStorage();

  /**
   * database numeric Id
   */
  public final static Property<Integer> DATABASE_ID = new Property<>(Integer.class, "id");
  public final static Property<String> FILENAME = new Property<>(String.class, "filename");
  public final static Property<Long> FILESIZE = new Property<>(Long.class, "filesize");
  public final static Property<String> CONTENT_TYPE = new Property<>(String.class, "content_type");
  public final static Property<String> DESCRIPTION = new Property<>(String.class, "description");
  public final static Property<String> CONTENT_URL = new Property<>(String.class, "content_url");
  public final static Property<User> AUTHOR = new Property<>(User.class, "author");
  public final static Property<Date> CREATED_ON = new Property<>(Date.class, "created_on");
  public final static Property<Version> VERSION = new Property<>(Version.class, "version");
  public final static Property<String> DIGEST = new Property<>(String.class, "digest");
  public final static Property<Integer> DOWNLOADS = new Property<>(Integer.class, "downloads");
  public final static Property<String> TOKEN = new Property<>(String.class, "token");

  private Transport transport;

  public File(Transport transport) {
    setTransport(transport);
  }

  @Override
  public void setTransport(Transport transport) {
    this.transport = transport;
    PropertyStorageUtil.updateCollections(storage, transport);
  }

  public File setId(Integer id) {
    storage.set(DATABASE_ID, id);
    return this;
  }

  @Override
  public Integer getId() {
    return storage.get(DATABASE_ID);
  }

  public String getContentType() {
    return storage.get(CONTENT_TYPE);
  }

  public File setContentType(String contentType) {
    storage.set(CONTENT_TYPE, contentType);
    return this;
  }

  public String getContentURL() {
    return storage.get(CONTENT_URL);
  }

  public File setContentURL(String contentURL) {
    storage.set(CONTENT_URL, contentURL);
    return this;
  }

  /**
   * Description is empty by default, not NULL.
   */
  public String getDescription() {
    return storage.get(DESCRIPTION);
  }

  public File setDescription(String description) {
    storage.set(DESCRIPTION, description);
    return this;
  }

  public Date getCreatedOn() {
    return storage.get(CREATED_ON);
  }

  public File setCreatedOn(Date createdOn) {
    storage.set(CREATED_ON, createdOn);
    return this;
  }

  public User getAuthor() {
    return storage.get(AUTHOR);
  }

  public File setAuthor(User author) {
    storage.set(AUTHOR, author);
    return this;
  }

  public String getFileName() {
    return storage.get(FILENAME);
  }

  public File setFileName(String fileName) {
    storage.set(FILENAME, fileName);
    return this;
  }

  public Long getFileSize() {
    return storage.get(FILESIZE);
  }

  public File setFileSize(Long fileSize) {
    storage.set(FILESIZE, fileSize);
    return this;
  }

  public Version getVersion() {
    return storage.get(VERSION);
  }

  public File setVersion(Version version) {
    storage.set(VERSION, version);
    return this;
  }

  public String getDigest() {
    return storage.get(DIGEST);
  }

  public File setDigest(String digest) {
    storage.set(DIGEST, digest);
    return this;
  }

  public int getDownloads() {
    return storage.get(DOWNLOADS);
  }

  public File setDownloads(int downloads) {
    storage.set(DOWNLOADS, downloads);
    return this;
  }

  public String getToken() {
    return storage.get(TOKEN);
  }

  public File setToken(String token) {
    storage.set(TOKEN, token);
    return this;
  }

  public PropertyStorage getStorage() {
    return storage;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    File that = (File) o;

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
    return "File{" +
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
}
