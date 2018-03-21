package ru.tinkoff.eclair.logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.aopalliance.intercept.MethodInvocation;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.logging.LogLevel;
import org.springframework.boot.logging.LoggerConfiguration;
import org.springframework.boot.logging.LoggingSystem;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import ru.tinkoff.eclair.definition.ArgLog;
import ru.tinkoff.eclair.definition.InLog;
import ru.tinkoff.eclair.definition.LogPack;
import ru.tinkoff.eclair.logger.facade.LoggerFacadeFactory;
import ru.tinkoff.eclair.printer.JacksonPrinter;
import ru.tinkoff.eclair.printer.Jaxb2Printer;
import ru.tinkoff.eclair.printer.Printer;
import ru.tinkoff.eclair.printer.ToStringPrinter;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.boot.logging.LogLevel.*;

/**
 * @author Viacheslav Klapatniuk
 */
public class LogInSimpleLoggerTest {

    private Method method;
    private Method methodWithParameters;

    @Before
    public void init() throws NoSuchMethodException {
        method = LogInSimpleLoggerTest.class.getMethod("method");
        methodWithParameters = LogInSimpleLoggerTest.class.getMethod("methodWithParameters", String.class, Integer.class, Dto.class);
    }

    @SuppressWarnings("unused")
    public void method() {
    }

    @SuppressWarnings("unused")
    public void methodWithParameters(String s, Integer i, Dto dto) {
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
    public void notPrintParameterNames() {
        // given, when
        Printer printer = new ToStringPrinter();
        SimpleLogger logger = new SimpleLoggerBuilder()
                .method(methodWithParameters)
                .arguments("s", 1, new Dto())
                .levels(DEBUG, OFF, DEBUG)
                .argLogs(DEBUG, printer, DEBUG, printer, DEBUG, printer)
                .effectiveLevel(DEBUG)
                .printParameterName(false)
                .buildAndInvokeAndGet();
        // then
        verify(logger.getLoggerFacadeFactory().getLoggerFacade(any())).log(DEBUG, "> \"s\", 1, Dto{i=0, s='null'}");
    }

    @Test
    public void argLogsAreNull() {
        // given, when
        SimpleLogger logger = new SimpleLoggerBuilder()
                .method(methodWithParameters)
                .arguments("s", 1, new Dto())
                .levels(DEBUG, OFF, DEBUG)
                .argLogs(null, null, null)
                .effectiveLevel(DEBUG)
                .buildAndInvokeAndGet();
        // then
        verify(logger.getLoggerFacadeFactory().getLoggerFacade(any())).log(DEBUG, "> s=\"s\", i=1, dto=Dto{i=0, s='null'}");
    }

    @Test
    public void argLogsAreNullIfEnabledSmallerThanEffectiveLevel() {
        // given, when
        SimpleLogger logger = new SimpleLoggerBuilder()
                .method(methodWithParameters)
                .arguments("s", 1, new Dto())
                .levels(DEBUG, TRACE, DEBUG)
                .argLogs(null, null, null)
                .effectiveLevel(DEBUG)
                .buildAndInvokeAndGet();
        // then
        verify(logger.getLoggerFacadeFactory().getLoggerFacade(any()), never()).log(any(), any());
    }

    @Test
    public void firstArgLogIsNull() {
        // given, when
        Printer printer = new ToStringPrinter();
        SimpleLogger logger = new SimpleLoggerBuilder()
                .method(methodWithParameters)
                .arguments("s", 1, new Dto())
                .levels(DEBUG, OFF, DEBUG)
                .argLog(null)
                .argLog(DEBUG, printer)
                .argLog(DEBUG, printer)
                .effectiveLevel(DEBUG)
                .buildAndInvokeAndGet();
        // then
        verify(logger.getLoggerFacadeFactory().getLoggerFacade(any())).log(DEBUG, "> s=\"s\", i=1, dto=Dto{i=0, s='null'}");
    }

    @Test
    public void middleArgLogIsNull() {
        // given, when
        Printer printer = new ToStringPrinter();
        SimpleLogger logger = new SimpleLoggerBuilder()
                .method(methodWithParameters)
                .arguments("s", 1, new Dto())
                .levels(DEBUG, OFF, DEBUG)
                .argLog(DEBUG, printer)
                .argLog(null)
                .argLog(DEBUG, printer)
                .effectiveLevel(DEBUG)
                .buildAndInvokeAndGet();
        // then
        verify(logger.getLoggerFacadeFactory().getLoggerFacade(any())).log(DEBUG, "> s=\"s\", i=1, dto=Dto{i=0, s='null'}");
    }

    @Test
    public void lastArgLogIsNull() {
        // given, when
        Printer printer = new ToStringPrinter();
        SimpleLogger logger = new SimpleLoggerBuilder()
                .method(methodWithParameters)
                .arguments("s", 1, new Dto())
                .levels(DEBUG, OFF, DEBUG)
                .argLog(DEBUG, printer)
                .argLog(DEBUG, printer)
                .argLog(null)
                .effectiveLevel(DEBUG)
                .buildAndInvokeAndGet();
        // then
        verify(logger.getLoggerFacadeFactory().getLoggerFacade(any())).log(DEBUG, "> s=\"s\", i=1, dto=Dto{i=0, s='null'}");
    }

    @Test
    public void someArgLogIfEnabledLevelSmallerThanEffectiveLevel() {
        // given, when
        Printer printer = new ToStringPrinter();
        SimpleLogger logger = new SimpleLoggerBuilder()
                .method(methodWithParameters)
                .arguments("s", 1, new Dto())
                .levels(INFO, OFF, DEBUG)
                .argLogs(DEBUG, printer, INFO, printer, TRACE, printer)
                .effectiveLevel(INFO)
                .buildAndInvokeAndGet();
        // then
        verify(logger.getLoggerFacadeFactory().getLoggerFacade(any())).log(INFO, "> i=1");
    }

    @Test
    public void inLogIsNullFirstArgLogIsNullLastArgLogNotEnabled() {
        // given, when
        Printer printer = new ToStringPrinter();
        SimpleLogger logger = new SimpleLoggerBuilder()
                .method(methodWithParameters)
                .arguments("s", 1, new Dto())
                .argLog(null)
                .argLog(INFO, printer)
                .argLog(DEBUG, printer)
                .effectiveLevel(INFO)
                .buildAndInvokeAndGet(null);
        // then
        verify(logger.getLoggerFacadeFactory().getLoggerFacade(any())).log(INFO, "> i=1");
    }

    @Test
    public void inLogIsNullMaximumArgLogEnabledLevelFound() {
        // given, when
        Printer printer = new ToStringPrinter();
        SimpleLogger logger = new SimpleLoggerBuilder()
                .method(methodWithParameters)
                .arguments("s", 1, new Dto())
                .argLog(null)
                .argLog(DEBUG, printer)
                .argLog(INFO, printer)
                .effectiveLevel(DEBUG)
                .buildAndInvokeAndGet(null);
        // then
        verify(logger.getLoggerFacadeFactory().getLoggerFacade(any())).log(INFO, "> i=1, dto=Dto{i=0, s='null'}");
    }

    @Test
    public void argLogsAreNullInLogIfEnabledLevelDenied() {
        // given, when
        SimpleLogger logger = new SimpleLoggerBuilder()
                .method(methodWithParameters)
                .arguments("s", 1, new Dto())
                .levels(INFO, DEBUG, DEBUG)
                .argLogs(null, null, null)
                .effectiveLevel(INFO)
                .buildAndInvokeAndGet();
        // then
        verify(logger.getLoggerFacadeFactory().getLoggerFacade(any()), never()).log(any(), any());
    }

    @Test
    public void argLogsIfEnabledLevelsAreDenied() {
        // given, when
        Printer printer = new ToStringPrinter();
        SimpleLogger logger = new SimpleLoggerBuilder()
                .method(methodWithParameters)
                .levels(INFO, OFF, DEBUG/*?*/)
                .arguments("s", 1, new Dto())
                .argLog(DEBUG, printer)
                .argLog(DEBUG, printer)
                .argLog(DEBUG, printer)
                .argLogs(null, null, null)
                .effectiveLevel(INFO)
                .buildAndInvokeAndGet();
        // then
        verify(logger.getLoggerFacadeFactory().getLoggerFacade(any())).log(INFO, ">");
    }

    @Test
    public void argLogsAreNullInLogVerboseLevelDenied() {
        // given, when
        SimpleLogger logger = new SimpleLoggerBuilder()
                .method(methodWithParameters)
                .arguments("s", 1, new Dto())
                .levels(INFO, OFF, DEBUG)
                .argLogs(null, null, null)
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
                .arguments(null, null, null)
                .levels(DEBUG, OFF, DEBUG)
                .argLogs(null, null, null)
                .effectiveLevel(DEBUG)
                .buildAndInvokeAndGet();
        // then
        verify(logger.getLoggerFacadeFactory().getLoggerFacade(any())).log(DEBUG, "> s=null, i=null, dto=null");
    }

    @Test
    public void printers() {
        // given, when
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setClassesToBeBound(Dto.class);
        SimpleLogger logger = new SimpleLoggerBuilder()
                .method(methodWithParameters)
                .arguments("s", 1, new Dto())
                .levels(DEBUG, OFF, DEBUG)
                .argLog(DEBUG, new JacksonPrinter(new ObjectMapper()))
                .argLog(DEBUG, new ToStringPrinter())
                .argLog(DEBUG, new Jaxb2Printer(marshaller))
                .effectiveLevel(DEBUG)
                .buildAndInvokeAndGet();
        // then
        verify(logger.getLoggerFacadeFactory().getLoggerFacade(any())).log(DEBUG, "> s=\"s\", i=1, dto=<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><dto><i>0</i></dto>");
    }

    private class SimpleLoggerBuilder {

        private Method method;
        private List<Object> arguments = emptyList();
        private LogLevel level = DEBUG;
        private LogLevel ifEnabledLevel = OFF;
        private LogLevel verboseLevel = DEBUG;
        private Printer printer = new ToStringPrinter();
        private List<ArgLog> argLogs = new ArrayList<>();
        private LogLevel effectiveLevel;
        private boolean printParameterName = true;

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

        private SimpleLoggerBuilder printer(Printer printer) {
            this.printer = printer;
            return this;
        }

        private SimpleLoggerBuilder argLog(LogLevel ifEnabledLevel, Printer printer) {
            this.argLogs.add(ArgLog.builder()
                    .ifEnabledLevel(ifEnabledLevel)
                    .printer(printer)
                    .build());
            return this;
        }

        private SimpleLoggerBuilder argLog(ArgLog argLog) {
            this.argLogs.add(argLog);
            return this;
        }

        private SimpleLoggerBuilder argLogs(LogLevel ifEnabledLevel0, Printer printer0,
                                            LogLevel ifEnabledLevel1, Printer printer1,
                                            LogLevel ifEnabledLevel2, Printer printer2) {
            return argLog(ifEnabledLevel0, printer0)
                    .argLog(ifEnabledLevel1, printer1)
                    .argLog(ifEnabledLevel2, printer2);
        }

        private SimpleLoggerBuilder argLogs(ArgLog argLog0, ArgLog argLog1, ArgLog argLog2) {
            return argLog(argLog0)
                    .argLog(argLog1)
                    .argLog(argLog2);
        }

        private SimpleLoggerBuilder effectiveLevel(LogLevel effectiveLevel) {
            this.effectiveLevel = effectiveLevel;
            return this;
        }

        private SimpleLoggerBuilder printParameterName(boolean printParameterName) {
            this.printParameterName = printParameterName;
            return this;
        }

        private SimpleLogger buildAndInvokeAndGet() {
            InLog inLog = InLog.builder()
                    .level(level)
                    .ifEnabledLevel(ifEnabledLevel)
                    .verboseLevel(verboseLevel)
                    .printer(printer)
                    .build();
            return buildAndInvokeAndGet(inLog);
        }

        private SimpleLogger buildAndInvokeAndGet(InLog inLog) {
            // given
            MethodInvocation invocation = methodInvocation(method, arguments.toArray());
            SimpleLogger simpleLogger = new SimpleLogger(loggerFacadeFactory(), loggingSystem(effectiveLevel));
            simpleLogger.setPrintParameterName(printParameterName);
            // when
            simpleLogger.logInIfNecessary(invocation, logPack(inLog, argLogs));
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

        private LogPack logPack(InLog inLog, List<ArgLog> argLogs) {
            LogPack logPack = mock(LogPack.class);
            when(logPack.getInLog()).thenReturn(inLog);
            when(logPack.getArgLogs()).thenReturn(argLogs);
            return logPack;
        }
    }
}
