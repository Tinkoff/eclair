package ru.tinkoff.eclair.aop;

import org.aopalliance.aop.Advice;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.support.StaticMethodMatcherPointcutAdvisor;
import org.springframework.boot.logging.LogLevel;
import ru.tinkoff.eclair.definition.ErrorLogDefinition;
import ru.tinkoff.eclair.definition.InLogDefinition;
import ru.tinkoff.eclair.definition.LogDefinition;
import ru.tinkoff.eclair.definition.OutLogDefinition;
import ru.tinkoff.eclair.logger.EclairLogger;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

/**
 * @author Viacheslav Klapatniuk
 */
final class LogAdvisor extends StaticMethodMatcherPointcutAdvisor implements MethodInterceptor {

    private final EclairLogger eclairLogger;
    private final Map<Method, LogDefinition> logDefinitions;

    private LogAdvisor(EclairLogger eclairLogger, List<LogDefinition> logDefinitions) {
        this.eclairLogger = eclairLogger;
        this.logDefinitions = logDefinitions.stream().collect(toMap(LogDefinition::getMethod, identity()));
    }

    static LogAdvisor newInstance(EclairLogger eclairLogger, List<LogDefinition> logDefinitions) {
        return logDefinitions.isEmpty() ? null : new LogAdvisor(eclairLogger, logDefinitions);
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
            ErrorLogDefinition errorLogDefinition = logDefinition.findErrorLogDefinition(throwable.getClass());
            logErrorIfNecessary(invocation, errorLogDefinition, logDefinition.getOutLogDefinition(), throwable);
            throw throwable;
        }
        logOutIfNecessary(invocation, logDefinition.getOutLogDefinition(), result, false);
        return result;
    }

    private void logInIfNecessary(MethodInvocation invocation, InLogDefinition inLogDefinition) {
        if (nonNull(inLogDefinition)) {
            String loggerName = eclairLogger.getLoggerName(invocation);
            LogLevel level = getMinLevel(inLogDefinition.getLevel(), inLogDefinition.getIfEnabledLevel());
            if (eclairLogger.isLevelEnabled(loggerName, level)) {
                eclairLogger.logIn(invocation, loggerName, inLogDefinition);
            }
        }
    }

    private void logOutIfNecessary(MethodInvocation invocation, OutLogDefinition outLogDefinition, Object result, /* TODO: refactor */ boolean emergency) {
        if (nonNull(outLogDefinition)) {
            String loggerName = eclairLogger.getLoggerName(invocation);
            LogLevel level = getMinLevel(outLogDefinition.getLevel(), outLogDefinition.getIfEnabledLevel());
            if (eclairLogger.isLevelEnabled(loggerName, level)) {
                eclairLogger.logOut(invocation, loggerName, result, outLogDefinition, emergency);
            }
        }
    }

    private void logErrorIfNecessary(MethodInvocation invocation, ErrorLogDefinition errorLogDefinition, OutLogDefinition outLogDefinition, Throwable throwable) {
        if (isNull(errorLogDefinition)) {
            // TODO: refactor
            logOutIfNecessary(invocation, outLogDefinition, null, true);
        } else {
            String loggerName = eclairLogger.getLoggerName(invocation);
            LogLevel level = getMinLevel(outLogDefinition.getLevel(), outLogDefinition.getIfEnabledLevel());
            if (eclairLogger.isLevelEnabled(loggerName, level)) {
                eclairLogger.logError(invocation, loggerName, throwable, errorLogDefinition);
            }
        }
    }

    private LogLevel getMinLevel(LogLevel a, LogLevel b) {
        return a.ordinal() <= b.ordinal() ? a : b;
    }
}
