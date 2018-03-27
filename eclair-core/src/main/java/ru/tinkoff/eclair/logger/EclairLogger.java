package ru.tinkoff.eclair.logger;

import org.aopalliance.intercept.MethodInvocation;
import org.springframework.core.Ordered;
import ru.tinkoff.eclair.definition.MethodLog;

import java.util.Objects;

import static java.util.Objects.nonNull;

/**
 * @author Viacheslav Klapatniuk
 */
public abstract class EclairLogger implements Ordered {

    @Override
    public int getOrder() {
        return 0;
    }

    protected abstract String getLoggerName(MethodInvocation invocation);

    public void logInIfNecessary(MethodInvocation invocation, MethodLog methodLog) {
        if (isLogInNecessary(invocation, methodLog)) {
            logIn(invocation, methodLog);
        }
    }

    /**
     * Could be overridden for lazy optimal check
     */
    protected boolean isLogInNecessary(MethodInvocation invocation, MethodLog methodLog) {
        return nonNull(methodLog.getInLog()) || methodLog.getParameterLogs().stream().anyMatch(Objects::nonNull);
    }

    protected abstract void logIn(MethodInvocation invocation, MethodLog methodLog);

    public void logOutIfNecessary(MethodInvocation invocation, MethodLog methodLog, Object result) {
        if (isLogOutNecessary(invocation, methodLog)) {
            logOut(invocation, methodLog, result);
        }
    }

    /**
     * Could be overridden for lazy optimal check
     */
    protected boolean isLogOutNecessary(MethodInvocation invocation, MethodLog methodLog) {
        return nonNull(methodLog.getOutLog());
    }

    protected abstract void logOut(MethodInvocation invocation, MethodLog methodLog, Object result);

    public void logErrorIfNecessary(MethodInvocation invocation, MethodLog methodLog, Throwable throwable) {
        if (isLogErrorNecessary(invocation, methodLog, throwable) || isLogOutNecessary(invocation, methodLog)) {
            logError(invocation, methodLog, throwable);
        }
    }

    /**
     * Could be overridden for lazy optimal check
     */
    protected boolean isLogErrorNecessary(MethodInvocation invocation, MethodLog methodLog, Throwable throwable) {
        return nonNull(methodLog.findErrorLog(throwable.getClass()));
    }

    protected abstract void logError(MethodInvocation invocation, MethodLog methodLog, Throwable throwable);
}
