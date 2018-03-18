package ru.tinkoff.eclair.definition;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.boot.logging.LogLevel;
import ru.tinkoff.eclair.annotation.Log;
import ru.tinkoff.eclair.core.AnnotationAttribute;
import ru.tinkoff.eclair.core.ClassUtils;
import ru.tinkoff.eclair.core.ErrorFilterFactory;

import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;

import static java.util.Collections.unmodifiableSet;
import static java.util.Comparator.comparing;

/**
 * TODO: extract instantiation logic to factory, generate 'builder' methods
 *
 * @author Viacheslav Klapatniuk
 */
public class ErrorLog implements LogDefinition {

    @Getter
    private final LogLevel level;
    @Getter
    private final LogLevel ifEnabledLevel;
    @Getter
    private final LogLevel verboseLevel;
    private final Filter filter;

    public ErrorLog(Log.error logError) {
        this.level = AnnotationAttribute.LEVEL.extract(logError);
        this.ifEnabledLevel = logError.ifEnabled();
        this.verboseLevel = logError.verbose();
        this.filter = ErrorFilterFactory.getInstance().buildErrorFilter(logError.ofType(), logError.exclude());
    }

    public Set<Class<? extends Throwable>> getIncludes() {
        return filter.getIncludes();
    }

    public Set<Class<? extends Throwable>> getExcludes() {
        return filter.getExcludes();
    }

    @Getter
    @EqualsAndHashCode
    public static class Filter {

        private static final Comparator<Class> classComparator =
                comparing((Function<Class, Integer>) clazz -> ClassUtils.calculateInheritanceDistance(Throwable.class, clazz))
                        .thenComparing(Class::getName);

        private final Set<Class<? extends Throwable>> includes;
        private final Set<Class<? extends Throwable>> excludes;

        public Filter(Set<Class<? extends Throwable>> includes, Set<Class<? extends Throwable>> excludes) {
            this.includes = constructSortedUnmodifiableSet(includes);
            this.excludes = constructSortedUnmodifiableSet(excludes);
        }

        private Set<Class<? extends Throwable>> constructSortedUnmodifiableSet(Set<Class<? extends Throwable>> input) {
            Set<Class<? extends Throwable>> result = new TreeSet<>(classComparator);
            result.addAll(input);
            return unmodifiableSet(result);
        }
    }
}
