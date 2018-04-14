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

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import org.springframework.boot.logging.LogLevel;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.springframework.boot.logging.LogLevel.OFF;
import static org.springframework.boot.logging.LogLevel.values;

/**
 * @author Vyacheslav Klapatnyuk
 */
class ExampleAppender extends AppenderBase<ILoggingEvent> {

    private final Map<String, Map<LogLevel, List<ILoggingEvent>>> events = new LinkedHashMap<>();

    private LogLevel level = OFF;

    @Override
    protected void append(ILoggingEvent event) {
        events.computeIfAbsent(event.getLoggerName(), name -> initLoggerEvents()).get(level).add(event);
    }

    private Map<LogLevel, List<ILoggingEvent>> initLoggerEvents() {
        Map<LogLevel, List<ILoggingEvent>> events = new LinkedHashMap<>();
        Stream.of(values()).forEach(level -> events.put(level, new ArrayList<>()));
        return events;
    }

    void setLevel(LogLevel level) {
        this.level = level;
    }

    Map<LogLevel, List<ILoggingEvent>> getLoggerEvents(String loggerName) {
        return events.get(loggerName);
    }
}
