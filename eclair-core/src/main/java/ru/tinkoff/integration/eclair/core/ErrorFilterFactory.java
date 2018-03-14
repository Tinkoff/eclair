package ru.tinkoff.integration.eclair.core;

import ru.tinkoff.integration.eclair.definition.ErrorFilter;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toSet;

/**
 * @author Viacheslav Klapatniuk
 */
public final class ErrorFilterFactory {

    private static final ErrorFilterFactory instance = new ErrorFilterFactory();

    private ErrorFilterFactory() {
    }

    public static ErrorFilterFactory getInstance() {
        return instance;
    }

    public ErrorFilter buildErrorFilter(Class<? extends Throwable>[] rawIncludes, Class<? extends Throwable>[] rawExcludes) {
        Set<Class<? extends Throwable>> includeCandidates = ClassUtils.reduceDescendants(new HashSet<>(asList(rawIncludes)));
        Set<Class<? extends Throwable>> excludeCandidates = ClassUtils.reduceDescendants(new HashSet<>(asList(rawExcludes)));
        Set<Class<? extends Throwable>> includes = optimizeAndSortIncludes(includeCandidates, excludeCandidates);
        Set<Class<? extends Throwable>> excludes = optimizeAndSortExcludes(includes, excludeCandidates);
        return new ErrorFilter(includes, excludes);
    }

    /**
     * @return Optimized 'include' error set.
     */
    private Set<Class<? extends Throwable>> optimizeAndSortIncludes(Collection<Class<? extends Throwable>> includes,
                                                                    Collection<Class<? extends Throwable>> excludes) {
        return includes.stream()
                .filter(include -> excludes.stream().noneMatch(exclude -> exclude.isAssignableFrom(include)))
                .collect(toSet());
    }

    /**
     * @return Optimized 'exclude' error set.
     */
    private Set<Class<? extends Throwable>> optimizeAndSortExcludes(Collection<Class<? extends Throwable>> includes,
                                                                    Collection<Class<? extends Throwable>> excludes) {
        return excludes.stream()
                .filter(exclude -> includes.stream().anyMatch(include -> include.isAssignableFrom(exclude)))
                .collect(toSet());
    }
}
