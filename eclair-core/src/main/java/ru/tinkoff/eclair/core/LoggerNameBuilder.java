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

package ru.tinkoff.eclair.core;

import org.aopalliance.intercept.MethodInvocation;
import ru.tinkoff.eclair.logger.ManualLogger;

import java.lang.reflect.Method;

/**
 * @author Vyacheslav Klapatnyuk
 */
public final class LoggerNameBuilder {

    private static final LoggerNameBuilder instance = new LoggerNameBuilder();
    private static final int MIN_CURRENT_DEPTH = 3;

    private LoggerNameBuilder() {
    }

    public static LoggerNameBuilder getInstance() {
        return instance;
    }

    public String build(MethodInvocation invocation) {
        return build(invocation.getMethod());
    }

    public String build(Method method) {
        return build(method.getDeclaringClass().getName(), method.getName());
    }

    public String buildByInvoker() {
        StackTraceElement invoker = resolveLoggerInvoker();
        return build(invoker.getClassName(), invoker.getMethodName());
    }

    private StackTraceElement resolveLoggerInvoker() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        int length = stackTrace.length;
        String previousName = "";
        for (int a = MIN_CURRENT_DEPTH; a < length; a++) {
            String className = stackTrace[a].getClassName();
            if (className.equals(previousName)) {
                continue;
            }
            previousName = className;
            if (ManualLogger.class.isAssignableFrom(forName(className))) {
                for (int b = a + 1; b < length; b++) {
                    className = stackTrace[b].getClassName();
                    if (className.equals(previousName)) {
                        continue;
                    }
                    if (!ManualLogger.class.isAssignableFrom(forName(className))) {
                        return stackTrace[b];
                    }
                    previousName = className;
                }
                break;
            }
        }
        throw new IllegalArgumentException("Invalid stacktrace");
    }

    private Class<?> forName(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private String build(String className, String methodName) {
        return className + '.' + methodName;
    }
}
