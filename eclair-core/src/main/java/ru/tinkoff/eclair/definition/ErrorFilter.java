package ru.tinkoff.eclair.definition;

import lombok.EqualsAndHashCode;
import lombok.Getter;
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
@Getter
@EqualsAndHashCode
public class ErrorFilter {

    private static final Comparator<Class> classComparator =
            comparing((Function<Class, Integer>) clazz -> ClassUtils.calculateInheritanceDistance(Throwable.class, clazz))
                    .thenComparing(Class::getName);

    private final Set<Class<? extends Throwable>> includes;
    private final Set<Class<? extends Throwable>> excludes;

    public ErrorFilter(Set<Class<? extends Throwable>> includes, Set<Class<? extends Throwable>> excludes) {
        this.includes = constructSortedUnmodifiableSet(includes);
        this.excludes = constructSortedUnmodifiableSet(excludes);
    }

    private Set<Class<? extends Throwable>> constructSortedUnmodifiableSet(Set<Class<? extends Throwable>> input) {
        Set<Class<? extends Throwable>> result = new TreeSet<>(classComparator);
        result.addAll(input);
        return unmodifiableSet(result);
    }
}
