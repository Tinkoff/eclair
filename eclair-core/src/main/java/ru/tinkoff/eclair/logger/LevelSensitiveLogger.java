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
 * TODO: add tests
 *
 * @author Viacheslav Klapatniuk
 */
public abstract class LevelSensitiveLogger extends EclairLogger {

    static final Function<LogDefinition, LogLevel> expectedLevelResolver = ExpectedLevelResolver.getInstance();

    protected abstract boolean isLogEnabled(String loggerName, LogLevel level);

    /**
     * Could be overridden for lazy optimal check
     */
    @Override
    protected boolean isLogInNecessary(MethodInvocation invocation, LogPack logPack) {
        String loggerName = getLoggerName(invocation);
        if (nonNull(logPack.getInLog())) {
            if (isLogEnabled(loggerName, expectedLevelResolver.apply(logPack.getInLog()))) {
                return true;
            }
        }
        return logPack.getParameterLogs().stream()
                .anyMatch(parameterLog -> nonNull(parameterLog) && isLogEnabled(loggerName, expectedLevelResolver.apply(parameterLog)));
    }

    /**
     * Could be overridden for lazy optimal check
     */
    @Override
    protected boolean isLogOutNecessary(MethodInvocation invocation, LogPack logPack) {
        return super.isLogOutNecessary(invocation, logPack) &&
                isLogEnabled(getLoggerName(invocation), expectedLevelResolver.apply(logPack.getOutLog()));
    }

    /**
     * Could be overridden for lazy optimal check
     */
    @Override
    protected boolean isLogErrorNecessary(MethodInvocation invocation, LogPack logPack, Throwable throwable) {
        ErrorLog errorLog = logPack.findErrorLog(throwable.getClass());
        return nonNull(errorLog) && isLogEnabled(getLoggerName(invocation), expectedLevelResolver.apply(errorLog));
    }
}
