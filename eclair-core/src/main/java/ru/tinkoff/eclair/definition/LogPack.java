package ru.tinkoff.eclair.definition;

import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.tinkoff.eclair.annotation.Log;
import ru.tinkoff.eclair.core.ErrorLogResolver;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Collections.unmodifiableSet;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.springframework.core.annotation.AnnotationUtils.synthesizeAnnotation;

/**
 * TODO: extract instantiation logic to factory, generate 'builder' methods
 *
 * @author Viacheslav Klapatniuk
 */
@AllArgsConstructor
public class LogPack {

    private static final ErrorLog EMPTY = new ErrorLog(synthesizeAnnotation(Log.error.class));

    @Getter
    private final Method method;
    @Getter
    private final InLog inLog;
    @Getter
    private final OutLog outLog;
    private final Set<ErrorLog> errorLogs;

    private final ErrorLogResolver errorLogResolver = ErrorLogResolver.getInstance();
    private final Map<Class<? extends Throwable>, ErrorLog> errorLogCache = new ConcurrentHashMap<>();

    /**
     * @param inLog  may be {@code null}
     * @param outLog may be {@code null}
     * @return Instantiated {@link InLog} or {@code null}
     */
    public static LogPack newInstance(Method method,
                                      InLog inLog,
                                      OutLog outLog,
                                      Set<ErrorLog> errorLogs) {
        return isNull(inLog) && isNull(outLog) && errorLogs.isEmpty() ?
                null : new LogPack(method, inLog, outLog, unmodifiableSet(errorLogs));
    }

    public ErrorLog findErrorLog(Class<? extends Throwable> causeClass) {
        ErrorLog found = errorLogCache.get(causeClass);
        if (nonNull(found)) {
            return found == EMPTY ? null : found;
        }
        ErrorLog resolved = errorLogResolver.resolve(errorLogs, causeClass);
        errorLogCache.put(causeClass, isNull(resolved) ? EMPTY : resolved);
        return resolved;
    }
}
