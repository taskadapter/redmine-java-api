package org.redmine.ta.internal;

public class Response {
    private int code;
    private String body;

    public Response(int code, String body) {
        super();
        this.code = code;
        this.body = body;
    }

    public int getCode() {
        return code;
    }

    public String getBody() {
        return body;
    }
}
