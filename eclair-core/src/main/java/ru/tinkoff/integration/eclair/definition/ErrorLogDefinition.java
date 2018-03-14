package ru.tinkoff.integration.eclair.definition;

import lombok.Getter;
import org.springframework.boot.logging.LogLevel;
import ru.tinkoff.integration.eclair.annotation.Log;
import ru.tinkoff.integration.eclair.core.AnnotationAttribute;
import ru.tinkoff.integration.eclair.core.ErrorFilterFactory;

import java.util.Set;

/**
 * @author Viacheslav Klapatniuk
 */
public class ErrorLogDefinition {

    @Getter
    private final LogLevel level;
    @Getter
    private final LogLevel ifEnabledLevel;
    private final ErrorFilter errorFilter;

    public ErrorLogDefinition(Log.error logError) {
        this.level = AnnotationAttribute.LEVEL.extract(logError);
        this.ifEnabledLevel = logError.ifEnabled();
        this.errorFilter = ErrorFilterFactory.getInstance().buildErrorFilter(logError.ofType(), logError.exclude());
    }

    public Set<Class<? extends Throwable>> getIncludes() {
        return errorFilter.getIncludes();
    }

    public Set<Class<? extends Throwable>> getExcludes() {
        return errorFilter.getExcludes();
    }
}
