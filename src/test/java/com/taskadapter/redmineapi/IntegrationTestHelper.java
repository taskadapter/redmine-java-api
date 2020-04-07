package com.taskadapter.redmineapi;

import com.taskadapter.redmineapi.bean.Project;
import com.taskadapter.redmineapi.bean.User;
import com.taskadapter.redmineapi.internal.Transport;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ClientConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.util.Calendar;
import java.util.Collections;
import java.util.Optional;

public class IntegrationTestHelper {
    private static final Logger logger = LoggerFactory.getLogger(IntegrationTestHelper.class);

    // to make sure we all use the same config
    public static TestConfig getTestConfig() {
        return new TestConfig();
    }

    public static User getOurUser(Transport transport) {
        TestConfig testConfig = getTestConfig();
        Integer userId = Integer.parseInt(testConfig.getParam("createissue.userid"));
        String login = testConfig.getLogin();
        String fName = testConfig.getParam("userFName");
        String lName = testConfig.getParam("userLName");
        return new User(transport).setId(userId)
                .setLogin(login)
                .setFirstName(fName)
                .setLastName(lName)
                .setApiKey(testConfig.getParam("apikey"));
    }

    public static RedmineManager createRedmineManager() {
        TestConfig testConfig = getTestConfig();
        logger.info("Running Redmine integration tests using: " + testConfig.getURI());
        final HttpClient client = getHttpClientForTestServer();
        return RedmineManagerFactory.createWithUserAuth(testConfig.getURI(),
                testConfig.getLogin(), testConfig.getPassword(),
                client);
    }

    public static RedmineManager createRedmineManagerWithAPIKey() {
        TestConfig testConfig = getTestConfig();
        logger.info("Running Redmine integration tests using: " + testConfig.getURI());
        final HttpClient client = getHttpClientForTestServer();
        return RedmineManagerFactory.createWithApiKey(testConfig.getURI(), testConfig.getApiKey(), client);
    }

    public static Project createProject(Transport transport) {
        Project testProject = new Project(transport, "test project", "test" + Calendar.getInstance().getTimeInMillis());
        try {
            return testProject.create();
        } catch (Exception e) {
            logger.error("Exception while configuring tests", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Delete the project if it exists. this method ignores NULL or empty projectKey parameter.
     */
    public static void deleteProject(Transport transport, String projectKey) {
        try {
            if (transport != null && projectKey != null) {
                new Project(transport).setIdentifier(projectKey).delete();;
            }
        } catch (Exception e) {
            logger.error("Exception while deleting test project", e);
            throw new RuntimeException("can't delete the test project '" + projectKey + ". reason: " + e.getMessage());
        }
    }

    /**
     * @return The client configured for our Dev Redmine server which has a self-signed SSL certificate.
     */
    public static HttpClient getHttpClientForTestServer() {
        final ClientConnectionManager connectionManager;
        try {
            connectionManager = createConnectionManagerWithOurDevKeystore();
        } catch (Exception e) {
            throw new RuntimeException("cannot create connection manager: " + e, e);
        }
        return RedmineManagerFactory.getNewHttpClient(getTestConfig().getURI(), connectionManager);
    }

    private static ClientConnectionManager createConnectionManagerWithOurDevKeystore() throws KeyManagementException, KeyStoreException {
        final Optional<KeyStore> builtInExtension = getExtensionKeystore();
        final Optional<KeyStore> builtInClient = getClientKeystore();

        if (builtInExtension.isPresent() && ! builtInClient.isPresent()) {
            return RedmineManagerFactory.createConnectionManagerWithExtraTrust(
                    Collections.singletonList(builtInExtension.get()));
        }

        if (builtInExtension.isPresent() && builtInClient.isPresent()) {
            return RedmineManagerFactory.createConnectionManagerWithClientCertificate(builtInClient.get(), 
                    "123456", Collections.singletonList(builtInExtension.get()));
        }

        return RedmineManagerFactory.createDefaultConnectionManager();
    }

    /**
     * Returns a key store with the additional SSL certificates.
     * this is how we provide the self-signed SSL certificate for our
     * Redmine dev server.
     */
    private static Optional<KeyStore> getExtensionKeystore() {
        final InputStream extStore =
                IntegrationTestHelper.class.getClassLoader().getResourceAsStream("ta-dev-cacerts");
        if (extStore == null) {
            return Optional.empty();
        }
        try {
            final KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
            // "123456" is the password for this custom keystore
            ks.load(extStore, "123456".toCharArray());
            return Optional.of(ks);
        } catch (Exception e) {
            e.printStackTrace();
            return Optional.empty();
        } finally {
            try {
                extStore.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Returns a key store with the additional SSL certificates.
     * this is how we provide the self-signed SSL certificate for our
     * Redmine dev server.
     */
    private static Optional<KeyStore> getClientKeystore() {
        final InputStream extStore =
                IntegrationTestHelper.class.getClassLoader().getResourceAsStream("ta-dev-keys");
        if (extStore == null) {
            return Optional.empty();
        }
        try {
            final KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
            // "123456" is the password for this custom keystore
            ks.load(extStore, "123456".toCharArray());
            return Optional.of(ks);
        } catch (Exception e) {
            e.printStackTrace();
            return Optional.empty();
        } finally {
            try {
                extStore.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
