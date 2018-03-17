package ru.tinkoff.eclair.logger;

import org.aopalliance.intercept.MethodInvocation;
import org.springframework.boot.logging.LogLevel;
import ru.tinkoff.eclair.core.ExpectedLevelResolver;
import ru.tinkoff.eclair.definition.ErrorLog;
import ru.tinkoff.eclair.definition.LogDefinition;
import ru.tinkoff.eclair.definition.LogPack;

import java.util.function.Function;

import static java.util.Objects.nonNull;

/**
 * @author Viacheslav Klapatniuk
 */
public abstract class LevelSensitiveLogger extends EclairLogger {

    private static final Function<LogDefinition, LogLevel> expectedLevelResolver = ExpectedLevelResolver.getInstance();

    protected abstract boolean isLevelEnabled(String loggerName, LogLevel expectedLevel);

    protected boolean isLogInNecessary(MethodInvocation invocation, LogPack logPack) {
        return super.isLogInNecessary(invocation, logPack) &&
                isLevelEnabled(getLoggerName(invocation), expectedLevelResolver.apply(logPack.getInLog()));
    }

    @Override
    protected boolean isLogOutNecessary(MethodInvocation invocation, LogPack logPack) {
        return super.isLogOutNecessary(invocation, logPack) &&
                isLevelEnabled(getLoggerName(invocation), expectedLevelResolver.apply(logPack.getOutLog()));
    }

    @Override
    protected boolean isLogErrorNecessary(MethodInvocation invocation, LogPack logPack, Throwable throwable) {
        ErrorLog errorLog = logPack.findErrorLog(throwable.getClass());
        return nonNull(errorLog) && isLevelEnabled(getLoggerName(invocation), expectedLevelResolver.apply(errorLog));
    }
}
