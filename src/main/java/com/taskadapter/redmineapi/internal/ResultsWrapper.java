package com.taskadapter.redmineapi.internal;

import java.util.List;

public final class ResultsWrapper<T> {
    private final Integer totalFoundOnServer;
    private final Integer limitOnServer;
    private final Integer offsetOnServer;
    private final List<T> results;

    public ResultsWrapper(Integer totalFoundOnServer, Integer limitOnServer, Integer offsetOnServer, List<T> results) {
        this.totalFoundOnServer = totalFoundOnServer;
        this.limitOnServer = limitOnServer;
        this.offsetOnServer = offsetOnServer;
        this.results = results;
    }

    public boolean hasSomeResults() {
        return results != null && !results.isEmpty();
    }

    public List<T> getResults() {
        return results;
    }

    public int getResultsNumber() {
        return results != null ? results.size() : 0;
    }

    public Integer getTotalFoundOnServer() {
        return totalFoundOnServer;
    }

    public Integer getLimitOnServer() {
        return limitOnServer;
    }

    public Integer getOffsetOnServer() {
        return offsetOnServer;
    }
}
