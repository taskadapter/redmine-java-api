package com.taskadapter.redmineapi.internal;

import java.util.List;

public class ResultsWrapper<T> {
    final private Integer totalFoundOnServer;
    final private List<T> results;

    public ResultsWrapper(Integer totalFoundOnServer, List<T> results) {
        this.totalFoundOnServer = totalFoundOnServer;
        this.results = results;
    }

    public boolean hasSomeResults() {
        return !results.isEmpty();
    }

    public List<T> getResults() {
        return results;
    }

    public int getResultsNumber() {
        return results.size();
    }

    public Integer getTotalFoundOnServer() {
        return totalFoundOnServer;
    }
}
