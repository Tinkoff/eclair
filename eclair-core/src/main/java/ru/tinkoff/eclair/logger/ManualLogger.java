/*
 * Copyright 2018 Tinkoff Bank
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ru.tinkoff.eclair.logger;

import org.springframework.boot.logging.LogLevel;

import static org.springframework.boot.logging.LogLevel.*;

/**
 * Defines set of facade methods for manual logging by {@link EclairLogger}.
 * Contains all necessary variants of log levels reflected in the method name.
 * Every logger that needs to support manual logging should implement this interface.
 *
 * @author Vyacheslav Klapatnyuk
 */
public interface ManualLogger {

    /**
     * Determines if specified log level is enabled for logger by current invocation context.
     *
     * @param level checkable level
     * @return {@code true} if enabled, {@code false} otherwise
     */
    boolean isLogEnabled(LogLevel level);

    /**
     * Main manual logging method callable from other facade methods with 'if enabled' condition.
     *
     * @param level          expected log level
     * @param ifEnabledLevel lowest enabled level that allows logging
     * @param format         format string with Slf4J syntax
     * @param arguments      arguments for substituting into the format string
     */
    void log(LogLevel level, LogLevel ifEnabledLevel, String format, Object... arguments);

    /**
     * Main manual logging method callable from other facade methods.
     *
     * @param level     expected log level
     * @param format    format string with Slf4J syntax
     * @param arguments arguments for substituting into the format string
     */
    default void log(LogLevel level, String format, Object... arguments) {
        log(level, level, format, arguments);
    }

    /**
     * Determines if {@link LogLevel#TRACE} level is enabled for logger by current invocation context.
     *
     * @return {@code true} if enabled, {@code false} otherwise
     */
    default boolean isTraceLogEnabled() {
        return isLogEnabled(TRACE);
    }

    /**
     * Determines if {@link LogLevel#DEBUG} level is enabled for logger by current invocation context.
     *
     * @return {@code true} if enabled, {@code false} otherwise
     */
    default boolean isDebugLogEnabled() {
        return isLogEnabled(DEBUG);
    }

    /**
     * Determines if {@link LogLevel#INFO} level is enabled for logger by current invocation context.
     *
     * @return {@code true} if enabled, {@code false} otherwise
     */
    default boolean isInfoLogEnabled() {
        return isLogEnabled(INFO);
    }

    /**
     * Determines if {@link LogLevel#WARN} level is enabled for logger by current invocation context.
     *
     * @return {@code true} if enabled, {@code false} otherwise
     */
    default boolean isWarnLogEnabled() {
        return isLogEnabled(WARN);
    }

    /**
     * Determines if {@link LogLevel#ERROR} level is enabled for logger by current invocation context.
     *
     * @return {@code true} if enabled, {@code false} otherwise
     */
    default boolean isErrorLogEnabled() {
        return isLogEnabled(ERROR);
    }

    /**
     * Performs logging with {@link LogLevel#TRACE} level.
     *
     * @param format    format string with Slf4J syntax
     * @param arguments arguments for substituting into the format string
     */
    default void trace(String format, Object... arguments) {
        log(TRACE, format, arguments);
    }

    /**
     * Performs logging with {@link LogLevel#DEBUG} level.
     *
     * @param format    format string with Slf4J syntax
     * @param arguments arguments for substituting into the format string
     */
    default void debug(String format, Object... arguments) {
        log(DEBUG, format, arguments);
    }

    /**
     * Performs logging with {@link LogLevel#INFO} level.
     *
     * @param format    format string with Slf4J syntax
     * @param arguments arguments for substituting into the format string
     */
    default void info(String format, Object... arguments) {
        log(INFO, format, arguments);
    }

    /**
     * Performs logging with {@link LogLevel#WARN} level.
     *
     * @param format    format string with Slf4J syntax
     * @param arguments arguments for substituting into the format string
     */
    default void warn(String format, Object... arguments) {
        log(WARN, format, arguments);
    }

    /**
     * Performs logging with {@link LogLevel#ERROR} level.
     *
     * @param format    format string with Slf4J syntax
     * @param arguments arguments for substituting into the format string
     */
    default void error(String format, Object... arguments) {
        log(ERROR, format, arguments);
    }

    /**
     * Performs logging with {@link LogLevel#DEBUG} level if {@link LogLevel#TRACE} is enabled too.
     *
     * @param format    format string with Slf4J syntax
     * @param arguments arguments for substituting into the format string
     * @see #isLogEnabled(LogLevel)
     */
    default void debugIfTraceEnabled(String format, Object... arguments) {
        log(DEBUG, TRACE, format, arguments);
    }

    /**
     * Performs logging with {@link LogLevel#INFO} level if {@link LogLevel#DEBUG} is enabled too.
     *
     * @param format    format string with Slf4J syntax
     * @param arguments arguments for substituting into the format string
     * @see #isLogEnabled(LogLevel)
     */
    default void infoIfDebugEnabled(String format, Object... arguments) {
        log(INFO, DEBUG, format, arguments);
    }

    /**
     * Performs logging with {@link LogLevel#INFO} level if {@link LogLevel#TRACE} is enabled too.
     *
     * @param format    format string with Slf4J syntax
     * @param arguments arguments for substituting into the format string
     * @see #isLogEnabled(LogLevel)
     */
    default void infoIfTraceEnabled(String format, Object... arguments) {
        log(INFO, TRACE, format, arguments);
    }

    /**
     * Performs logging with {@link LogLevel#WARN} level if {@link LogLevel#INFO} is enabled too.
     *
     * @param format    format string with Slf4J syntax
     * @param arguments arguments for substituting into the format string
     * @see #isLogEnabled(LogLevel)
     */
    default void warnIfInfoEnabled(String format, Object... arguments) {
        log(WARN, INFO, format, arguments);
    }

    /**
     * Performs logging with {@link LogLevel#WARN} level if {@link LogLevel#DEBUG} is enabled too.
     *
     * @param format    format string with Slf4J syntax
     * @param arguments arguments for substituting into the format string
     * @see #isLogEnabled(LogLevel)
     */
    default void warnIfDebugEnabled(String format, Object... arguments) {
        log(WARN, DEBUG, format, arguments);
    }

    /**
     * Performs logging with {@link LogLevel#WARN} level if {@link LogLevel#TRACE} is enabled too.
     *
     * @param format    format string with Slf4J syntax
     * @param arguments arguments for substituting into the format string
     * @see #isLogEnabled(LogLevel)
     */
    default void warnIfTraceEnabled(String format, Object... arguments) {
        log(WARN, TRACE, format, arguments);
    }

    /**
     * Performs logging with {@link LogLevel#ERROR} level if {@link LogLevel#WARN} is enabled too.
     *
     * @param format    format string with Slf4J syntax
     * @param arguments arguments for substituting into the format string
     * @see #isLogEnabled(LogLevel)
     */
    default void errorIfWarnEnabled(String format, Object... arguments) {
        log(ERROR, WARN, format, arguments);
    }

    /**
     * Performs logging with {@link LogLevel#ERROR} level if {@link LogLevel#INFO} is enabled too.
     *
     * @param format    format string with Slf4J syntax
     * @param arguments arguments for substituting into the format string
     * @see #isLogEnabled(LogLevel)
     */
    default void errorIfInfoEnabled(String format, Object... arguments) {
        log(ERROR, INFO, format, arguments);
    }

    /**
     * Performs logging with {@link LogLevel#ERROR} level if {@link LogLevel#DEBUG} is enabled too.
     *
     * @param format    format string with Slf4J syntax
     * @param arguments arguments for substituting into the format string
     * @see #isLogEnabled(LogLevel)
     */
    default void errorIfDebugEnabled(String format, Object... arguments) {
        log(ERROR, DEBUG, format, arguments);
    }

    /**
     * Performs logging with {@link LogLevel#ERROR} level if {@link LogLevel#TRACE} is enabled too.
     *
     * @param format    format string with Slf4J syntax
     * @param arguments arguments for substituting into the format string
     * @see #isLogEnabled(LogLevel)
     */
    default void errorIfTraceEnabled(String format, Object... arguments) {
        log(ERROR, TRACE, format, arguments);
    }
}
