package com.taskadapter.redmineapi.bean;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class WikiPageDetail {

    public final static Property<WikiPageDetail> PARENT = new Property<>(WikiPageDetail.class, "parent");
    public final static Property<String> TEXT = new Property<>(String.class, "text");
    public final static Property<User> USER = new Property<>(User.class, "user");
    public final static Property<String> COMMENTS = new Property<>(String.class, "comments");
    public final static Property<List<Attachment>> ATTACHMENTS = (Property<List<Attachment>>) new Property(List.class, "uploads");

    private final PropertyStorage storage = new PropertyStorage();
    private final WikiPage wikiPage = new WikiPage();

    public WikiPageDetail() {
        storage.set(ATTACHMENTS, new ArrayList<>());
    }

    /**
     * @return the comment entered when the wiki page was last edited
     */
    public String getComments() {
        return storage.get(COMMENTS);
    }

    public WikiPageDetail setComments(String comments) {
        storage.set(COMMENTS, comments);
        return this;
    }

    public List<Attachment> getAttachments() {
        return storage.get(ATTACHMENTS);
    }

    public WikiPageDetail setAttachments(List<Attachment> attachments) {
        storage.set(ATTACHMENTS, attachments);
        return this;
    }

    public WikiPageDetail getParent() {
        return storage.get(PARENT);
    }

    public WikiPageDetail setParent(WikiPageDetail parent) {
        storage.set(PARENT, parent);
        return this;
    }

    public String getText() {
        return storage.get(TEXT);
    }

    public WikiPageDetail setText(String text) {
        storage.set(TEXT, text);
        return this;
    }

    public User getUser() {
        return storage.get(USER);
    }

    public Integer getVersion() {
        return wikiPage.getVersion();
    }

    public WikiPageDetail setUser(User user) {
        storage.set(USER, user);
        return this;
    }

    public WikiPageDetail setTitle(String title) {
        wikiPage.setTitle(title);
        return this;
    }

    public WikiPageDetail setVersion(Integer version) {
        wikiPage.setVersion(version);
        return this;
    }

    public WikiPageDetail setCreatedOn(Date createdOn) {
        wikiPage.setCreatedOn(createdOn);
        return this;
    }

    @Override
    public String toString() {
        return "WikiPageDetail{" +
                "text='" + getText() + '\'' +
                '}';
    }

    public PropertyStorage getStorage() {
        return storage;
    }

    public String getTitle() {
        return wikiPage.getTitle();
    }

    public Date getCreatedOn() {
        return wikiPage.getCreatedOn();
    }

    public Date getUpdatedOn() {
        return wikiPage.getUpdatedOn();
    }

    public WikiPageDetail setUpdatedOn(Date updatedOn) {
        wikiPage.setUpdatedOn(updatedOn);
        return this;
    }
}