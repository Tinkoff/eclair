package ru.tinkoff.eclair.aop;

import org.aopalliance.aop.Advice;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.support.StaticMethodMatcherPointcutAdvisor;
import ru.tinkoff.eclair.definition.LogPack;
import ru.tinkoff.eclair.logger.EclairLogger;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

/**
 * @author Viacheslav Klapatniuk
 */
final class LogAdvisor extends StaticMethodMatcherPointcutAdvisor implements MethodInterceptor {

    private final EclairLogger eclairLogger;
    private final Map<Method, LogPack> logPacks;

    private LogAdvisor(EclairLogger eclairLogger, List<LogPack> logPacks) {
        this.eclairLogger = eclairLogger;
        this.logPacks = logPacks.stream().collect(toMap(LogPack::getMethod, identity()));
    }

    static LogAdvisor newInstance(EclairLogger eclairLogger, List<LogPack> logPacks) {
        return logPacks.isEmpty() ? null : new LogAdvisor(eclairLogger, logPacks);
    }

    @Override
    public Advice getAdvice() {
        return this;
    }

    @Override
    public boolean matches(Method method, Class<?> targetClass) {
        return logPacks.containsKey(method);
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
        LogPack logPack = logPacks.get(invocation.getMethod());
        eclairLogger.logInIfNecessary(invocation, logPack);
        Object result;
        try {
            result = invocation.proceed();
        } catch (Throwable throwable) {
            eclairLogger.logErrorIfNecessary(invocation, logPack, throwable);
            throw throwable;
        }
        eclairLogger.logOutIfNecessary(invocation, logPack, result);
        return result;
    }
}
