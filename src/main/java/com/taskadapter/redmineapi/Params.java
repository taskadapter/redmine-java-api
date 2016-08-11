package com.taskadapter.redmineapi;

import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;

public final class Params {
    private List<BasicNameValuePair> list = new ArrayList<>();

    public Params add(String name, String value) {
        list.add(new BasicNameValuePair(name, value));
        return this;
    }

    public List<BasicNameValuePair> getList() {
        return list;
    }
}
