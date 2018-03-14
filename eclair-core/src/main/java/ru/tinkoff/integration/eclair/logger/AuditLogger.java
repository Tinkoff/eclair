package ru.tinkoff.integration.eclair.logger;

import org.aopalliance.intercept.MethodInvocation;
import org.springframework.boot.logging.LogLevel;
import ru.tinkoff.integration.eclair.definition.ErrorLogDefinition;
import ru.tinkoff.integration.eclair.definition.InLogDefinition;
import ru.tinkoff.integration.eclair.definition.OutLogDefinition;

/**
 * TODO: implement
 * TODO: extract to special artifact
 */
public class AuditLogger extends Logger {

    @Override
    public boolean isLogInEnabled(MethodInvocation invocation, InLogDefinition inLogDefinition) {
        return true;
    }

    @Override
    public boolean isLogOutEnabled(MethodInvocation invocation, OutLogDefinition outLogDefinition) {
        return true;
    }

    @Override
    public boolean isLogErrorEnabled(MethodInvocation invocation, ErrorLogDefinition errorLogDefinition) {
        return true;
    }

    @Override
    protected boolean isLevelEnabled(MethodInvocation invocation, LogLevel expectedLevel) {
        return true;
    }

    @Override
    public void logIn(MethodInvocation invocation, InLogDefinition inLogDefinition) {
        System.out.println("AuditLogger in");
    }

    @Override
    public void logOut(MethodInvocation invocation, Object result, OutLogDefinition outLogDefinition, boolean emergency) {
        System.out.println("AuditLogger out");
    }

    @Override
    public void logError(MethodInvocation invocation, Throwable throwable, ErrorLogDefinition errorLogDefinition) {
        System.out.println("AuditLogger error");
    }
}
