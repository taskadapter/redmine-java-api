package com.taskadapter.redmineapi;

import com.taskadapter.redmineapi.internal.RequestParam;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;

public final class Params {
    private List<RequestParam> list = new ArrayList<>();

    public Params add(String name, String value) {
        list.add(new RequestParam(name, value));
        return this;
    }

    public List<RequestParam> getList() {
        return list;
    }
}
