package ru.tinkoff.eclair.logger;

import org.aopalliance.intercept.MethodInvocation;
import org.springframework.core.Ordered;
import ru.tinkoff.eclair.definition.LogDefinition;

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

    public void logInIfNecessary(MethodInvocation invocation, LogDefinition definition) {
        if (isLogInNecessary(invocation, definition)) {
            logIn(invocation, definition);
        }
    }

    protected boolean isLogInNecessary(MethodInvocation invocation, LogDefinition definition) {
        return nonNull(definition.getInLogDefinition());
    }

    protected abstract void logIn(MethodInvocation invocation, LogDefinition definition);

    public void logOutIfNecessary(MethodInvocation invocation, LogDefinition definition, Object result) {
        if (isLogOutNecessary(invocation, definition)) {
            logOut(invocation, definition, result);
        }
    }

    protected boolean isLogOutNecessary(MethodInvocation invocation, LogDefinition definition) {
        return nonNull(definition.getOutLogDefinition());
    }

    protected abstract void logOut(MethodInvocation invocation, LogDefinition definition, Object result);

    public void logErrorIfNecessary(MethodInvocation invocation, LogDefinition definition, Throwable throwable) {
        if (isLogErrorNecessary(invocation, definition, throwable)) {
            logError(invocation, definition, throwable);
        } else if (isLogOutNecessary(invocation, definition)) {
            logEmergencyOut(invocation, definition, throwable);
        }
    }

    protected boolean isLogErrorNecessary(MethodInvocation invocation, LogDefinition definition, Throwable throwable) {
        return nonNull(definition.findErrorLogDefinition(throwable.getClass()));
    }

    protected abstract void logError(MethodInvocation invocation, LogDefinition definition, Throwable throwable);

    protected abstract void logEmergencyOut(MethodInvocation invocation, LogDefinition definition, Throwable throwable);
}
