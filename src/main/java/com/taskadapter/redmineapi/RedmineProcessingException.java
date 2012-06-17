package com.taskadapter.redmineapi;

import java.util.List;

public class RedmineProcessingException extends RedmineException {
    private static final long serialVersionUID = 1L;

    private final List<String> errors;
    private String text = "";

    // TODO Refactor this to get rid of adding "\n". it should be up to the UI layer how to format all this
    public RedmineProcessingException(List<String> errors) {
        this.errors = errors;
        final StringBuilder builder = new StringBuilder();
        for (String s : errors) {
            builder.append(s);
            builder.append("\n");
        }
        this.text = builder.toString();
    }

    public List<String> getErrors() {
        return errors;
    }

    @Override
    public String getMessage() {
        return text;
    }
}
