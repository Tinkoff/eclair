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
import ru.tinkoff.eclair.definition.method.MethodLog;
import ru.tinkoff.eclair.definition.OutLog;
import ru.tinkoff.eclair.logger.facade.LoggerFacadeFactory;
import ru.tinkoff.eclair.printer.Printer;
import ru.tinkoff.eclair.printer.ToStringPrinter;

import java.lang.reflect.Method;
import java.math.BigDecimal;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.boot.logging.LogLevel.*;

/**
 * @author Vyacheslav Klapatnyuk
 */
public class LogOutSimpleLoggerTest {

    private Method voidMethod;
    private Method voidObjectMethod;
    private Method method;

    @Before
    public void init() throws NoSuchMethodException {
        voidMethod = LogOutSimpleLoggerTest.class.getMethod("voidMethod");
        voidObjectMethod = LogOutSimpleLoggerTest.class.getMethod("voidObjectMethod");
        method = LogOutSimpleLoggerTest.class.getMethod("method");
    }

    @SuppressWarnings("unused")
    public void voidMethod() {
    }

    @SuppressWarnings("unused")
    public Void voidObjectMethod() {
        return null;
    }

    @SuppressWarnings("unused")
    public BigDecimal method() {
        return null;
    }

    @Test
    public void outLogIsNull() {
        // given, when
        SimpleLogger logger = new SimpleLoggerBuilder()
                .method(method)
                .effectiveLevel(DEBUG)
                .buildAndInvokeAndGet(null);
        // then
        verify(logger.getLoggerFacadeFactory().getLoggerFacade(any()), never()).log(any(), any());
    }

    @Test
    public void effectiveLevelIsGreaterThanLevelAndIfEnabledLevel() {
        // given, when
        SimpleLogger logger = new SimpleLoggerBuilder()
                .method(method)
                .levels(DEBUG, OFF, DEBUG)
                .effectiveLevel(INFO)
                .buildAndInvokeAndGet();
        // then
        verify(logger.getLoggerFacadeFactory().getLoggerFacade(any()), never()).log(any(), any());
    }

    @Test
    public void effectiveLevelIsOff() {
        // given, when
        SimpleLogger logger = new SimpleLoggerBuilder()
                .method(method)
                .levels(DEBUG, OFF, DEBUG)
                .effectiveLevel(OFF)
                .buildAndInvokeAndGet();
        // then
        verify(logger.getLoggerFacadeFactory().getLoggerFacade(any()), never()).log(any(), any());
    }

    @Test
    public void ifEnabledSmallerThanEffectiveLevel() {
        // given, when
        SimpleLogger logger = new SimpleLoggerBuilder()
                .method(method)
                .levels(DEBUG, TRACE, DEBUG)
                .effectiveLevel(DEBUG)
                .buildAndInvokeAndGet();
        // then
        verify(logger.getLoggerFacadeFactory().getLoggerFacade(any()), never()).log(any(), any());
    }

    @Test
    public void outLogIfEnabledLevelDenied() {
        // given, when
        SimpleLogger logger = new SimpleLoggerBuilder()
                .method(method)
                .levels(INFO, DEBUG, DEBUG)
                .effectiveLevel(INFO)
                .buildAndInvokeAndGet();
        // then
        verify(logger.getLoggerFacadeFactory().getLoggerFacade(any()), never()).log(any(), any());
    }

    @Test
    public void outLogVerboseLevelDenied() {
        // given, when
        SimpleLogger logger = new SimpleLoggerBuilder()
                .method(method)
                .levels(INFO, OFF, DEBUG)
                .effectiveLevel(INFO)
                .buildAndInvokeAndGet();
        // then
        verify(logger.getLoggerFacadeFactory().getLoggerFacade(any())).log(INFO, "<");
    }

    @Test
    public void testVoidMethod() {
        // given, when
        SimpleLogger logger = new SimpleLoggerBuilder()
                .method(voidMethod)
                .levels(DEBUG, OFF, DEBUG)
                .effectiveLevel(DEBUG)
                .buildAndInvokeAndGet();
        // then
        verify(logger.getLoggerFacadeFactory().getLoggerFacade(any())).log(DEBUG, "<");
    }

    @Test
    public void testVoidObjectMethod() {
        // given, when
        SimpleLogger logger = new SimpleLoggerBuilder()
                .method(voidObjectMethod)
                .levels(DEBUG, OFF, DEBUG)
                .effectiveLevel(DEBUG)
                .buildAndInvokeAndGet();
        // then
        verify(logger.getLoggerFacadeFactory().getLoggerFacade(any())).log(DEBUG, "<");
    }

    @Test
    public void testMethod() {
        // given, when
        SimpleLogger logger = new SimpleLoggerBuilder()
                .method(method)
                .result(new BigDecimal("123.456"))
                .levels(DEBUG, OFF, DEBUG)
                .effectiveLevel(DEBUG)
                .buildAndInvokeAndGet();
        // then
        verify(logger.getLoggerFacadeFactory().getLoggerFacade(any())).log(DEBUG, "< 123.456");
    }

    @Test
    public void printer() {
        // given
        Printer printer = new Printer() {
            @Override
            protected String serialize(Object input) {
                return "!";
            }
        };
        // when
        SimpleLogger logger = new SimpleLoggerBuilder()
                .method(method)
                .result(new BigDecimal("123.456"))
                .levels(DEBUG, OFF, DEBUG)
                .printer(printer)
                .effectiveLevel(DEBUG)
                .buildAndInvokeAndGet();
        // then
        verify(logger.getLoggerFacadeFactory().getLoggerFacade(any())).log(DEBUG, "< !");
    }

    @Test
    public void levelIsOff() {
        // given, when
        SimpleLogger logger = new SimpleLoggerBuilder()
                .method(method)
                .levels(OFF, OFF, DEBUG)
                .effectiveLevel(TRACE)
                .buildAndInvokeAndGet();
        // then
        verify(logger.getLoggerFacadeFactory().getLoggerFacade(any()), never()).log(any(), any());
    }

    @Test
    public void verboseLevelIsOff() {
        // given, when
        SimpleLogger logger = new SimpleLoggerBuilder()
                .method(method)
                .levels(DEBUG, OFF, OFF)
                .effectiveLevel(TRACE)
                .buildAndInvokeAndGet();
        // then
        verify(logger.getLoggerFacadeFactory().getLoggerFacade(any())).log(DEBUG, "<");
    }

    @Test
    public void returnNull() {
        // given, when
        SimpleLogger logger = new SimpleLoggerBuilder()
                .method(method)
                .result(null)
                .levels(DEBUG, OFF, DEBUG)
                .effectiveLevel(TRACE)
                .buildAndInvokeAndGet();
        // then
        verify(logger.getLoggerFacadeFactory().getLoggerFacade(any())).log(DEBUG, "< null");
    }

    @Test
    public void printerThrowsException() {
        // given
        Printer printer = mock(Printer.class);
        when(printer.print(any())).thenThrow(new RuntimeException());
        // when
        SimpleLogger logger = new SimpleLoggerBuilder()
                .method(method)
                .printer(printer)
                .result(new BigDecimal("123.456"))
                .levels(DEBUG, OFF, DEBUG)
                .effectiveLevel(TRACE)
                .buildAndInvokeAndGet();
        // then
        verify(logger.getLoggerFacadeFactory().getLoggerFacade(any())).log(DEBUG, "< 123.456");
    }

    private static class SimpleLoggerBuilder {

        private Method method;
        private Object result;
        private LogLevel level = DEBUG;
        private LogLevel ifEnabledLevel = OFF;
        private LogLevel verboseLevel = DEBUG;
        private Printer printer = new ToStringPrinter();
        private LogLevel effectiveLevel;

        private SimpleLoggerBuilder method(Method method) {
            this.method = method;
            return this;
        }

        private SimpleLoggerBuilder result(Object result) {
            this.result = result;
            return this;
        }

        private SimpleLoggerBuilder levels(LogLevel level, LogLevel ifEnabledLevel, LogLevel verboseLevel) {
            this.level = level;
            this.ifEnabledLevel = ifEnabledLevel;
            this.verboseLevel = verboseLevel;
            return this;
        }

        private SimpleLoggerBuilder printer(Printer printer) {
            this.printer = printer;
            return this;
        }

        private SimpleLoggerBuilder effectiveLevel(LogLevel effectiveLevel) {
            this.effectiveLevel = effectiveLevel;
            return this;
        }

        private SimpleLogger buildAndInvokeAndGet() {
            OutLog outLog = OutLog.builder()
                    .level(level)
                    .ifEnabledLevel(ifEnabledLevel)
                    .verboseLevel(verboseLevel)
                    .printer(printer)
                    .build();
            return buildAndInvokeAndGet(outLog);
        }

        private SimpleLogger buildAndInvokeAndGet(OutLog outLog) {
            MethodInvocation invocation = methodInvocation(method);
            SimpleLogger simpleLogger = new SimpleLogger(loggerFacadeFactory(), loggingSystem(effectiveLevel));
            simpleLogger.logOutIfNecessary(invocation, methodLog(outLog), result);
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

        private MethodLog methodLog(OutLog outLog) {
            MethodLog methodLog = mock(MethodLog.class);
            when(methodLog.getOutLog()).thenReturn(outLog);
            return methodLog;
        }
    }
}
