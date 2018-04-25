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

import org.aopalliance.intercept.MethodInvocation;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.logging.LogLevel;
import org.springframework.boot.logging.LoggerConfiguration;
import org.springframework.boot.logging.LoggingSystem;
import ru.tinkoff.eclair.definition.ErrorLog;
import ru.tinkoff.eclair.definition.OutLog;
import ru.tinkoff.eclair.definition.method.MethodLog;
import ru.tinkoff.eclair.logger.facade.LoggerFacadeFactory;
import ru.tinkoff.eclair.printer.ToStringPrinter;

import java.lang.reflect.Method;

import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.boot.logging.LogLevel.*;

/**
 * @author Vyacheslav Klapatnyuk
 */
public class LogErrorSimpleLoggerTest {

    private Method method;

    @Before
    public void init() throws NoSuchMethodException {
        method = LogErrorSimpleLoggerTest.class.getMethod("method");
    }

    @SuppressWarnings("unused")
    public void method() {
    }

    @Test
    public void errorLogAndOutLogAreNull() {
        // given
        Throwable throwable = new RuntimeException();
        // when
        SimpleLogger logger = new SimpleLoggerBuilder()
                .method(method)
                .throwable(throwable)
                .effectiveLevel(DEBUG)
                .buildAndInvokeAndGet(null, null);
        // then
        verify(logger.getLoggerFacadeFactory().getLoggerFacade(any()), never()).log(any(), any(), any());
        verify(logger.getLoggerFacadeFactory().getLoggerFacade(any()), never()).log(any(), any());
    }

    @Test
    public void effectiveLevelIsGreaterThanLevelAndIfEnabledLevel() {
        // given
        Throwable throwable = new RuntimeException();
        // when
        SimpleLogger logger = new SimpleLoggerBuilder()
                .method(method)
                .throwable(throwable)
                .levels(WARN, OFF, ERROR)
                .effectiveLevel(ERROR)
                .buildAndInvokeAndGet();
        // then
        verify(logger.getLoggerFacadeFactory().getLoggerFacade(any()), never()).log(any(), any(), any());
        verify(logger.getLoggerFacadeFactory().getLoggerFacade(any()), never()).log(any(), any());
    }

    @Test
    public void effectiveLevelIsOff() {
        // given
        Throwable throwable = new RuntimeException();
        // when
        SimpleLogger logger = new SimpleLoggerBuilder()
                .method(method)
                .throwable(throwable)
                .levels(ERROR, OFF, ERROR)
                .effectiveLevel(OFF)
                .buildAndInvokeAndGet();
        // then
        verify(logger.getLoggerFacadeFactory().getLoggerFacade(any()), never()).log(any(), any(), any());
        verify(logger.getLoggerFacadeFactory().getLoggerFacade(any()), never()).log(any(), any());
    }

    @Test
    public void ifEnabledSmallerThanEffectiveLevel() {
        // given
        Throwable throwable = new RuntimeException();
        // when
        SimpleLogger logger = new SimpleLoggerBuilder()
                .method(method)
                .throwable(throwable)
                .levels(ERROR, TRACE, ERROR)
                .effectiveLevel(DEBUG)
                .buildAndInvokeAndGet();
        // then
        verify(logger.getLoggerFacadeFactory().getLoggerFacade(any()), never()).log(any(), any(), any());
        verify(logger.getLoggerFacadeFactory().getLoggerFacade(any()), never()).log(any(), any());
    }

    @Test
    public void errorLogVerboseLevelDenied() {
        // given
        Throwable throwable = new RuntimeException();
        // when
        SimpleLogger logger = new SimpleLoggerBuilder()
                .method(method)
                .throwable(throwable)
                .levels(ERROR, OFF, DEBUG)
                .effectiveLevel(INFO)
                .buildAndInvokeAndGet();
        // then
        verify(logger.getLoggerFacadeFactory().getLoggerFacade(any())).log(ERROR, "!", throwable);
        verify(logger.getLoggerFacadeFactory().getLoggerFacade(any()), never()).log(any(), any());
    }

    @Test
    public void errorLog() {
        // given
        RuntimeException throwable = new RuntimeException("message");
        // when
        SimpleLogger logger = new SimpleLoggerBuilder()
                .method(method)
                .throwable(throwable)
                .effectiveLevel(DEBUG)
                .buildAndInvokeAndGet();
        // then
        verify(logger.getLoggerFacadeFactory().getLoggerFacade(any())).log(ERROR, "! java.lang.RuntimeException: message", throwable);
        verify(logger.getLoggerFacadeFactory().getLoggerFacade(any()), never()).log(any(), any());
    }

    @Test
    public void levelIsOff() {
        // given
        RuntimeException throwable = new RuntimeException();
        // when
        SimpleLogger logger = new SimpleLoggerBuilder()
                .method(method)
                .throwable(throwable)
                .levels(OFF, OFF, DEBUG)
                .effectiveLevel(TRACE)
                .buildAndInvokeAndGet();
        // then
        verify(logger.getLoggerFacadeFactory().getLoggerFacade(any()), never()).log(any(), any(), any());
        verify(logger.getLoggerFacadeFactory().getLoggerFacade(any()), never()).log(any(), any());
    }

    @Test
    public void verboseLevelIsOff() {
        // given
        RuntimeException throwable = new RuntimeException();
        // when
        SimpleLogger logger = new SimpleLoggerBuilder()
                .method(method)
                .throwable(throwable)
                .levels(ERROR, OFF, OFF)
                .effectiveLevel(TRACE)
                .buildAndInvokeAndGet();
        // then
        verify(logger.getLoggerFacadeFactory().getLoggerFacade(any())).log(ERROR, "!", throwable);
        verify(logger.getLoggerFacadeFactory().getLoggerFacade(any()), never()).log(any(), any());
    }

    @Test
    public void outLogInsteadOfErrorLog() {
        // given
        Throwable throwable = new RuntimeException();
        OutLog outLog = new OutLog(DEBUG, OFF, DEBUG, new ToStringPrinter());
        // when
        SimpleLogger logger = new SimpleLoggerBuilder()
                .method(method)
                .throwable(throwable)
                .effectiveLevel(DEBUG)
                .buildAndInvokeAndGet(null, outLog);
        // then
        verify(logger.getLoggerFacadeFactory().getLoggerFacade(any()), never()).log(any(), any(), any());
        verify(logger.getLoggerFacadeFactory().getLoggerFacade(any())).log(DEBUG, "!");
    }

    private static class SimpleLoggerBuilder {

        private static final ErrorLog.Filter defaultFilter = new ErrorLog.Filter(singleton(Throwable.class), emptySet());

        private Method method;

        private Throwable throwable;

        private LogLevel level = ERROR;
        private LogLevel ifEnabledLevel = OFF;
        private LogLevel verboseLevel = ERROR;

        /**
         * TODO: add test with differing values
         */
        private LogLevel outLevel = DEBUG;
        /**
         * TODO: add test with differing values
         */
        private LogLevel outIfEnabledLevel = OFF;
        /**
         * TODO: add test with differing values
         */
        private LogLevel outVerboseLevel = DEBUG;
        /**
         * TODO: add test with differing values
         */
        private ErrorLog.Filter filter = defaultFilter;

        private LogLevel effectiveLevel;

        private SimpleLoggerBuilder method(Method method) {
            this.method = method;
            return this;
        }

        private SimpleLoggerBuilder throwable(Throwable throwable) {
            this.throwable = throwable;
            return this;
        }

        private SimpleLoggerBuilder levels(LogLevel level, LogLevel ifEnabledLevel, LogLevel verboseLevel) {
            this.level = level;
            this.ifEnabledLevel = ifEnabledLevel;
            this.verboseLevel = verboseLevel;
            return this;
        }

        private SimpleLoggerBuilder effectiveLevel(LogLevel effectiveLevel) {
            this.effectiveLevel = effectiveLevel;
            return this;
        }

        private SimpleLogger buildAndInvokeAndGet() {
            ErrorLog errorLog = new ErrorLog(level, ifEnabledLevel, verboseLevel, filter);
            OutLog outLog = new OutLog(outLevel, outIfEnabledLevel, outVerboseLevel, new ToStringPrinter());
            return buildAndInvokeAndGet(errorLog, outLog);
        }

        private SimpleLogger buildAndInvokeAndGet(ErrorLog errorLog, OutLog outLog) {
            MethodInvocation invocation = methodInvocation(method);
            SimpleLogger simpleLogger = new SimpleLogger(loggerFacadeFactory(), loggingSystem(effectiveLevel));
            simpleLogger.logErrorIfNecessary(invocation, methodLog(outLog, errorLog), throwable);
            return simpleLogger;
        }

        private MethodInvocation methodInvocation(Method method) {
            MethodInvocation invocation = mock(MethodInvocation.class);
            when(invocation.getMethod()).thenReturn(method);
            return invocation;
        }

        private LoggerFacadeFactory loggerFacadeFactory() {
            return mock(LoggerFacadeFactory.class, RETURNS_DEEP_STUBS);
        }

        private LoggingSystem loggingSystem(LogLevel level) {
            LoggingSystem loggingSystem = mock(LoggingSystem.class);
            when(loggingSystem.getLoggerConfiguration(any())).thenReturn(new LoggerConfiguration("", null, level));
            return loggingSystem;
        }

        private MethodLog methodLog(OutLog outLog, ErrorLog errorLog) {
            MethodLog methodLog = mock(MethodLog.class);
            when(methodLog.getOutLog()).thenReturn(outLog);
            when(methodLog.findErrorLog(any())).thenReturn(errorLog);
            return methodLog;
        }
    }
}
