package org.redmine.ta.internal.logging;

/**
 * Interface for a logger.
 *
 * @author Matthias Paul Scholz
 */
public interface Logger {

    /**
     * Logs a message on debug level.
     *
     * @param message the message to log
     */
    void debug(String message);

    /**
     * Logs a message on info level.
     *
     * @param message the message to log
     */
    void info(String message);

    /**
     * Logs a message on warn level.
     *
     * @param message the message to log
     */
    void warn(String message);

    /**
     * Logs a message on error level.
     *
     * @param message the message to log
     */
    void error(String message);

    /**
     * Logs a {@link Throwable} on error level.
     *
     * @param throwable the {@link Throwable} to log
     * @param message   the message to log
     */
    void error(Throwable throwable, String message);

    /**
     * @param logLevel the {@link LogLevel}
     */
    void setLogLevel(LogLevel logLevel);

    /**
     * @return the {@link LogLevel} of the {@link DefaultLogger}
     */
    LogLevel getLogLevel();
}
