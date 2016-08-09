package com.taskadapter.redmineapi.bean;

import java.util.ArrayList;
import java.util.List;

public class WikiPageDetail extends WikiPage {

    public final static Property<WikiPageDetail> PARENT = new Property<>(WikiPageDetail.class, "parent");
    public final static Property<String> TEXT = new Property<>(String.class, "text");
    public final static Property<User> USER = new Property<>(User.class, "user");
    public final static Property<String> COMMENTS = new Property<>(String.class, "comments");
    public final static Property<List<Attachment>> ATTACHMENTS = (Property<List<Attachment>>) new Property(List.class, "attachments");

    public WikiPageDetail() {
        super();
        initCollections();
    }

    private void initCollections() {
        storage.set(ATTACHMENTS, new ArrayList<>());
    }

    /**
     * @return the comment entered when the wiki page was last edited
     */
    public String getComments() {
        return storage.get(COMMENTS);
    }

    public void setComments(String comments) {
        storage.set(COMMENTS, comments);
    }

    public List<Attachment> getAttachments() {
        return storage.get(ATTACHMENTS);
    }

    public void setAttachments(List<Attachment> attachments) {
        storage.set(ATTACHMENTS, attachments);
    }

    public WikiPageDetail getParent() {
        return storage.get(PARENT);
    }

    public void setParent(WikiPageDetail parent) {
        storage.set(PARENT, parent);
    }

    public String getText() {
        return storage.get(TEXT);
    }

    public void setText(String text) {
        storage.set(TEXT, text);
    }

    public User getUser() {
        return storage.get(USER);
    }

    public void setUser(User user) {
        storage.set(USER, user);
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
}