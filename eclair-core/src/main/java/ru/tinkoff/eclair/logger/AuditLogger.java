package ru.tinkoff.eclair.logger;

import org.aopalliance.intercept.MethodInvocation;
import ru.tinkoff.eclair.core.LoggerNameBuilder;
import ru.tinkoff.eclair.definition.LogPack;

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
    protected void logIn(MethodInvocation invocation, LogPack logPack) {
        System.out.println(getLoggerName(invocation) + " " + asList(invocation.getArguments()));
    }

    @Override
    protected void logOut(MethodInvocation invocation, LogPack logPack, Object result) {
        System.out.println(getLoggerName(invocation) + " " + result);
    }

    @Override
    protected void logError(MethodInvocation invocation, LogPack logPack, Throwable throwable) {
        System.out.println(getLoggerName(invocation) + " " + throwable);
    }
}
