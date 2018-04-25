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

package ru.tinkoff.eclair.definition;

import org.springframework.boot.logging.LogLevel;
import ru.tinkoff.eclair.annotation.Log;
import ru.tinkoff.eclair.printer.Printer;

import java.util.List;

import static java.util.Collections.unmodifiableList;

/**
 * Simple DTO matches to @Log.in annotation.
 *
 * @author Vyacheslav Klapatnyuk
 * @see Log.in
 */
public class InLog implements LogDefinition {

    private final LogLevel level;
    private final LogLevel ifEnabledLevel;
    private final LogLevel verboseLevel;
    private final List<Printer> printers;

    public InLog(LogLevel level, LogLevel ifEnabledLevel, LogLevel verboseLevel, List<Printer> printers) {
        this.level = level;
        this.ifEnabledLevel = ifEnabledLevel;
        this.verboseLevel = verboseLevel;
        this.printers = unmodifiableList(printers);
    }

    @Override
    public LogLevel getLevel() {
        return level;
    }

    @Override
    public LogLevel getIfEnabledLevel() {
        return ifEnabledLevel;
    }

    @Override
    public LogLevel getVerboseLevel() {
        return verboseLevel;
    }

    public List<Printer> getPrinters() {
        return printers;
    }
}
