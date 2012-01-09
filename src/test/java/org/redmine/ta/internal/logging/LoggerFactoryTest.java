package org.redmine.ta.internal.logging;

import org.junit.Test;

import java.io.IOException;
import java.net.URL;
import java.util.Properties;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * Test case for the {@link LoggerFactory}.
 *
 * @author Matthias Paul Scholz
 */
public class LoggerFactoryTest {

    private static final String PROPERTIES_FILE_NAME = "redmine.log.properties";
    private static final String PROPERTY_KEY_LOGLEVEL = "log.level";

    /**
     * Tests the creation of loggers.
     */
    @Test
    public void testGetLogger() {
        /*
         get same logger twice and check for identity.
         We want to have only one instance of each logger.
          */
        String loggerIdentifier = "Test logger";
        Logger logger1 = LoggerFactory.getLogger(loggerIdentifier);
        Logger logger2 = LoggerFactory.getLogger(loggerIdentifier);
        Logger logger3 = LoggerFactory.getLogger(this.getClass());
        assertNotNull("First logger retrieved from LoggerFactory should not be null", logger1);
        assertNotNull("Second logger retrieved from LoggerFactory should not be null", logger2);
        assertNotNull("Third logger retrieved from LoggerFactory should not be null", logger3);
        // check for identity of first two loggers
        assertTrue("Two loggers with same identifier should point to the same instance", logger1 == logger2);
        // check that third logger is not equal to the two others
        assertFalse("Expected third logger not to be equal to first logger",logger3.equals(logger1));
    }

    /**
     * Tests the correct configuration of the {@link LoggerFactory}.
     * The test assumes that there is a valid log configuration file in the classpath.
     * @throws IOException thrown in case the configuration file could not be read or is invalid
     * @throws IllegalArgumentException thrown in case the configuration file contains an invalid value
     */
    @Test
    public void testValidLogConfiguration() throws IOException,IllegalArgumentException {
        // load log level from configuration file 
        URL url = ClassLoader.getSystemResource(PROPERTIES_FILE_NAME);
        // in case properties file is not found => use default properties
        if (url == null) {
            throw new IOException("Could not find configuration file " + PROPERTIES_FILE_NAME + " in class path");
        }
        Properties properties = new Properties();
        properties.load(url.openStream());
        LogLevel logLevel = LogLevel.valueOf((String) properties.get(PROPERTY_KEY_LOGLEVEL));
        if(logLevel==null) {
            throw new IOException("Invalid configuration file " + PROPERTIES_FILE_NAME + ": no entry for " + PROPERTY_KEY_LOGLEVEL);
        }
        // create logger
        String loggerIdentifier = "Test logger";
        Logger logger = LoggerFactory.getLogger(loggerIdentifier);
        // assert that the logger has the same log level as provided in the according redmine.log.properties file
       assertEquals("Logger has wrong log level",logLevel,logger.getLogLevel());
    }

}
