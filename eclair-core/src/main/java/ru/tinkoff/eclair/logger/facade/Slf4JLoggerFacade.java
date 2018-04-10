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

import org.slf4j.Logger;
import org.springframework.boot.logging.LogLevel;

/**
 * @author Vyacheslav Klapatnyuk
 */
public class Slf4JLoggerFacade implements LoggerFacade {

    private final Logger logger;

    public Slf4JLoggerFacade(Logger logger) {
        this.logger = logger;
    }

    @Override
    public void log(LogLevel level, String format, Object... arguments) {
        switch (level) {
            case OFF:
                break;
            case FATAL:
            case ERROR:
                logger.error(format, arguments);
                break;
            case WARN:
                logger.warn(format, arguments);
                break;
            case INFO:
                logger.info(format, arguments);
                break;
            case DEBUG:
                logger.debug(format, arguments);
                break;
            case TRACE:
                logger.trace(format, arguments);
                break;
            default:
                throw new IllegalArgumentException("Unexpected logging level: " + level);
        }
    }
}
