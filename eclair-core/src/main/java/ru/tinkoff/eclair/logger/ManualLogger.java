package ru.tinkoff.eclair.logger;

import org.springframework.boot.logging.LogLevel;

import static org.springframework.boot.logging.LogLevel.*;

/**
 * @author Viacheslav Klapatniuk
 */
public interface ManualLogger {

    boolean isLogEnabled(LogLevel level);

    void log(LogLevel level, LogLevel ifEnabledLevel, String format, Object... arguments);

    default void log(LogLevel level, String format, Object... arguments) {
        log(level, level, format, arguments);
    }

    default boolean isTraceLogEnabled() {
        return isLogEnabled(TRACE);
    }

    default boolean isDebugLogEnabled() {
        return isLogEnabled(DEBUG);
    }

    default boolean isInfoLogEnabled() {
        return isLogEnabled(INFO);
    }

    default boolean isWarnLogEnabled() {
        return isLogEnabled(WARN);
    }

    default boolean isErrorLogEnabled() {
        return isLogEnabled(ERROR);
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
        log(DEBUG, TRACE, format, arguments);
    }

    default void infoIfDebugEnabled(String format, Object... arguments) {
        log(INFO, DEBUG, format, arguments);
    }

    default void infoIfTraceEnabled(String format, Object... arguments) {
        log(INFO, TRACE, format, arguments);
    }

    default void warnIfInfoEnabled(String format, Object... arguments) {
        log(WARN, INFO, format, arguments);
    }

    default void warnIfDebugEnabled(String format, Object... arguments) {
        log(WARN, DEBUG, format, arguments);
    }

    default void warnIfTraceEnabled(String format, Object... arguments) {
        log(WARN, TRACE, format, arguments);
    }

    default void errorIfWarnEnabled(String format, Object... arguments) {
        log(ERROR, WARN, format, arguments);
    }

    default void errorIfInfoEnabled(String format, Object... arguments) {
        log(ERROR, INFO, format, arguments);
    }

    default void errorIfDebugEnabled(String format, Object... arguments) {
        log(ERROR, DEBUG, format, arguments);
    }

    default void errorIfTraceEnabled(String format, Object... arguments) {
        log(ERROR, TRACE, format, arguments);
    }
}
