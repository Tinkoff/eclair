package ru.tinkoff.eclair.logger;

import org.aopalliance.intercept.MethodInvocation;
import org.springframework.core.Ordered;
import ru.tinkoff.eclair.definition.LogPack;

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

    public void logInIfNecessary(MethodInvocation invocation, LogPack logPack) {
        if (isLogInNecessary(invocation, logPack)) {
            logIn(invocation, logPack);
        }
    }

    protected boolean isLogInNecessary(MethodInvocation invocation, LogPack logPack) {
        return nonNull(logPack.getInLog());
    }

    protected abstract void logIn(MethodInvocation invocation, LogPack logPack);

    public void logOutIfNecessary(MethodInvocation invocation, LogPack logPack, Object result) {
        if (isLogOutNecessary(invocation, logPack)) {
            logOut(invocation, logPack, result);
        }
    }

    protected boolean isLogOutNecessary(MethodInvocation invocation, LogPack logPack) {
        return nonNull(logPack.getOutLog());
    }

    protected abstract void logOut(MethodInvocation invocation, LogPack logPack, Object result);

    public void logErrorIfNecessary(MethodInvocation invocation, LogPack logPack, Throwable throwable) {
        if (isLogErrorNecessary(invocation, logPack, throwable)) {
            logError(invocation, logPack, throwable);
        } else if (isLogOutNecessary(invocation, logPack)) {
            logEmergencyOut(invocation, logPack, throwable);
        }
    }

    protected boolean isLogErrorNecessary(MethodInvocation invocation, LogPack logPack, Throwable throwable) {
        return nonNull(logPack.findErrorLog(throwable.getClass()));
    }

    protected abstract void logError(MethodInvocation invocation, LogPack logPack, Throwable throwable);

    protected abstract void logEmergencyOut(MethodInvocation invocation, LogPack logPack, Throwable throwable);
}
