package com.taskadapter.redmineapi.bean;

public class NewsFactory {
    public static News create(Integer id) {
        return new News(id);
    }
}
