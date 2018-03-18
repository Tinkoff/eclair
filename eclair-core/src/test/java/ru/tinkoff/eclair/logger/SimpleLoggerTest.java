package ru.tinkoff.eclair.logger;

import org.aopalliance.intercept.MethodInvocation;
import org.junit.Test;
import org.springframework.boot.logging.LogLevel;
import org.springframework.boot.logging.LoggerConfiguration;
import org.springframework.boot.logging.LoggingSystem;
import ru.tinkoff.eclair.definition.ArgLog;
import ru.tinkoff.eclair.definition.LogPack;
import ru.tinkoff.eclair.logger.facade.LoggerFacadeFactory;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.boot.logging.LogLevel.DEBUG;

/**
 * @author Viacheslav Klapatniuk
 */
public class SimpleLoggerTest {

    @Test
    public void logIn() throws NoSuchMethodException {
        // given
        MethodInvocation invocation = givenMethodInvocation(SimpleLoggerTest.class.getMethod("logInTest", String.class));
        List<ArgLog> argLogs = new ArrayList<>();
        argLogs.add(null);
        LogPack logPack = givenLogPack(DEBUG, DEBUG, argLogs);
        LoggerFacadeFactory loggerFacadeFactory = givenLoggerFacadeFactory();
        SimpleLogger simpleLogger = new SimpleLogger(loggerFacadeFactory, givenLoggingSystem(DEBUG));
        // when
        simpleLogger.logIn(invocation, logPack);
        // then
        verify(loggerFacadeFactory.getLoggerFacade("ru.tinkoff.eclair.logger.SimpleLoggerTest.logInTest")).log(DEBUG, ">");
    }

    @SuppressWarnings("unused")
    public void logInTest(String string) {
    }

    private MethodInvocation givenMethodInvocation(Method method) throws NoSuchMethodException {
        MethodInvocation invocation = mock(MethodInvocation.class);
        when(invocation.getMethod()).thenReturn(method);
        when(invocation.getArguments()).thenReturn(new Object[0]);
        return invocation;
    }

    private LogPack givenLogPack(LogLevel level, LogLevel verboseLevel, List<ArgLog> argLogs) {
        LogPack logPack = mock(LogPack.class, RETURNS_DEEP_STUBS);
        when(logPack.getInLog().getLevel()).thenReturn(level);
        when(logPack.getInLog().getVerboseLevel()).thenReturn(verboseLevel);
        when(logPack.getInLog().getArgLogs()).thenReturn(argLogs);
        return logPack;
    }

    private LoggerFacadeFactory givenLoggerFacadeFactory() {
        return mock(LoggerFacadeFactory.class, RETURNS_DEEP_STUBS);
    }

    private LoggingSystem givenLoggingSystem(LogLevel level) {
        LoggingSystem loggingSystem = mock(LoggingSystem.class);
        when(loggingSystem.getLoggerConfiguration(any())).thenReturn(new LoggerConfiguration("", null, level));
        return loggingSystem;
    }
}
