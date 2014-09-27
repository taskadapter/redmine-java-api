package com.taskadapter.redmineapi.bean;

import java.util.List;

public class WikiPageDetail extends WikiPage {

    private WikiPageDetail parent;
    private String text;
    private User user;
    private String comments;
    private List<Attachment> attachments;

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public List<Attachment> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<Attachment> attachments) {
        this.attachments = attachments;
    }

    public WikiPageDetail getParent() {
        return parent;
    }

    public void setParent(WikiPageDetail parent) {
        this.parent = parent;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return "WikiPageDetail{" +
                "text='" + text + '\'' +
                '}';
    }
}