package ru.tinkoff.eclair.logger;

import org.aopalliance.intercept.MethodInvocation;
import org.springframework.boot.logging.LogLevel;
import ru.tinkoff.eclair.definition.ErrorLogDefinition;
import ru.tinkoff.eclair.definition.InLogDefinition;
import ru.tinkoff.eclair.definition.LogDefinition;
import ru.tinkoff.eclair.definition.OutLogDefinition;

import static java.util.Objects.nonNull;

/**
 * @author Viacheslav Klapatniuk
 */
public abstract class LevelInsensitiveLogger extends EclairLogger {

    @Override
    public boolean isLevelEnabled(String loggerName, LogLevel expectedLevel) {
        throw new UnsupportedOperationException();
    }

    /**
     * TODO: reduce 'nonNull' duplicates (refactor inheritance tree)
     */
    @Override
    public void logInIfNecessary(MethodInvocation invocation, LogDefinition definition) {
        InLogDefinition inLogDefinition = definition.getInLogDefinition();
        if (nonNull(inLogDefinition)) {
            logIn(invocation, inLogDefinition, getLoggerName(invocation));
        }
    }

    @Override
    public void logOutIfNecessary(MethodInvocation invocation, LogDefinition definition, Object result) {
        OutLogDefinition outLogDefinition = definition.getOutLogDefinition();
        if (nonNull(outLogDefinition)) {
            logOut(invocation, outLogDefinition, result, getLoggerName(invocation));
        }
    }

    @Override
    public void logErrorIfNecessary(MethodInvocation invocation, LogDefinition definition, Throwable throwable) {
        ErrorLogDefinition errorLogDefinition = definition.findErrorLogDefinition(throwable.getClass());
        if (nonNull(errorLogDefinition)) {
            logError(invocation, errorLogDefinition, throwable, getLoggerName(invocation));
        } else {
            OutLogDefinition outLogDefinition = definition.getOutLogDefinition();
            if (nonNull(outLogDefinition)) {
                logEmergencyOut(invocation, outLogDefinition, throwable, getLoggerName(invocation));
            }
        }
    }
}
