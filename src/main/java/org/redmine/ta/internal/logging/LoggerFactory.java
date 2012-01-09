package org.redmine.ta.internal.logging;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Factory for {@link DefaultLogger}s
 */
public final class LoggerFactory {

    private static final String PROPERTIES_FILE_NAME = "redmine.log.properties";
    private static final String PROPERTY_KEY_LOGLEVEL = "log.level";
    private static final LogLevel DEFAULT_LOG_LEVEL = LogLevel.INFO;

    private static Map<String, DefaultLogger> loggers = new HashMap<String, DefaultLogger>();
    private static LogLevel logLevel;

    /**
     * Private constructor. Class is not meant to be instantiated by clients.
     */
    private LoggerFactory() {
    }

    /**
     * Delivers the {@link Logger} associated to the given identifier.
     *
     * @param identifier the identifier
     * @return the {@link Logger}
     */
    public static Logger getLogger(String identifier) {
        // lazy loading of properties
        if (logLevel == null) {
            init();
        }
        // create and add at first access
        DefaultLogger logger = loggers.get(identifier);
        if (logger == null) {
            // create logger and set log level from configuration
            logger = new DefaultLogger(identifier);
            logger.setLogLevel(logLevel);
            loggers.put(identifier, logger);
        }
        return logger;
    }

    /**
     * Delivers the {@link Logger} associated to the given {@link Class}.
     *
     * @param clazz the {@link Class}
     * @return the {@link Logger}
     */
    public static Logger getLogger(Class clazz) {
        return getLogger(clazz.getSimpleName());
    }

    private static void init() {
        // load logging properties
        Properties properties = null;
        try {
            URL url = ClassLoader.getSystemResource(PROPERTIES_FILE_NAME);
            // in case properties file is not found => use default properties
            if (url != null) {
                properties = new Properties();
                properties.load(url.openStream());
            }
        } catch (IOException ioe) {
            // something went wrong while trying to load properties file
            System.err.println("Error when loading logging properties: " + ioe);
        }
        // valid properties loaded? If not, we will use the default configuration.
        if ((properties == null) || (!properties.containsKey(PROPERTY_KEY_LOGLEVEL))) {
            System.err.println("No valid logging configuration could be loaded [" + PROPERTIES_FILE_NAME + "] => using default configuration");
            properties = createDefaultConfiguration();
        }
        // inspect properties for log level to use
        try {
            logLevel = LogLevel.valueOf((String) properties.get(PROPERTY_KEY_LOGLEVEL));
        } catch (IllegalArgumentException iae) {
            // invalid value for log level specified in properties file = we use the default
            System.err.println("Invalid value for " + PROPERTY_KEY_LOGLEVEL + " specified in logging configuration " + PROPERTIES_FILE_NAME + " => using default log level " + DEFAULT_LOG_LEVEL);
            logLevel = DEFAULT_LOG_LEVEL;
        }
    }

    private static Properties createDefaultConfiguration() {
        Properties properties = new Properties();
        properties.put("log.level", DEFAULT_LOG_LEVEL);
        return properties;
    }
}
