/*
 * Copyright 2018 Tinkoff Bank
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
 * @author Vyacheslav Klapatnyuk
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

    EclairLogger getEclairLogger() {
        return eclairLogger;
    }

    Map<Method, MethodLog> getMethodLogs() {
        return methodLogs;
    }
}
