package com.taskadapter.redmineapi.bean;

public class WikiPageFactory {
    public static WikiPage create(String title) {
        WikiPage page = new WikiPage();
        page.setTitle(title);
        return page;
    }
}
