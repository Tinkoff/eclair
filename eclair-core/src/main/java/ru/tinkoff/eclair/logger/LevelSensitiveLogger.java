package ru.tinkoff.eclair.logger;

import org.aopalliance.intercept.MethodInvocation;
import org.springframework.boot.logging.LogLevel;
import ru.tinkoff.eclair.core.ExpectedLevelResolver;
import ru.tinkoff.eclair.definition.ErrorLogDefinition;
import ru.tinkoff.eclair.definition.EventLogDefinition;
import ru.tinkoff.eclair.definition.LogDefinition;

import java.util.function.Function;

import static java.util.Objects.nonNull;

/**
 * @author Viacheslav Klapatniuk
 */
public abstract class LevelSensitiveLogger extends EclairLogger {

    private static final Function<EventLogDefinition, LogLevel> expectedLevelResolver = ExpectedLevelResolver.getInstance();

    protected abstract boolean isLevelEnabled(String loggerName, LogLevel expectedLevel);

    protected boolean isLogInNecessary(MethodInvocation invocation, LogDefinition definition) {
        return super.isLogInNecessary(invocation, definition) &&
                isLevelEnabled(getLoggerName(invocation), expectedLevelResolver.apply(definition.getInLogDefinition()));
    }

    @Override
    protected boolean isLogOutNecessary(MethodInvocation invocation, LogDefinition definition) {
        return super.isLogOutNecessary(invocation, definition) &&
                isLevelEnabled(getLoggerName(invocation), expectedLevelResolver.apply(definition.getOutLogDefinition()));
    }

    @Override
    protected boolean isLogErrorNecessary(MethodInvocation invocation, LogDefinition definition, Throwable throwable) {
        ErrorLogDefinition errorLogDefinition = definition.findErrorLogDefinition(throwable.getClass());
        return nonNull(errorLogDefinition) && isLevelEnabled(getLoggerName(invocation), expectedLevelResolver.apply(errorLogDefinition));
    }
}
