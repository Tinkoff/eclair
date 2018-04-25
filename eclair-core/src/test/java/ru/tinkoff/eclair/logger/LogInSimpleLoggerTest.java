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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.aopalliance.intercept.MethodInvocation;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.logging.LogLevel;
import org.springframework.boot.logging.LoggerConfiguration;
import org.springframework.boot.logging.LoggingSystem;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import ru.tinkoff.eclair.definition.InLog;
import ru.tinkoff.eclair.definition.ParameterLog;
import ru.tinkoff.eclair.definition.method.MethodLog;
import ru.tinkoff.eclair.logger.facade.LoggerFacadeFactory;
import ru.tinkoff.eclair.printer.JacksonPrinter;
import ru.tinkoff.eclair.printer.Jaxb2Printer;
import ru.tinkoff.eclair.printer.Printer;
import ru.tinkoff.eclair.printer.ToStringPrinter;
import ru.tinkoff.eclair.printer.resolver.PrinterResolver;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.nCopies;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.boot.logging.LogLevel.*;

/**
 * @author Vyacheslav Klapatnyuk
 */
public class LogInSimpleLoggerTest {

    private Method method;
    private Method methodWithParameters;

    @Before
    public void init() throws NoSuchMethodException {
        method = LogInSimpleLoggerTest.class.getDeclaredMethod("method");
        methodWithParameters = LogInSimpleLoggerTest.class.getDeclaredMethod("methodWithParameters", String.class, Integer.class, Dto.class);
    }

    @SuppressWarnings("unused")
    private void method() {
    }

    @SuppressWarnings("unused")
    private void methodWithParameters(String s, Integer i, Dto dto) {
    }

    @Test
    public void inLogIsNull() {
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
    public void methodWithoutParameters() {
        // given, when
        SimpleLogger logger = new SimpleLoggerBuilder()
                .method(method)
                .levels(DEBUG, OFF, DEBUG)
                .effectiveLevel(DEBUG)
                .buildAndInvokeAndGet();
        // then
        verify(logger.getLoggerFacadeFactory().getLoggerFacade(any())).log(DEBUG, ">");
    }

    @Test
    public void parameterLogsAreNull() {
        // given, when
        SimpleLogger logger = new SimpleLoggerBuilder()
                .method(methodWithParameters)
                .parameterNames("s", "i", "dto")
                .arguments("s", 1, new Dto())
                .levels(DEBUG, OFF, DEBUG)
                .parameterLogs(null, null, null)
                .effectiveLevel(DEBUG)
                .buildAndInvokeAndGet();
        // then
        verify(logger.getLoggerFacadeFactory().getLoggerFacade(any())).log(DEBUG, "> s=\"s\", i=1, dto=Dto{i=0, s='null'}");
    }

    @Test
    public void parameterLogsWithoutParameterNames() {
        // given
        Printer printer = new ToStringPrinter();
        // when
        SimpleLogger logger = new SimpleLoggerBuilder()
                .method(methodWithParameters)
                .arguments("s", 1, new Dto())
                .parameterLog(DEBUG, OFF, TRACE, printer)
                .parameterLog(DEBUG, OFF, TRACE, printer)
                .parameterLog(DEBUG, OFF, TRACE, printer)
                .effectiveLevel(DEBUG)
                .buildAndInvokeAndGet(null);
        // then
        verify(logger.getLoggerFacadeFactory().getLoggerFacade(any())).log(DEBUG, "> \"s\", 1, Dto{i=0, s='null'}");
    }

    @Test
    public void parameterLogsWithoutParameterNamesAndSkippedArgument() {
        // given
        Printer printer = new ToStringPrinter();
        // when
        SimpleLogger logger = new SimpleLoggerBuilder()
                .method(methodWithParameters)
                .parameterNames(null, null, null)
                .arguments("s", 1, new Dto())
                .parameterLog(DEBUG, OFF, DEBUG, printer)
                .parameterLog(null)
                .parameterLog(DEBUG, OFF, DEBUG, printer)
                .effectiveLevel(DEBUG)
                .buildAndInvokeAndGet(null);
        // then
        verify(logger.getLoggerFacadeFactory().getLoggerFacade(any())).log(DEBUG, "> \"s\", 2=Dto{i=0, s='null'}");
    }

    @Test
    public void parameterLogsAreNullIfEnabledSmallerThanEffectiveLevel() {
        // given, when
        SimpleLogger logger = new SimpleLoggerBuilder()
                .method(methodWithParameters)
                .arguments("s", 1, new Dto())
                .levels(DEBUG, TRACE, DEBUG)
                .parameterLogs(null, null, null)
                .effectiveLevel(DEBUG)
                .buildAndInvokeAndGet();
        // then
        verify(logger.getLoggerFacadeFactory().getLoggerFacade(any()), never()).log(any(), any());
    }

    @Test
    public void firstParameterLogIsNull() {
        // given
        Printer printer = new ToStringPrinter();
        // when
        SimpleLogger logger = new SimpleLoggerBuilder()
                .method(methodWithParameters)
                .parameterNames("s", "i", "dto")
                .arguments("s", 1, new Dto())
                .levels(DEBUG, OFF, DEBUG)
                .parameterLog(null)
                .parameterLog(DEBUG, OFF, DEBUG, printer)
                .parameterLog(DEBUG, OFF, DEBUG, printer)
                .effectiveLevel(DEBUG)
                .buildAndInvokeAndGet();
        // then
        verify(logger.getLoggerFacadeFactory().getLoggerFacade(any())).log(DEBUG, "> s=\"s\", i=1, dto=Dto{i=0, s='null'}");
    }

    @Test
    public void middleParameterLogIsNull() {
        // given
        Printer printer = new ToStringPrinter();
        // when
        SimpleLogger logger = new SimpleLoggerBuilder()
                .method(methodWithParameters)
                .parameterNames("s", "i", "dto")
                .arguments("s", 1, new Dto())
                .levels(DEBUG, OFF, DEBUG)
                .parameterLog(DEBUG, OFF, DEBUG, printer)
                .parameterLog(null)
                .parameterLog(DEBUG, OFF, DEBUG, printer)
                .effectiveLevel(DEBUG)
                .buildAndInvokeAndGet();
        // then
        verify(logger.getLoggerFacadeFactory().getLoggerFacade(any())).log(DEBUG, "> s=\"s\", i=1, dto=Dto{i=0, s='null'}");
    }

    @Test
    public void lastParameterLogIsNull() {
        // given
        Printer printer = new ToStringPrinter();
        // when
        SimpleLogger logger = new SimpleLoggerBuilder()
                .method(methodWithParameters)
                .parameterNames("s", "i", "dto")
                .arguments("s", 1, new Dto())
                .levels(DEBUG, OFF, DEBUG)
                .parameterLog(DEBUG, OFF, DEBUG, printer)
                .parameterLog(DEBUG, OFF, DEBUG, printer)
                .parameterLog(null)
                .effectiveLevel(DEBUG)
                .buildAndInvokeAndGet();
        // then
        verify(logger.getLoggerFacadeFactory().getLoggerFacade(any())).log(DEBUG, "> s=\"s\", i=1, dto=Dto{i=0, s='null'}");
    }

    @Test
    public void someParameterLogLevelSmallerThanEffectiveLevel() {
        // given
        Printer printer = new ToStringPrinter();
        // when
        SimpleLogger logger = new SimpleLoggerBuilder()
                .method(methodWithParameters)
                .parameterNames("s", "i", "dto")
                .arguments("s", 1, new Dto())
                .levels(INFO, OFF, DEBUG)
                .parameterLog(DEBUG, OFF, DEBUG, printer)
                .parameterLog(INFO, OFF, INFO, printer)
                .parameterLog(TRACE, OFF, DEBUG, printer)
                .effectiveLevel(INFO)
                .buildAndInvokeAndGet();
        // then
        verify(logger.getLoggerFacadeFactory().getLoggerFacade(any())).log(INFO, "> i=1");
    }

    @Test
    public void inLogIsNullFirstParameterLogIsNullLastParameterLogNotEnabled() {
        // given
        Printer printer = new ToStringPrinter();
        // when
        SimpleLogger logger = new SimpleLoggerBuilder()
                .method(methodWithParameters)
                .parameterNames("s", "i", "dto")
                .arguments("s", 1, new Dto())
                .parameterLog(null)
                .parameterLog(INFO, OFF, INFO, printer)
                .parameterLog(DEBUG, OFF, DEBUG, printer)
                .effectiveLevel(INFO)
                .buildAndInvokeAndGet(null);
        // then
        verify(logger.getLoggerFacadeFactory().getLoggerFacade(any())).log(INFO, "> i=1");
    }

    @Test
    public void inLogIsNullMaximumParameterLogEnabledLevelFound() {
        // given
        Printer printer = new ToStringPrinter();
        // when
        SimpleLogger logger = new SimpleLoggerBuilder()
                .method(methodWithParameters)
                .parameterNames("s", "i", "dto")
                .arguments("s", 1, new Dto())
                .parameterLog(null)
                .parameterLog(DEBUG, OFF, DEBUG, printer)
                .parameterLog(INFO, OFF, DEBUG, printer)
                .effectiveLevel(DEBUG)
                .buildAndInvokeAndGet(null);
        // then
        verify(logger.getLoggerFacadeFactory().getLoggerFacade(any())).log(INFO, "> i=1, dto=Dto{i=0, s='null'}");
    }

    @Test
    public void parameterLogsAreNullInLogIfEnabledLevelDenied() {
        // given, when
        SimpleLogger logger = new SimpleLoggerBuilder()
                .method(methodWithParameters)
                .arguments("s", 1, new Dto())
                .levels(INFO, DEBUG, DEBUG)
                .parameterLogs(null, null, null)
                .effectiveLevel(INFO)
                .buildAndInvokeAndGet();
        // then
        verify(logger.getLoggerFacadeFactory().getLoggerFacade(any()), never()).log(any(), any());
    }

    @Test
    public void parameterLogLevelsAreDenied() {
        // given
        Printer printer = new ToStringPrinter();
        // when
        SimpleLogger logger = new SimpleLoggerBuilder()
                .method(methodWithParameters)
                .levels(INFO, OFF, DEBUG)
                .arguments("s", 1, new Dto())
                .parameterLog(DEBUG, OFF, DEBUG, printer)
                .parameterLog(DEBUG, OFF, DEBUG, printer)
                .parameterLog(DEBUG, OFF, DEBUG, printer)
                .parameterLogs(null, null, null)
                .effectiveLevel(INFO)
                .buildAndInvokeAndGet();
        // then
        verify(logger.getLoggerFacadeFactory().getLoggerFacade(any())).log(INFO, ">");
    }

    @Test
    public void parameterLogsAreNullInLogVerboseLevelDenied() {
        // given, when
        SimpleLogger logger = new SimpleLoggerBuilder()
                .method(methodWithParameters)
                .arguments("s", 1, new Dto())
                .levels(INFO, OFF, DEBUG)
                .parameterLogs(null, null, null)
                .effectiveLevel(INFO)
                .buildAndInvokeAndGet();
        // then
        verify(logger.getLoggerFacadeFactory().getLoggerFacade(any())).log(INFO, ">");
    }

    @Test
    public void argumentsAreNull() {
        // given, when
        SimpleLogger logger = new SimpleLoggerBuilder()
                .method(methodWithParameters)
                .parameterNames("s", "i", "dto")
                .arguments(null, null, null)
                .levels(DEBUG, OFF, DEBUG)
                .parameterLogs(null, null, null)
                .effectiveLevel(DEBUG)
                .buildAndInvokeAndGet();
        // then
        verify(logger.getLoggerFacadeFactory().getLoggerFacade(any())).log(DEBUG, "> s=null, i=null, dto=null");
    }

    @Test
    public void printers() {
        // given
        Printer inLogPrinter = new Printer() {
            @Override
            protected String serialize(Object input) {
                return "!";
            }
        };
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setClassesToBeBound(Dto.class);
        // when
        SimpleLogger logger = new SimpleLoggerBuilder()
                .method(methodWithParameters)
                .parameterNames("s", "i", "dto")
                .arguments("s", 1, new Dto())
                .levels(DEBUG, OFF, DEBUG)
                .printers(nCopies(3, inLogPrinter))
                .parameterLog(DEBUG, OFF, DEBUG, new JacksonPrinter(new ObjectMapper()))
                .parameterLog(null)
                .parameterLog(DEBUG, OFF, DEBUG, new Jaxb2Printer(marshaller))
                .effectiveLevel(DEBUG)
                .buildAndInvokeAndGet();
        // then
        verify(logger.getLoggerFacadeFactory().getLoggerFacade(any())).log(DEBUG, "> s=\"s\", i=!, dto=<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><dto><i>0</i></dto>");
    }

    @Test
    public void levelIsOff() {
        // given, when
        SimpleLogger logger = new SimpleLoggerBuilder()
                .method(methodWithParameters)
                .parameterNames("s", "i", "dto")
                .arguments("s", 1, new Dto())
                .levels(OFF, OFF, DEBUG)
                .parameterLogs(null, null, null)
                .effectiveLevel(TRACE)
                .buildAndInvokeAndGet();
        // then
        verify(logger.getLoggerFacadeFactory().getLoggerFacade(any()), never()).log(any(), any());
    }

    @Test
    public void offLevelWithArgs() {
        // given
        Printer printer = new ToStringPrinter();
        // when
        SimpleLogger logger = new SimpleLoggerBuilder()
                .method(methodWithParameters)
                .parameterNames("s", "i", "dto")
                .arguments("s", 1, new Dto())
                .levels(OFF, OFF, DEBUG)
                .parameterLog(DEBUG, OFF, DEBUG, printer)
                .parameterLog(OFF, OFF, DEBUG, printer)
                .parameterLog(INFO, OFF, TRACE, printer)
                .effectiveLevel(DEBUG)
                .buildAndInvokeAndGet();
        // then
        verify(logger.getLoggerFacadeFactory().getLoggerFacade(any())).log(INFO, "> s=\"s\", Dto{i=0, s='null'}");
    }

    @Test
    public void parameterLogLevelIsOff() {
        // given
        Printer printer = new ToStringPrinter();
        // when
        SimpleLogger logger = new SimpleLoggerBuilder()
                .method(methodWithParameters)
                .parameterNames("s", "i", "dto")
                .arguments("s", 1, new Dto())
                .levels(DEBUG, OFF, DEBUG)
                .parameterLog(OFF, OFF, DEBUG, printer)
                .parameterLog(OFF, OFF, DEBUG, printer)
                .parameterLog(OFF, OFF, DEBUG, printer)
                .effectiveLevel(TRACE)
                .buildAndInvokeAndGet();
        // then
        verify(logger.getLoggerFacadeFactory().getLoggerFacade(any())).log(DEBUG, ">");
    }

    @Test
    public void verboseLevelIsOff() {
        // given, when
        SimpleLogger logger = new SimpleLoggerBuilder()
                .method(methodWithParameters)
                .parameterNames("s", "i", "dto")
                .arguments("s", 1, new Dto())
                .levels(DEBUG, OFF, OFF)
                .parameterLogs(null, null, null)
                .effectiveLevel(TRACE)
                .buildAndInvokeAndGet();
        // then
        verify(logger.getLoggerFacadeFactory().getLoggerFacade(any())).log(DEBUG, ">");
    }

    @Test
    public void inLogAndParameterLogLevelsOff() {
        // given
        Printer printer = new ToStringPrinter();
        // when
        SimpleLogger logger = new SimpleLoggerBuilder()
                .method(methodWithParameters)
                .parameterNames("s", "i", "dto")
                .arguments("s", 1, new Dto())
                .levels(OFF, OFF, DEBUG)
                .parameterLog(OFF, OFF, DEBUG, printer)
                .parameterLog(OFF, OFF, DEBUG, printer)
                .parameterLog(OFF, OFF, DEBUG, printer)
                .effectiveLevel(TRACE)
                .buildAndInvokeAndGet();
        // then
        verify(logger.getLoggerFacadeFactory().getLoggerFacade(any()), never()).log(any(), any());
    }

    @Test
    public void printerThrowsException() {
        // given
        Printer printer = mock(Printer.class);
        when(printer.print(any())).thenThrow(new RuntimeException());
        // when
        SimpleLogger logger = new SimpleLoggerBuilder()
                .method(methodWithParameters)
                .parameterNames("s", "i", "dto")
                .arguments("s", 1, new Dto())
                .levels(DEBUG, OFF, DEBUG)
                .parameterLog(DEBUG, OFF, DEBUG, printer)
                .parameterLog(DEBUG, OFF, DEBUG, printer)
                .parameterLog(DEBUG, OFF, DEBUG, printer)
                .effectiveLevel(TRACE)
                .buildAndInvokeAndGet();
        // then
        verify(logger.getLoggerFacadeFactory().getLoggerFacade(any())).log(DEBUG, "> s=\"s\", i=1, dto=Dto{i=0, s='null'}");
    }

    private static class SimpleLoggerBuilder {

        private final List<ParameterLog> parameterLogs = new ArrayList<>();

        private Method method;
        private List<Object> arguments = emptyList();
        private LogLevel level = DEBUG;
        private LogLevel ifEnabledLevel = OFF;
        private LogLevel verboseLevel = DEBUG;
        private List<Printer> printers = emptyList();
        private LogLevel effectiveLevel;
        private List<String> parameterNames;

        private SimpleLoggerBuilder method(Method method) {
            this.method = method;
            return this;
        }

        private SimpleLoggerBuilder arguments(Object... argument) {
            this.arguments = asList(argument);
            return this;
        }

        private SimpleLoggerBuilder levels(LogLevel level, LogLevel ifEnabledLevel, LogLevel verboseLevel) {
            this.level = level;
            this.ifEnabledLevel = ifEnabledLevel;
            this.verboseLevel = verboseLevel;
            return this;
        }

        private SimpleLoggerBuilder printers(List<Printer> printers) {
            this.printers = printers;
            return this;
        }

        /**
         * TODO: add tests with 'ifEnabledLevel != LogLevel.OFF'
         */
        private SimpleLoggerBuilder parameterLog(LogLevel level, LogLevel ifEnabledLevel, LogLevel verboseLevel, Printer printer) {
            ParameterLog parameterLog = new ParameterLog(level, ifEnabledLevel, verboseLevel, printer);
            this.parameterLogs.add(parameterLog);
            return this;
        }

        private SimpleLoggerBuilder parameterLog(ParameterLog parameterLog) {
            this.parameterLogs.add(parameterLog);
            return this;
        }

        private SimpleLoggerBuilder parameterLogs(ParameterLog parameterLog0, ParameterLog parameterLog1, ParameterLog parameterLog2) {
            return parameterLog(parameterLog0)
                    .parameterLog(parameterLog1)
                    .parameterLog(parameterLog2);
        }

        private SimpleLoggerBuilder effectiveLevel(LogLevel effectiveLevel) {
            this.effectiveLevel = effectiveLevel;
            return this;
        }

        private SimpleLoggerBuilder parameterNames(String... parameterNames) {
            this.parameterNames = Arrays.asList(parameterNames);
            return this;
        }

        private SimpleLogger buildAndInvokeAndGet() {
            List<Printer> printers = this.printers.isEmpty() ? nCopies(arguments.size(), PrinterResolver.defaultPrinter) : this.printers;
            InLog inLog = new InLog(level, ifEnabledLevel, verboseLevel, printers);
            return buildAndInvokeAndGet(inLog);
        }

        private SimpleLogger buildAndInvokeAndGet(InLog inLog) {
            MethodInvocation invocation = methodInvocation(method, arguments.toArray());
            SimpleLogger simpleLogger = new SimpleLogger(loggerFacadeFactory(), loggingSystem(effectiveLevel));
            simpleLogger.logInIfNecessary(invocation, methodLog(inLog, parameterLogs, parameterNames));
            return simpleLogger;
        }

        private MethodInvocation methodInvocation(Method method, Object... arguments) {
            MethodInvocation invocation = mock(MethodInvocation.class);
            when(invocation.getMethod()).thenReturn(method);
            when(invocation.getArguments()).thenReturn(arguments);
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

        private MethodLog methodLog(InLog inLog, List<ParameterLog> parameterLogs, List<String> parameterNames) {
            MethodLog methodLog = mock(MethodLog.class);
            when(methodLog.getInLog()).thenReturn(inLog);
            when(methodLog.getParameterLogs()).thenReturn(parameterLogs);
            when(methodLog.getParameterNames()).thenReturn(parameterNames);
            return methodLog;
        }
    }

    @XmlRootElement
    @XmlType(name = "dto")
    public static class Dto {

        private int i;
        private String s;

        public int getI() {
            return i;
        }

        public void setI(int i) {
            this.i = i;
        }

        public String getS() {
            return s;
        }

        public void setS(String s) {
            this.s = s;
        }

        @Override
        public String toString() {
            return "Dto{i=" + i + ", s='" + s + "'}";
        }
    }
}
