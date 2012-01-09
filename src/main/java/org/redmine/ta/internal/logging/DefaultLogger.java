package org.redmine.ta.internal.logging;

import java.util.Date;

/**
 * @author Matthias Paul Scholz
 */
public final class DefaultLogger implements Logger {

    private String identifier;

    /**
     * default level is INFO
     */
    private LogLevel logLevel = LogLevel.INFO;

    public DefaultLogger(String identifier) {
        this.identifier = identifier;
    }

    @Override
    public LogLevel getLogLevel() {
        return logLevel;
    }

    @Override
    public void setLogLevel(LogLevel logLevel) {
        this.logLevel = logLevel;
    }

    public String getIdentifier() {
        return identifier;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DefaultLogger logger = (DefaultLogger) o;

        if (identifier != null ? !identifier.equals(logger.identifier) : logger.identifier != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return identifier != null ? identifier.hashCode() : 0;
    }

    @Override
    public void debug(String message) {
        // log only in case log level is set to DEBUG
        if (LogLevel.DEBUG.equals(logLevel)) {
            log(message, LogLevel.DEBUG);
        }
    }

    @Override
    public void info(String message) {
        // log in case log level has been set to DEBUG or INFO
        switch (logLevel) {
            case INFO:
            case DEBUG:
                log(message, LogLevel.INFO);
        }
    }

    @Override
    public void warn(String message) {
        // log in case log level has been set to DEBUG or INFO or WARN
        switch (logLevel) {
            case INFO:
            case DEBUG:
            case WARN:
                log(message, LogLevel.WARN);
        }
    }

    @Override
    public void error(String message) {
        // log for all log levels set
        log(message, LogLevel.ERROR);
    }

    @Override
    public void error(Throwable throwable, String message) {
        if (LogLevel.ERROR.equals(logLevel)) {
            log(throwable, message, LogLevel.ERROR);
        }
    }

    private void log(String message, LogLevel level) {
        // we use a fixed logging layout for now
        StringBuilder logBuilder = new StringBuilder();
        logBuilder.append(new Date());
        logBuilder.append(" - [");
        logBuilder.append(level);
        logBuilder.append("] - ");
        logBuilder.append(getIdentifier());
        logBuilder.append(" - ");
        logBuilder.append(message);
        // we log to System.out only for now
        System.out.println(logBuilder.toString());
    }

    private void log(Throwable throwable, String message, LogLevel level) {
        log(message, level);
        // we log to System.out only for now
        throwable.printStackTrace();
    }
}
