package ru.tinkoff.eclair.logger;

import org.aopalliance.intercept.MethodInvocation;
import org.springframework.boot.logging.LogLevel;
import ru.tinkoff.eclair.definition.ErrorLogDefinition;
import ru.tinkoff.eclair.definition.InLogDefinition;
import ru.tinkoff.eclair.definition.OutLogDefinition;

/**
 * @author Viacheslav Klapatniuk
 */
public abstract class LevelInsensitiveLogger extends EclairLogger {

    @Override
    public boolean isLevelEnabled(String loggerName, LogLevel expectedLevel) {
        return true;
    }

    @Override
    public void logInIfLevelEnabled(MethodInvocation invocation, InLogDefinition definition) {
        logIn(invocation, definition, getLoggerName(invocation));
    }

    @Override
    public void logOutIfLevelEnabled(MethodInvocation invocation, Object result, OutLogDefinition definition, boolean emergency) {
        logOut(invocation, result, definition, emergency, getLoggerName(invocation));
    }

    @Override
    public void logErrorIfLevelEnabled(MethodInvocation invocation, Throwable throwable, ErrorLogDefinition definition) {
        logError(invocation, throwable, definition, getLoggerName(invocation));
    }
}
