package ru.tinkoff.integration.eclair.core;

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

    public String build(Method method) {
        return build(method.getDeclaringClass().getName(), method.getName());
    }

    public String build(StackTraceElement stackTraceElement) {
        return build(stackTraceElement.getClassName(), stackTraceElement.getMethodName());
    }

    private String build(String className, String methodName) {
        return className + '.' + methodName;
    }
}
