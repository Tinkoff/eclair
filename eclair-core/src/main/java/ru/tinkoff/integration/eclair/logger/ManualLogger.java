package ru.tinkoff.integration.eclair.logger;

import org.springframework.boot.logging.LogLevel;

import static org.springframework.boot.logging.LogLevel.*;

public interface ManualLogger {

    boolean isLevelEnabled(LogLevel expectedLevel);

    void log(LogLevel level, String format, Object... arguments);

    default boolean isTraceEnabled() {
        return isLevelEnabled(TRACE);
    }

    default boolean isDebugEnabled() {
        return isLevelEnabled(DEBUG);
    }

    default boolean isInfoEnabled() {
        return isLevelEnabled(INFO);
    }

    default boolean isWarnEnabled() {
        return isLevelEnabled(WARN);
    }

    default boolean isErrorEnabled() {
        return isLevelEnabled(ERROR);
    }

    default void trace(String format, Object... arguments) {
        log(TRACE, format, arguments);
    }

    default void debug(String format, Object... arguments) {
        log(DEBUG, format, arguments);
    }

    default void info(String format, Object... arguments) {
        log(INFO, format, arguments);
    }

    default void warn(String format, Object... arguments) {
        log(WARN, format, arguments);
    }

    default void error(String format, Object... arguments) {
        log(ERROR, format, arguments);
    }

    default void debugIfTraceEnabled(String format, Object... arguments) {
        if (isTraceEnabled()) {
            debug(format, arguments);
        }
    }

    default void infoIfDebugEnabled(String format, Object... arguments) {
        if (isDebugEnabled()) {
            info(format, arguments);
        }
    }

    default void infoIfTraceEnabled(String format, Object... arguments) {
        if (isTraceEnabled()) {
            info(format, arguments);
        }
    }

    default void warnIfInfoEnabled(String format, Object... arguments) {
        if (isInfoEnabled()) {
            warn(format, arguments);
        }
    }

    default void warnIfDebugEnabled(String format, Object... arguments) {
        if (isDebugEnabled()) {
            warn(format, arguments);
        }
    }

    default void warnIfTraceEnabled(String format, Object... arguments) {
        if (isTraceEnabled()) {
            warn(format, arguments);
        }
    }

    default void errorIfWarnEnabled(String format, Object... arguments) {
        if (isWarnEnabled()) {
            error(format, arguments);
        }
    }

    default void errorIfInfoEnabled(String format, Object... arguments) {
        if (isInfoEnabled()) {
            error(format, arguments);
        }
    }

    default void errorIfDebugEnabled(String format, Object... arguments) {
        if (isDebugEnabled()) {
            error(format, arguments);
        }
    }

    default void errorIfTraceEnabled(String format, Object... arguments) {
        if (isTraceEnabled()) {
            error(format, arguments);
        }
    }
}
