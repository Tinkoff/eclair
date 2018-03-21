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

    protected abstract boolean isLevelEnabled(String loggerName, LogLevel expectedLevel);

    /**
     * Could be overridden for lazy optimal check
     */
    @Override
    protected boolean isLogInNecessary(MethodInvocation invocation, LogPack logPack) {
        String loggerName = getLoggerName(invocation);
        if (nonNull(logPack.getInLog())) {
            if (isLevelEnabled(loggerName, expectedLevelResolver.apply(logPack.getInLog()))) {
                return true;
            }
        }
        return logPack.getArgLogs().stream()
                .anyMatch(argLog -> nonNull(argLog) && isLevelEnabled(loggerName, argLog.getIfEnabledLevel()));
    }

    /**
     * Could be overridden for lazy optimal check
     */
    @Override
    protected boolean isLogOutNecessary(MethodInvocation invocation, LogPack logPack) {
        return super.isLogOutNecessary(invocation, logPack) &&
                isLevelEnabled(getLoggerName(invocation), expectedLevelResolver.apply(logPack.getOutLog()));
    }

    /**
     * Could be overridden for lazy optimal check
     */
    @Override
    protected boolean isLogErrorNecessary(MethodInvocation invocation, LogPack logPack, Throwable throwable) {
        ErrorLog errorLog = logPack.findErrorLog(throwable.getClass());
        return nonNull(errorLog) && isLevelEnabled(getLoggerName(invocation), expectedLevelResolver.apply(errorLog));
    }
}
