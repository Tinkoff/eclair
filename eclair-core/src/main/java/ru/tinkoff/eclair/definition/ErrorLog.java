package ru.tinkoff.eclair.definition;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import org.springframework.boot.logging.LogLevel;
import ru.tinkoff.eclair.core.ClassUtils;

import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;

import static java.util.Collections.unmodifiableSet;
import static java.util.Comparator.comparing;

/**
 * @author Viacheslav Klapatniuk
 */
@Builder
public class ErrorLog implements LogDefinition {

    @Getter
    @NonNull
    private LogLevel level;
    @Getter
    @NonNull
    private LogLevel ifEnabledLevel;
    @Getter
    @NonNull
    private LogLevel verboseLevel;
    @NonNull
    private Filter filter;

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
