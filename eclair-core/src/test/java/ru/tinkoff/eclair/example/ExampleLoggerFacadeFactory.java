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

package ru.tinkoff.eclair.example;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import org.slf4j.LoggerFactory;
import ru.tinkoff.eclair.logger.facade.LoggerFacade;
import ru.tinkoff.eclair.logger.facade.LoggerFacadeFactory;
import ru.tinkoff.eclair.logger.facade.Slf4JLoggerFacade;

/**
 * @author Vyacheslav Klapatnyuk
 */
class ExampleLoggerFacadeFactory implements LoggerFacadeFactory {

    private final Appender<ILoggingEvent> appender;

    ExampleLoggerFacadeFactory(Appender<ILoggingEvent> appender) {
        this.appender = appender;
    }

    @Override
    public LoggerFacade getLoggerFacade(String loggerName) {
        Logger logger = (Logger) LoggerFactory.getLogger(loggerName);
        logger.addAppender(appender);
        return new Slf4JLoggerFacade(logger);
    }
}
