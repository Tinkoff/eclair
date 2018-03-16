package ru.tinkoff.eclair.definition;

import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.tinkoff.eclair.annotation.Log;
import ru.tinkoff.eclair.core.ErrorLogDefinitionResolver;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Collections.unmodifiableList;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.springframework.core.annotation.AnnotationUtils.synthesizeAnnotation;

/**
 * @author Viacheslav Klapatniuk
 */
@AllArgsConstructor
public class LogDefinition {

    private static final ErrorLogDefinition EMPTY = new ErrorLogDefinition(synthesizeAnnotation(Log.error.class));

    @Getter
    private final Method method;
    @Getter
    private final InLogDefinition inLogDefinition;
    @Getter
    private final OutLogDefinition outLogDefinition;
    private final List<ErrorLogDefinition> errorLogDefinitions;

    private final ErrorLogDefinitionResolver errorLogDefinitionResolver = ErrorLogDefinitionResolver.getInstance();
    private final Map<Class<? extends Throwable>, ErrorLogDefinition> errorLogDefinitionCache = new ConcurrentHashMap<>();

    /**
     * @param inLogDefinition  may be {@code null}
     * @param outLogDefinition may be {@code null}
     * @return Instantiated {@link InLogDefinition} or {@code null}
     */
    public static LogDefinition newInstance(Method method,
                                            InLogDefinition inLogDefinition,
                                            OutLogDefinition outLogDefinition,
                                            List<ErrorLogDefinition> errorLogDefinitions) {
        return isNull(inLogDefinition) && isNull(outLogDefinition) && errorLogDefinitions.isEmpty() ?
                null : new LogDefinition(method, inLogDefinition, outLogDefinition, unmodifiableList(errorLogDefinitions));
    }

    public ErrorLogDefinition findErrorLogDefinition(Class<? extends Throwable> causeClass) {
        ErrorLogDefinition found = errorLogDefinitionCache.get(causeClass);
        if (nonNull(found)) {
            return found == EMPTY ? null : found;
        }
        ErrorLogDefinition resolved = errorLogDefinitionResolver.resolve(errorLogDefinitions, causeClass);
        errorLogDefinitionCache.put(causeClass, isNull(resolved) ? EMPTY : resolved);
        return resolved;
    }
}
