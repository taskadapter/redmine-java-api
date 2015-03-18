package com.taskadapter.redmineapi;

import com.taskadapter.redmineapi.internal.Transport;
import com.taskadapter.redmineapi.internal.URIConfigurator;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import org.apache.http.HttpHost;
import org.apache.http.HttpVersion;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;

import com.taskadapter.redmineapi.internal.comm.ConnectionEvictor;
import com.taskadapter.redmineapi.internal.comm.naivessl.NaiveSSLFactory;

/**
 * <strong>Entry point</strong> for the API. Use this class to communicate with Redmine servers.
 * <p>
 * Collection of creation methods for the redmine. Method number may grow as
 * grows number of requirements. However, having all creation methods in one
 * place allows us to refactor RemineManager internals without changing this
 * external APIs. Moreover, we can create "named constructor" for redmine
 * instances. This will allow us to have many construction methods with the same
 * signature.
 * <p>
 * Sample usage:
 * <pre>
 RedmineManager redmineManager = RedmineManagerFactory.createWithUserAuth(redmineURI, login, password);
 * </pre>
 *
 * @see RedmineManager
 */
public final class RedmineManagerFactory {
    /**
     * Prevent construction of this object even with use of dirty tricks.
     */
    private RedmineManagerFactory() {
        throw new UnsupportedOperationException();
    }

    /**
     * Creates a non-authenticating redmine manager.
     *
     * @param uri redmine manager URI.
     */
    public static RedmineManager createUnauthenticated(String uri) {
        return createUnauthenticated(uri, createDefaultTransportConfig());
    }

    /**
     * Creates a non-authenticating redmine manager.
     *
     * @param uri    redmine manager URI.
     * @param config transport configuration.
     */
    public static RedmineManager createUnauthenticated(String uri,
                                                TransportConfiguration config) {
        return createWithUserAuth(uri, null, null, config);
    }

    /**
     * Creates an instance of RedmineManager class. Host and apiAccessKey are
     * not checked at this moment.
     *
     * @param uri          complete Redmine server web URI, including protocol and port
     *                     number. Example: http://demo.redmine.org:8080
     * @param apiAccessKey Redmine API access key. It is shown on "My Account" /
     *                     "API access key" webpage (check
     *                     <i>http://redmine_server_url/my/account</i> URL). This
     *                     parameter is <strong>optional</strong> (can be set to NULL) for Redmine
     *                     projects, which are "public".
     */
    public static RedmineManager createWithApiKey(String uri,
                                                  String apiAccessKey) {
        return createWithApiKey(uri, apiAccessKey,
                createDefaultTransportConfig());
    }

    /**
     * Creates an instance of RedmineManager class. Host and apiAccessKey are
     * not checked at this moment.
     *
     * @param uri          complete Redmine server web URI, including protocol and port
     *                     number. Example: http://demo.redmine.org:8080
     * @param apiAccessKey Redmine API access key. It is shown on "My Account" /
     *                     "API access key" webpage (check
     *                     <i>http://redmine_server_url/my/account</i> URL). This
     *                     parameter is <strong>optional</strong> (can be set to NULL) for Redmine
     *                     projects, which are "public".
     * @param config       transport configuration.
     */
    public static RedmineManager createWithApiKey(String uri,
                                                  String apiAccessKey, TransportConfiguration config) {
        return new RedmineManager(new Transport(new URIConfigurator(uri,
                apiAccessKey), config.client), config.shutdownListener);
    }

    /**
     * Creates a new RedmineManager with user-based authentication.
     *
     * @param uri      redmine manager URI.
     * @param login    user's name.
     * @param password user's password.
     */
    public static RedmineManager createWithUserAuth(String uri, String login,
                                                    String password) {
        return createWithUserAuth(uri, login, password,
                createDefaultTransportConfig());
    }

    /**
     * Creates a new redmine managen with user-based authentication.
     *
     * @param uri      redmine manager URI.
     * @param login    user's name.
     * @param password user's password.
     * @param config   transport configuration.
     */
    public static RedmineManager createWithUserAuth(String uri, String login,
                                                    String password, TransportConfiguration config) {
        final Transport transport = new Transport(
                new URIConfigurator(uri, null), config.client);
        transport.setCredentials(login, password);
        return new RedmineManager(transport, config.shutdownListener);
    }

    /**
     * Creates default insecure connection manager.
     *
     * @return default insecure connection manager.
     */
    public static PoolingClientConnectionManager createInsecureConnectionManager()
            throws KeyStoreException, NoSuchAlgorithmException,
            CertificateException, KeyManagementException,
            UnrecoverableKeyException {
        return createConnectionManager(Integer.MAX_VALUE,
                NaiveSSLFactory.createNaiveSSLSocketFactory());
    }

    /**
     * Creates default connection manager.
     *
     * @return default insecure connection manager.
     */
    public static PoolingClientConnectionManager createDefaultConnectionManager()
            throws KeyStoreException, NoSuchAlgorithmException,
            CertificateException, KeyManagementException,
            UnrecoverableKeyException {
        return createConnectionManager(Integer.MAX_VALUE,
                SSLSocketFactory.getSocketFactory());
    }

    /**
     * Creates system default connection manager. Takes in account system
     * properties for SSL configuration.
     *
     * @return default insecure connection manager.
     */
    public static PoolingClientConnectionManager createSystemDefaultConnectionManager()
            throws KeyStoreException, NoSuchAlgorithmException,
            CertificateException, KeyManagementException,
            UnrecoverableKeyException {
        return createConnectionManager(Integer.MAX_VALUE,
                SSLSocketFactory.getSystemSocketFactory());
    }

    public static PoolingClientConnectionManager createConnectionManager(
            int maxConnections, SSLSocketFactory sslSocketFactory) {
        SchemeRegistry registry = new SchemeRegistry();
        registry.register(new Scheme("http", 80, PlainSocketFactory
                .getSocketFactory()));
        registry.register(new Scheme("https", 443, sslSocketFactory));

        PoolingClientConnectionManager manager = new PoolingClientConnectionManager(
                registry);
        manager.setMaxTotal(maxConnections);
        manager.setDefaultMaxPerRoute(maxConnections);
        return manager;
    }

    /**
     * Creates a default configuration using a default HTTP client.
     */
    public static TransportConfiguration createDefaultHttpClientConfig() {
        return TransportConfiguration.create(new DefaultHttpClient(), null);
    }

    /**
     * Creates a transport which uses a client connection manager underneath.
     * This transport configuration do not perform any connectino eviction and
     * is usefull for some short-time communication. Common scerario for this
     * method is to create RedmineManager, load/update some tasks and then
     * shutdown all the manager.
     * <p>
     * Note that this configuration will shutdown connection manager when
     * shutdown will be called on RedmineManager instance.
     * </p>
     */
    public static TransportConfiguration createShortTermConfig(
            final ClientConnectionManager connectionManager) {
        return TransportConfiguration.create(
                getNewHttpClient(connectionManager), new Runnable() {
                    @Override
                    public void run() {
                        connectionManager.shutdown();
                    }
                });
    }

    /**
     * Creates a transport which supports connection eviction. This transport
     * can be used in a long-term interactive scenarios where actual redmine
     * communications are interleaved with user interactios (data input).
     * <p>
     * Shutting down redmine manager will also shut down provided connection
     * manager.
     * </p>
     * @param connectionManager connection manager to use.
     * @param idleTimeout       idle timeout for connection before eviction, seconds.
     * @param evictionCheck     eviction check interval, seconds.
     */
    public static TransportConfiguration createLongTermConfiguration(
            final ClientConnectionManager connectionManager, int idleTimeout,
            int evictionCheck) {
        final ConnectionEvictor evictor = new ConnectionEvictor(
                connectionManager, evictionCheck, idleTimeout);

        final Thread evictorThread = new Thread(evictor);
        evictorThread.setDaemon(true);
        evictorThread
                .setName("Redmine communicator connection eviction thread");
        evictorThread.start();

        try {
            return TransportConfiguration.create(
                    getNewHttpClient(connectionManager), new Runnable() {
                        @Override
                        public void run() {
                            try {
                                connectionManager.shutdown();
                            } finally {
                                evictor.shutdown();
                            }
                        }
                    });
        } catch (RuntimeException t) {
            /* A little paranoia, StackOferflow, OOM, other excetpions. */
            evictor.shutdown();
            throw t;
        } catch (Error e) {
            evictor.shutdown();
            throw e;
        }
    }

    public static TransportConfiguration createDefaultTransportConfig() {
        try {
            return createShortTermConfig(createSystemDefaultConnectionManager());
        } catch (Exception e) {
            e.printStackTrace();
            return createDefaultHttpClientConfig();
        }
    }

    /**
     * Helper method to create an http client from connection manager. This new
     * client is configured to use system proxy (if any).
     */
    public static DefaultHttpClient getNewHttpClient(
            ClientConnectionManager connectionManager) {
        try {

            HttpParams params = new BasicHttpParams();
            HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
            HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);

            final DefaultHttpClient result = new DefaultHttpClient(
                    connectionManager, params);
            configureProxy(result);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return new DefaultHttpClient();
        }
    }

    private static void configureProxy(DefaultHttpClient httpclient) {
        String proxyHost = System.getProperty("http.proxyHost");
        String proxyPort = System.getProperty("http.proxyPort");
        if (proxyHost != null && proxyPort != null) {
            int port;
            try {
                port = Integer.parseInt(proxyPort);
            } catch (NumberFormatException e) {
                throw new RedmineConfigurationException("Illegal proxy port "
                        + proxyPort, e);
            }
            HttpHost proxy = new HttpHost(proxyHost, port);
            httpclient.getParams().setParameter(
                    org.apache.http.conn.params.ConnRoutePNames.DEFAULT_PROXY,
                    proxy);
            String proxyUser = System.getProperty("http.proxyUser");
            if (proxyUser != null) {
                String proxyPassword = System.getProperty("http.proxyPassword");
                httpclient.getCredentialsProvider().setCredentials(
                        new AuthScope(proxyHost, port),
                        new UsernamePasswordCredentials(proxyUser,
                                proxyPassword));
            }
        }
    }
}
