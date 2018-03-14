package ru.tinkoff.integration.eclair.core;

import ru.tinkoff.integration.eclair.definition.ErrorLogDefinition;

import java.util.List;

/**
 * @author Viacheslav Klapatniuk
 */
public final class ErrorLogDefinitionResolver {

    private static final ErrorLogDefinitionResolver instance = new ErrorLogDefinitionResolver();

    private ErrorLogDefinitionResolver() {
    }

    public static ErrorLogDefinitionResolver getInstance() {
        return instance;
    }

    public ErrorLogDefinition resolve(List<ErrorLogDefinition> errorLogDefinitions, Class<?> causeClass) {
        int minDistance = -1;
        ErrorLogDefinition result = null;
        for (ErrorLogDefinition errorLogDefinition : errorLogDefinitions) {
            if (noneMatchExclude(errorLogDefinition, causeClass)) {
                int distance = minInheritanceDistance(errorLogDefinition, causeClass);
                if (distance >= 0 && (minDistance < 0 || distance < minDistance)) {
                    minDistance = distance;
                    result = errorLogDefinition;
                }
            }
        }
        return result;
    }

    private boolean noneMatchExclude(ErrorLogDefinition errorLogDefinition, Class<?> causeClass) {
        for (Class<? extends Throwable> exclude : errorLogDefinition.getExcludes()) {
            if (exclude.isAssignableFrom(causeClass)) {
                return false;
            }
        }
        return true;
    }

    private int minInheritanceDistance(ErrorLogDefinition errorLogDefinition, Class<?> causeClass) {
        int result = -1;
        for (Class<? extends Throwable> include : errorLogDefinition.getIncludes()) {
            int distance = ClassUtils.calculateInheritanceDistance(include, causeClass);
            if (distance >= 0 && (result < 0 || distance < result)) {
                result = distance;
            }
        }
        return result;
    }
}
