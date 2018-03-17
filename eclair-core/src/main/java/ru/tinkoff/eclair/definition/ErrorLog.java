package ru.tinkoff.eclair.definition;

import lombok.Getter;
import org.springframework.boot.logging.LogLevel;
import ru.tinkoff.eclair.annotation.Log;
import ru.tinkoff.eclair.core.AnnotationAttribute;
import ru.tinkoff.eclair.core.ErrorFilterFactory;

import java.util.Set;

/**
 * @author Viacheslav Klapatniuk
 */
public class ErrorLog implements LogDefinition {

    @Getter
    private final LogLevel level;
    @Getter
    private final LogLevel ifEnabledLevel;
    @Getter
    private final LogLevel verboseLevel;
    private final ErrorFilter errorFilter;

    public ErrorLog(Log.error logError) {
        this.level = AnnotationAttribute.LEVEL.extract(logError);
        this.ifEnabledLevel = logError.ifEnabled();
        this.verboseLevel = logError.verbose();
        this.errorFilter = ErrorFilterFactory.getInstance().buildErrorFilter(logError.ofType(), logError.exclude());
    }

    public Set<Class<? extends Throwable>> getIncludes() {
        return errorFilter.getIncludes();
    }

    public Set<Class<? extends Throwable>> getExcludes() {
        return errorFilter.getExcludes();
    }
}