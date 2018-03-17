package ru.tinkoff.eclair.core;

import ru.tinkoff.eclair.definition.ErrorLog;

import java.util.Set;

/**
 * @author Viacheslav Klapatniuk
 */
public final class ErrorLogResolver {

    private static final ErrorLogResolver instance = new ErrorLogResolver();

    private ErrorLogResolver() {
    }

    public static ErrorLogResolver getInstance() {
        return instance;
    }

    public ErrorLog resolve(Set<ErrorLog> errorLogs, Class<?> causeClass) {
        int minDistance = -1;
        ErrorLog result = null;
        for (ErrorLog errorLog : errorLogs) {
            if (noneMatchExclude(errorLog, causeClass)) {
                int distance = minInheritanceDistance(errorLog, causeClass);
                if (distance >= 0 && (minDistance < 0 || distance < minDistance)) {
                    minDistance = distance;
                    result = errorLog;
                }
            }
        }
        return result;
    }

    private boolean noneMatchExclude(ErrorLog errorLog, Class<?> causeClass) {
        for (Class<? extends Throwable> exclude : errorLog.getExcludes()) {
            if (exclude.isAssignableFrom(causeClass)) {
                return false;
            }
        }
        return true;
    }

    private int minInheritanceDistance(ErrorLog errorLog, Class<?> causeClass) {
        int result = -1;
        for (Class<? extends Throwable> include : errorLog.getIncludes()) {
            int distance = ClassUtils.calculateInheritanceDistance(include, causeClass);
            if (distance >= 0 && (result < 0 || distance < result)) {
                result = distance;
            }
        }
        return result;
    }
}
