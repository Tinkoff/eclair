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

package ru.tinkoff.eclair.logger.facade;

import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;
import org.springframework.boot.logging.LogLevel;

import java.util.EnumMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * TODO: test this case
 */
public class JavaLoggerFacade implements LoggerFacade {

    private static final Map<LogLevel, Level> LEVELS = new EnumMap<>(LogLevel.class);

    private final Logger logger;

    static {
        LEVELS.put(LogLevel.TRACE, Level.FINEST);
        LEVELS.put(LogLevel.DEBUG, Level.FINE);
        LEVELS.put(LogLevel.INFO, Level.INFO);
        LEVELS.put(LogLevel.WARN, Level.WARNING);
        LEVELS.put(LogLevel.ERROR, Level.SEVERE);
        LEVELS.put(LogLevel.FATAL, Level.SEVERE);
        LEVELS.put(LogLevel.OFF, Level.OFF);
    }

    public JavaLoggerFacade(Logger logger) {
        this.logger = logger;
    }

    @Override
    public void log(LogLevel level, String format, Object... arguments) {
        FormattingTuple formattingTuple = MessageFormatter.arrayFormat(format, arguments);
        String message = formattingTuple.getMessage();
        Throwable throwable = formattingTuple.getThrowable();
        logger.log(LEVELS.get(level), message, throwable);
    }
}
