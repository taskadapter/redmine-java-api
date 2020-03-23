package com.taskadapter.redmineapi.internal.comm.redmine;

import com.taskadapter.redmineapi.RedmineException;
import com.taskadapter.redmineapi.internal.comm.Communicator;
import com.taskadapter.redmineapi.internal.comm.ContentHandler;
import org.apache.http.HttpRequest;

public class RedmineApiKeyAuthenticator<K> implements Communicator<K> {

    private final String apiKey;

    /**
     * Peer communicator.
     */
    private final Communicator<K> peer;

    public RedmineApiKeyAuthenticator(Communicator<K> peer, String apiKey) {
        if (apiKey == null) {
            throw new IllegalArgumentException("api key cannot be null");
        }
        this.peer = peer;
        this.apiKey = apiKey;
    }

    @Override
    public <R> R sendRequest(HttpRequest request, ContentHandler<K, R> handler)
            throws RedmineException {
        request.addHeader("X-Redmine-API-Key", apiKey);
        return peer.sendRequest(request, handler);
    }
}
