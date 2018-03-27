package ru.tinkoff.eclair.aop;

import org.aopalliance.aop.Advice;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.support.StaticMethodMatcherPointcutAdvisor;
import org.springframework.core.BridgeMethodResolver;
import ru.tinkoff.eclair.definition.MethodLog;
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
    private final Map<Method, MethodLog> methodLogs;

    private LogAdvisor(EclairLogger eclairLogger, List<MethodLog> methodLogs) {
        this.eclairLogger = eclairLogger;
        this.methodLogs = methodLogs.stream().collect(toMap(MethodLog::getMethod, identity()));
    }

    static LogAdvisor newInstance(EclairLogger eclairLogger, List<MethodLog> methodLogs) {
        return methodLogs.isEmpty() ? null : new LogAdvisor(eclairLogger, methodLogs);
    }

    @Override
    public Advice getAdvice() {
        return this;
    }

    @Override
    public boolean matches(Method method, Class<?> targetClass) {
        return methodLogs.containsKey(method) || methodLogs.containsKey(BridgeMethodResolver.findBridgedMethod(method));
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
        MethodLog methodLog = methodLogs.get(invocation.getMethod());
        eclairLogger.logInIfNecessary(invocation, methodLog);
        Object result;
        try {
            result = invocation.proceed();
        } catch (Throwable throwable) {
            eclairLogger.logErrorIfNecessary(invocation, methodLog, throwable);
            throw throwable;
        }
        eclairLogger.logOutIfNecessary(invocation, methodLog, result);
        return result;
    }
}
