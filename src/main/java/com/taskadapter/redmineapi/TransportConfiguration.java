package com.taskadapter.redmineapi;

import org.apache.http.client.HttpClient;

/**
 * Configuration of transport layer for the Redmine manager. We are leaking
 * transport level abstraction. As demand grows, we provide more and more
 * options. That options make sense only for a specific implementations
 * (like eviction parameters). Adding more options will just leak more and
 * more abstractions and we still would not be able to change implementation
 * without making out API obsolete. So we provide a class to configure lowest
 * possible level but encourage users to use provided factory methods for
 * such configurations.
 */
public final class TransportConfiguration {
    final HttpClient client;
    public final Runnable shutdownListener;

    private TransportConfiguration(HttpClient client, Runnable shutdownListener) {
        this.client = client;
        this.shutdownListener = shutdownListener;
    }

    /**
     * Creates a new transport configuration to use inside redmine manager.
     * <p> Redmine manager works with the HTTP protocol. That protocol supports
     * keep-alive connections so connection management became crucial. At some
     * point RedmineManager may became unneeded and can be closed by call to
     * a {@link RedmineManager#shutdown()}. We need to notify an underlying
     * provider that this "client" is no longer used by manager. We can't close
     * client's connection manager for two reasons. First, that connection
     * manager can be used by other clients. Second, some additional work may
     * be associated with that connection manager. Connection evictors are
     * usually point of caution.
     * @param client http client to use for the communication with the server.
     * @param shutdownListener listener to call when redmine manager is
     * shut down or finalized. May be null if no additional shutdown procedures
     * are required.
     * @return transport configuration for redmine manager.
     */
    public static TransportConfiguration create(HttpClient client, Runnable shutdownListener) {
        return new TransportConfiguration(client, shutdownListener);
    }
}