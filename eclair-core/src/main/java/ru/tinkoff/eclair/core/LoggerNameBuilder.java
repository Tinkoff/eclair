package ru.tinkoff.eclair.core;

import org.aopalliance.intercept.MethodInvocation;

import java.lang.reflect.Method;

/**
 * @author Viacheslav Klapatniuk
 */
public final class LoggerNameBuilder {

    private static final LoggerNameBuilder instance = new LoggerNameBuilder();

    private LoggerNameBuilder() {
    }

    public static LoggerNameBuilder getInstance() {
        return instance;
    }

    public String build(MethodInvocation methodInvocation) {
        Method method = methodInvocation.getMethod();
        return build(method.getDeclaringClass().getName(), method.getName());
    }

    public String build(Class<?> invokedClass) {
        StackTraceElement invoker = resolveInvoker(invokedClass);
        return build(invoker.getClassName(), invoker.getMethodName());
    }

    private StackTraceElement resolveInvoker(Class<?> clazz) {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        for (int a = stackTrace.length - 2; a >= 0; a--) {
            if (stackTrace[a].getClassName().equals(clazz.getName())) {
                return stackTrace[a + 1];
            }
        }
        throw new IllegalArgumentException("Invalid stacktrace");
    }

    private String build(String className, String methodName) {
        return className + '.' + methodName;
    }
}
