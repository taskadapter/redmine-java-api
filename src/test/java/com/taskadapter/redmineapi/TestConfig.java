package com.taskadapter.redmineapi;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

class TestConfig {
    private static final String TEST_PROPERTIES = "api_test.properties";

    private final Properties properties = new Properties();

    public TestConfig() {
        loadProperties();
    }

    private void loadProperties() {
        InputStream is = TestConfig.class.getClassLoader().getResourceAsStream(
                TEST_PROPERTIES);
        if (is == null) {
            throw new RuntimeException("Can't find file " + TEST_PROPERTIES +
                    " in classpath. Please create it using one of the templates");
        }
        try {
            properties.load(is);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String getURI() {
        return properties.getProperty("uri");
    }

    public String getLogin() {
        return properties.getProperty("user");
    }

    public String getPassword() {
        return properties.getProperty("password");
    }

    public String getApiKey() {
        return properties.getProperty("apikey");
    }

    public String getParam(String key) {
        return properties.getProperty(key);
    }
}
