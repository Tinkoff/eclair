package ru.tinkoff.eclair.logger;

import org.aopalliance.intercept.MethodInvocation;
import org.springframework.boot.logging.LogLevel;
import org.springframework.core.Ordered;
import ru.tinkoff.eclair.definition.ErrorLogDefinition;
import ru.tinkoff.eclair.definition.InLogDefinition;
import ru.tinkoff.eclair.definition.OutLogDefinition;

import java.util.function.BinaryOperator;

/**
 * @author Viacheslav Klapatniuk
 */
public abstract class EclairLogger implements Ordered {

    private static final BinaryOperator<LogLevel> expectedLevelOperator = (o, o2) -> o.ordinal() <= o2.ordinal() ? o : o2;

    @Override
    public int getOrder() {
        return 0;
    }

    protected abstract String getLoggerName(MethodInvocation invocation);

    protected abstract boolean isLevelEnabled(String loggerName, LogLevel expectedLevel);

    public void logInIfLevelEnabled(MethodInvocation invocation, InLogDefinition definition) {
        String loggerName = getLoggerName(invocation);
        LogLevel expectedLevel = expectedLevelOperator.apply(definition.getLevel(), definition.getIfEnabledLevel());
        if (isLevelEnabled(loggerName, expectedLevel)) {
            logIn(invocation, definition, loggerName);
        }
    }

    protected abstract void logIn(MethodInvocation invocation, InLogDefinition definition, String loggerName);

    public void logOutIfLevelEnabled(MethodInvocation invocation, Object result, OutLogDefinition definition, boolean emergency) {
        String loggerName = getLoggerName(invocation);
        LogLevel expectedLevel = expectedLevelOperator.apply(definition.getLevel(), definition.getIfEnabledLevel());
        if (isLevelEnabled(loggerName, expectedLevel)) {
            logOut(invocation, result, definition, emergency, loggerName);
        }
    }

    protected abstract void logOut(MethodInvocation invocation, Object result, OutLogDefinition definition, boolean emergency, String loggerName);

    public void logErrorIfLevelEnabled(MethodInvocation invocation, Throwable throwable, ErrorLogDefinition definition) {
        String loggerName = getLoggerName(invocation);
        LogLevel expectedLevel = expectedLevelOperator.apply(definition.getLevel(), definition.getIfEnabledLevel());
        if (isLevelEnabled(loggerName, expectedLevel)) {
            logError(invocation, throwable, definition, loggerName);
        }
    }

    protected abstract void logError(MethodInvocation invocation, Throwable throwable, ErrorLogDefinition definition, String loggerName);
}
