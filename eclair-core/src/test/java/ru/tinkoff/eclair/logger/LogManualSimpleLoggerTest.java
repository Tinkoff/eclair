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

import org.junit.Test;
import org.springframework.boot.logging.LogLevel;
import org.springframework.boot.logging.LoggerConfiguration;
import org.springframework.boot.logging.LoggingSystem;
import ru.tinkoff.eclair.logger.facade.LoggerFacadeFactory;

import java.math.BigDecimal;
import java.util.function.Supplier;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.boot.logging.LogLevel.*;

/**
 * @author Vyacheslav Klapatnyuk
 */
public class LogManualSimpleLoggerTest {

    @Test
    public void effectiveLevelIsGreaterThanLevelAndIfEnabledLevel() {
        // given, when
        SimpleLogger logger = new SimpleLoggerBuilder()
                .effectiveLevel(INFO)
                .levels(DEBUG, DEBUG)
                .format("{}")
                .arguments("argument")
                .buildAndInvokeAndGet();
        // then
        verify(logger.getLoggerFacadeFactory().getLoggerFacade(any()), never()).log(any(), any(), any());
    }

    @Test
    public void effectiveLevelIsOff() {
        // given, when
        SimpleLogger logger = new SimpleLoggerBuilder()
                .effectiveLevel(OFF)
                .levels(DEBUG, DEBUG)
                .format("{}")
                .arguments("argument")
                .buildAndInvokeAndGet();
        // then
        verify(logger.getLoggerFacadeFactory().getLoggerFacade(any()), never()).log(any(), any(), any());
    }

    @Test
    public void logNull() {
        // given, when
        SimpleLogger logger = new SimpleLoggerBuilder()
                .effectiveLevel(DEBUG)
                .levels(DEBUG, DEBUG)
                .format(null)
                .arguments()
                .buildAndInvokeAndGet();
        // then
        verify(logger.getLoggerFacadeFactory().getLoggerFacade(any())).log(DEBUG, "- null");
    }

    @Test
    public void logNullArguments() {
        // given, when
        SimpleLogger logger = new SimpleLoggerBuilder()
                .effectiveLevel(DEBUG)
                .levels(DEBUG, DEBUG)
                .format("{} {}")
                .arguments(null, null)
                .buildAndInvokeAndGet();
        // then
        verify(logger.getLoggerFacadeFactory().getLoggerFacade(any())).log(DEBUG, "- {} {}", null, null);
    }

    @Test
    public void levelIsOff() {
        // given, when
        SimpleLogger logger = new SimpleLoggerBuilder()
                .effectiveLevel(DEBUG)
                .levels(OFF, DEBUG)
                .format("format")
                .arguments()
                .buildAndInvokeAndGet();
        // then
        verify(logger.getLoggerFacadeFactory().getLoggerFacade(any()), never()).log(any(), any());
    }

    @Test
    public void ifEnabledLevelIsOff() {
        // given, when
        SimpleLogger logger = new SimpleLoggerBuilder()
                .effectiveLevel(DEBUG)
                .levels(DEBUG, OFF)
                .format("format")
                .arguments()
                .buildAndInvokeAndGet();
        // then
        verify(logger.getLoggerFacadeFactory().getLoggerFacade(any()), never()).log(any(), any());
    }

    @Test
    public void logWithSuppliers() {
        // given, when
        SimpleLogger logger = new SimpleLoggerBuilder()
                .effectiveLevel(DEBUG)
                .levels(DEBUG, DEBUG)
                .format("{} {} {}")
                .arguments(
                        (Supplier<String>) () -> "0",
                        1,
                        (Supplier<BigDecimal>) () -> BigDecimal.ONE
                )
                .buildAndInvokeAndGet();
        // then
        verify(logger.getLoggerFacadeFactory().getLoggerFacade(any())).log(DEBUG, "- {} {} {}", "0", 1, BigDecimal.ONE);
    }

    private static class SimpleLoggerBuilder {

        private LogLevel level;
        private LogLevel ifEnabledLevel;
        private String format;
        private Object[] arguments;
        private LogLevel effectiveLevel;

        private SimpleLoggerBuilder levels(LogLevel level, LogLevel ifEnabledLevel) {
            this.level = level;
            this.ifEnabledLevel = ifEnabledLevel;
            return this;
        }

        private SimpleLoggerBuilder format(String format) {
            this.format = format;
            return this;
        }

        private SimpleLoggerBuilder arguments(Object... argument) {
            this.arguments = argument;
            return this;
        }

        private SimpleLoggerBuilder effectiveLevel(LogLevel effectiveLevel) {
            this.effectiveLevel = effectiveLevel;
            return this;
        }

        private SimpleLogger buildAndInvokeAndGet() {
            SimpleLogger simpleLogger = new SimpleLogger(loggerFacadeFactory(), loggingSystem(effectiveLevel));
            simpleLogger.log(level, ifEnabledLevel, format, arguments);
            return simpleLogger;
        }

        private LoggerFacadeFactory loggerFacadeFactory() {
            return mock(LoggerFacadeFactory.class, RETURNS_DEEP_STUBS);
        }

        private LoggingSystem loggingSystem(LogLevel level) {
            LoggingSystem loggingSystem = mock(LoggingSystem.class);
            when(loggingSystem.getLoggerConfiguration(any())).thenReturn(new LoggerConfiguration("", null, level));
            return loggingSystem;
        }
    }
}
