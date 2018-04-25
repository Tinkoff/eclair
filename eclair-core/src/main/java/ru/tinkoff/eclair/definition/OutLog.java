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
import ru.tinkoff.eclair.printer.Printer;

/**
 * @author Vyacheslav Klapatnyuk
 */
public class OutLog implements LogDefinition {

    private LogLevel level;
    private LogLevel ifEnabledLevel;
    private LogLevel verboseLevel;
    private Printer printer;

    public OutLog(LogLevel level, LogLevel ifEnabledLevel, LogLevel verboseLevel, Printer printer) {
        this.level = level;
        this.ifEnabledLevel = ifEnabledLevel;
        this.verboseLevel = verboseLevel;
        this.printer = printer;
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

    public Printer getPrinter() {
        return printer;
    }
}
