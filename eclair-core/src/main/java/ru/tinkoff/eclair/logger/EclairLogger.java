package ru.tinkoff.eclair.logger;

import org.aopalliance.intercept.MethodInvocation;
import org.springframework.boot.logging.LogLevel;
import org.springframework.core.Ordered;
import ru.tinkoff.eclair.definition.ErrorLogDefinition;
import ru.tinkoff.eclair.definition.InLogDefinition;
import ru.tinkoff.eclair.definition.OutLogDefinition;

/**
 * @author Viacheslav Klapatniuk
 */
public abstract class EclairLogger implements Ordered {

    @Override
    public int getOrder() {
        return 0;
    }

    public abstract String getLoggerName(MethodInvocation invocation);

    public abstract boolean isLevelEnabled(String loggerName, LogLevel expectedLevel);

    public abstract void logIn(MethodInvocation invocation, String loggerName, InLogDefinition inLogDefinition);

    public abstract void logOut(MethodInvocation invocation, String loggerName, Object result, OutLogDefinition outLogDefinition, boolean emergency);

    public abstract void logError(MethodInvocation invocation, String loggerName, Throwable throwable, ErrorLogDefinition errorLogDefinition);
}
