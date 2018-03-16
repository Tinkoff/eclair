package ru.tinkoff.eclair.core;

import org.springframework.boot.logging.LogLevel;
import org.springframework.boot.logging.LoggingSystem;

import java.lang.reflect.Method;

/**
 * @author Viacheslav Klapatniuk
 */
public final class ActualLevelResolver {

    private static final ActualLevelResolver instance = new ActualLevelResolver();
    private static final LoggingSystem loggingSystem = LoggingSystem.get(ActualLevelResolver.class.getClassLoader());

    private final LoggerNameBuilder loggerNameBuilder = LoggerNameBuilder.getInstance();

    private ActualLevelResolver() {
    }

    public static ActualLevelResolver getInstance() {
        return instance;
    }

    public LogLevel resolve(Method method) {
        return resolve(loggerNameBuilder.build(method));
    }

    public LogLevel resolve(StackTraceElement stackTraceElement) {
        return resolve(loggerNameBuilder.build(stackTraceElement));
    }

    public LogLevel resolve(String loggerName) {
        return loggingSystem.getLoggerConfiguration(loggerName).getEffectiveLevel();
    }
}
