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
public abstract class Logger implements Ordered {

    @Override
    public int getOrder() {
        return 0;
    }

    public boolean isLogInEnabled(MethodInvocation invocation, InLogDefinition inLogDefinition) {
        LogLevel expectedLevel = getMinLevel(inLogDefinition.getLevel(), inLogDefinition.getIfEnabledLevel());
        return isLevelEnabled(invocation, expectedLevel);
    }

    public boolean isLogOutEnabled(MethodInvocation invocation, OutLogDefinition outLogDefinition) {
        LogLevel expectedLevel = getMinLevel(outLogDefinition.getLevel(), outLogDefinition.getIfEnabledLevel());
        return isLevelEnabled(invocation, expectedLevel);
    }

    public boolean isLogErrorEnabled(MethodInvocation invocation, ErrorLogDefinition errorLogDefinition) {
        LogLevel expectedLevel = getMinLevel(errorLogDefinition.getLevel(), errorLogDefinition.getIfEnabledLevel());
        return isLevelEnabled(invocation, expectedLevel);
    }

    private LogLevel getMinLevel(LogLevel first, LogLevel second) {
        return first.ordinal() <= second.ordinal() ? first : second;
    }

    protected abstract boolean isLevelEnabled(MethodInvocation methodInvocation, LogLevel expectedLevel);

    public abstract void logIn(MethodInvocation invocation, InLogDefinition inLogDefinition);

    public abstract void logOut(MethodInvocation invocation, Object result, OutLogDefinition outLogDefinition, boolean emergency);

    public abstract void logError(MethodInvocation invocation, Throwable throwable, ErrorLogDefinition errorLogDefinition);
}
