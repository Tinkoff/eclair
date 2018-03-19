package ru.tinkoff.eclair.definition.factory;

import ru.tinkoff.eclair.annotation.Log;
import ru.tinkoff.eclair.core.AnnotationAttribute;
import ru.tinkoff.eclair.core.ErrorFilterFactory;
import ru.tinkoff.eclair.definition.ErrorLog;

/**
 * @author Viacheslav Klapatniuk
 */
public class ErrorLogFactory {

    private static final ErrorFilterFactory errorFilterFactory = ErrorFilterFactory.getInstance();

    public static ErrorLog newInstance(Log.error logError) {
        return ErrorLog.builder()
                .level(AnnotationAttribute.LEVEL.extract(logError))
                .ifEnabledLevel(logError.ifEnabled())
                .verboseLevel(logError.verbose())
                .filter(errorFilterFactory.buildErrorFilter(logError.ofType(), logError.exclude()))
                .build();
    }
}
