/*
 * Copyright 2018 Tinkoff Bank
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ru.tinkoff.eclair.core;

import ru.tinkoff.eclair.definition.ErrorLog;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toSet;

/**
 * @author Vyacheslav Klapatnyuk
 */
public final class ErrorFilterFactory {

    private static final ErrorFilterFactory instance = new ErrorFilterFactory();

    private ErrorFilterFactory() {
    }

    public static ErrorFilterFactory getInstance() {
        return instance;
    }

    public ErrorLog.Filter buildErrorFilter(Class<? extends Throwable>[] rawIncludes, Class<? extends Throwable>[] rawExcludes) {
        Set<Class<? extends Throwable>> includeCandidates = RelationResolver.reduceDescendants(new HashSet<>(asList(rawIncludes)));
        Set<Class<? extends Throwable>> excludeCandidates = RelationResolver.reduceDescendants(new HashSet<>(asList(rawExcludes)));
        Set<Class<? extends Throwable>> includes = optimizeAndSortIncludes(includeCandidates, excludeCandidates);
        Set<Class<? extends Throwable>> excludes = optimizeAndSortExcludes(includes, excludeCandidates);
        return new ErrorLog.Filter(includes, excludes);
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
