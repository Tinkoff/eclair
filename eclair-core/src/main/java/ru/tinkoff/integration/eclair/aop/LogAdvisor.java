package ru.tinkoff.integration.eclair.aop;

import org.aopalliance.aop.Advice;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.support.StaticMethodMatcherPointcutAdvisor;
import ru.tinkoff.integration.eclair.definition.ErrorLogDefinition;
import ru.tinkoff.integration.eclair.definition.InLogDefinition;
import ru.tinkoff.integration.eclair.definition.LogDefinition;
import ru.tinkoff.integration.eclair.definition.OutLogDefinition;
import ru.tinkoff.integration.eclair.logger.Logger;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

final class LogAdvisor extends StaticMethodMatcherPointcutAdvisor implements MethodInterceptor {

    private final Logger logger;
    private final Map<Method, LogDefinition> logDefinitions;

    private LogAdvisor(Logger logger, List<LogDefinition> logDefinitions) {
        this.logger = logger;
        this.logDefinitions = logDefinitions.stream().collect(toMap(LogDefinition::getMethod, identity()));
    }

    static LogAdvisor newInstance(Logger logger, List<LogDefinition> logDefinitions) {
        return logDefinitions.isEmpty() ? null : new LogAdvisor(logger, logDefinitions);
    }

    @Override
    public Advice getAdvice() {
        return this;
    }

    @Override
    public boolean matches(Method method, Class<?> targetClass) {
        return logDefinitions.containsKey(method);
    }

    /**
     * Despite the fact that this method is not being used.
     */
    @Override
    public boolean isPerInstance() {
        return false;
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        LogDefinition logDefinition = logDefinitions.get(invocation.getMethod());
        if (isNull(logDefinition)) {
            return invocation.proceed();
        }
        logInIfNecessary(invocation, logDefinition.getInLogDefinition());
        Object result;
        try {
            result = invocation.proceed();
        } catch (Throwable throwable) {
            logErrorIfNecessary(invocation, logDefinition.findErrorLogDefinition(throwable.getClass()), logDefinition.getOutLogDefinition(), throwable);
            throw throwable;
        }
        logOutIfNecessary(invocation, logDefinition.getOutLogDefinition(), result, false);
        return result;
    }

    private void logInIfNecessary(MethodInvocation invocation, InLogDefinition inLogDefinition) {
        if (nonNull(inLogDefinition) && logger.isLogInEnabled(invocation, inLogDefinition)) {
            logger.logIn(invocation, inLogDefinition);
        }
    }

    private void logOutIfNecessary(MethodInvocation invocation, OutLogDefinition outLogDefinition, Object result, /* TODO: refactor */ boolean emergency) {
        if (nonNull(outLogDefinition) && logger.isLogOutEnabled(invocation, outLogDefinition)) {
            logger.logOut(invocation, result, outLogDefinition, emergency);
        }
    }

    private void logErrorIfNecessary(MethodInvocation invocation, ErrorLogDefinition errorLogDefinition, OutLogDefinition outLogDefinition, Throwable throwable) {
        if (isNull(errorLogDefinition)) {
            logOutIfNecessary(invocation, outLogDefinition, null, true);
        } else if (logger.isLogErrorEnabled(invocation, errorLogDefinition)) {
            logger.logError(invocation, throwable, errorLogDefinition);
        }
    }
}
