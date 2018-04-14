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

import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;
import org.springframework.boot.logging.LogLevel;

import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.joining;

/**
 * @author Vyacheslav Klapatnyuk
 */
class ExampleTableBuilder {

    static final String TABLE_HEADER = "Enabled level|Log sample\n---|---\n";

    private PatternLayout patternLayout;

    void setPatternLayout(PatternLayout patternLayout) {
        this.patternLayout = patternLayout;
    }

    String buildSampleCell(List<ILoggingEvent> events) {
        if (events.isEmpty()) {
            return "-";
        }
        return events.stream().map(patternLayout::doLayout).map(s -> '`' + s + '`').collect(joining("<br>"));
    }

    String buildTable(Map<String, List<LogLevel>> levels) {
        String body = buildTableBody(levels);
        return TABLE_HEADER + body;
    }

    private String buildTableBody(Map<String, List<LogLevel>> levels) {
        return levels.entrySet().stream()
                .map(entry -> buildLevelsCell(entry.getValue()) + "|" + entry.getKey())
                .collect(joining("\n"));
    }

    private String buildLevelsCell(List<LogLevel> logLevels) {
        return '`' + logLevels.stream().map(Enum::name).collect(joining(" > ")) + '`';
    }
}
