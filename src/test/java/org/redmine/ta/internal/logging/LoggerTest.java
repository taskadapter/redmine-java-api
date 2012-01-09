package org.redmine.ta.internal.logging;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test case for a {@link DefaultLogger}.
 *
 * @author Matthias Paul Scholz
 */
public class LoggerTest {

    @Test
    public void testWriteLogMessage() throws IOException {
        Logger logger = LoggerFactory.getLogger("Test logger");
        // redirect system out
        ByteArrayOutputStream outContent = null;
        try {
            outContent = new ByteArrayOutputStream();
            System.setOut(new PrintStream(outContent));
            // set log level to WARN
            logger.setLogLevel(LogLevel.WARN);
            // log info message
            logger.info("An INFO test log message written on log level WARN");
            // assert that log message was not written as we have set the log level to WARN
            assertEquals("Logger should not have written log message", 0, outContent.size());
            // now set log level to DEBUG
            logger.setLogLevel(LogLevel.DEBUG);
            // log info message
            logger.info("An INFO test log message written on log level DEBUG");
            // assert that log message was written as we have set the log level to DEBUG
            assertTrue("Logger did not write log message as expected", outContent.size() > 0);
        } finally {
            System.setOut(System.out);
            if (outContent != null) {
                outContent.close();
            }
        }

    }

}
