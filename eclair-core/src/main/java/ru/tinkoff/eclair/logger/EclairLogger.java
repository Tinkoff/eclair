package ru.tinkoff.eclair.logger;

import org.aopalliance.intercept.MethodInvocation;
import org.springframework.boot.logging.LogLevel;
import org.springframework.core.Ordered;
import ru.tinkoff.eclair.core.ExpectedLevelResolver;
import ru.tinkoff.eclair.definition.*;

import java.util.function.Function;

import static java.util.Objects.nonNull;

/**
 * @author Viacheslav Klapatniuk
 */
public abstract class EclairLogger implements Ordered {

    private static final Function<EventLogDefinition, LogLevel> expectedLevelResolver = ExpectedLevelResolver.getInstance();

    @Override
    public int getOrder() {
        return 0;
    }

    protected abstract String getLoggerName(MethodInvocation invocation);

    protected abstract boolean isLevelEnabled(String loggerName, LogLevel expectedLevel);

    public void logInIfNecessary(MethodInvocation invocation, LogDefinition definition) {
        InLogDefinition inLogDefinition = definition.getInLogDefinition();
        if (nonNull(inLogDefinition)) {
            String loggerName = getLoggerName(invocation);
            if (isLevelEnabled(loggerName, expectedLevelResolver.apply(inLogDefinition))) {
                logIn(invocation, inLogDefinition, loggerName);
            }
        }
    }

    protected abstract void logIn(MethodInvocation invocation, InLogDefinition definition, String loggerName);

    public void logOutIfNecessary(MethodInvocation invocation, LogDefinition definition, Object result) {
        OutLogDefinition outLogDefinition = definition.getOutLogDefinition();
        if (nonNull(outLogDefinition)) {
            String loggerName = getLoggerName(invocation);
            if (isLevelEnabled(loggerName, expectedLevelResolver.apply(outLogDefinition))) {
                logOut(invocation, outLogDefinition, result, loggerName);
            }
        }
    }

    protected abstract void logOut(MethodInvocation invocation, OutLogDefinition definition, Object result, String loggerName);

    public void logErrorIfNecessary(MethodInvocation invocation, LogDefinition definition, Throwable throwable) {
        ErrorLogDefinition errorLogDefinition = definition.findErrorLogDefinition(throwable.getClass());
        if (nonNull(errorLogDefinition)) {
            String loggerName = getLoggerName(invocation);
            if (isLevelEnabled(loggerName, expectedLevelResolver.apply(errorLogDefinition))) {
                logError(invocation, errorLogDefinition, throwable, loggerName);
            }
        } else {
            OutLogDefinition outLogDefinition = definition.getOutLogDefinition();
            if (nonNull(outLogDefinition)) {
                String loggerName = getLoggerName(invocation);
                if (isLevelEnabled(loggerName, expectedLevelResolver.apply(outLogDefinition))) {
                    logEmergencyOut(invocation, outLogDefinition, throwable, loggerName);
                }
            }
        }
    }

    protected abstract void logError(MethodInvocation invocation, ErrorLogDefinition definition, Throwable throwable, String loggerName);

    protected abstract void logEmergencyOut(MethodInvocation invocation, OutLogDefinition definition, Throwable throwable, String loggerName);
}
