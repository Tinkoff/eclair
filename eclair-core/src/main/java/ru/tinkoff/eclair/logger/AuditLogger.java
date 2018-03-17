package ru.tinkoff.eclair.logger;

import org.aopalliance.intercept.MethodInvocation;
import ru.tinkoff.eclair.core.LoggerNameBuilder;
import ru.tinkoff.eclair.definition.LogDefinition;

import static java.util.Arrays.asList;

/**
 * TODO: implement
 *
 * @author Viacheslav Klapatniuk
 */
public class AuditLogger extends EclairLogger {

    private final LoggerNameBuilder loggerNameBuilder = LoggerNameBuilder.getInstance();

    @Override
    protected String getLoggerName(MethodInvocation invocation) {
        return loggerNameBuilder.build(invocation);
    }

    @Override
    protected void logIn(MethodInvocation invocation, LogDefinition definition) {
        System.out.println(getLoggerName(invocation) + " " + asList(invocation.getArguments()));
    }

    @Override
    protected void logOut(MethodInvocation invocation, LogDefinition definition, Object result) {
        System.out.println(getLoggerName(invocation) + " " + result);
    }

    @Override
    protected void logError(MethodInvocation invocation, LogDefinition definition, Throwable throwable) {
        System.out.println(getLoggerName(invocation) + " " + throwable);
    }

    @Override
    protected void logEmergencyOut(MethodInvocation invocation, LogDefinition definition, Throwable throwable) {
        logError(invocation, definition, throwable);
    }
}
