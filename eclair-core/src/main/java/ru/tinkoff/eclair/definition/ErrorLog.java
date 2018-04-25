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

package ru.tinkoff.eclair.definition;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import org.springframework.boot.logging.LogLevel;
import ru.tinkoff.eclair.core.RelationResolver;

import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;

import static java.util.Collections.unmodifiableSet;
import static java.util.Comparator.comparing;

/**
 * @author Vyacheslav Klapatnyuk
 */
@Builder
public class ErrorLog implements LogDefinition {

    @NonNull
    private LogLevel level;

    @NonNull
    private LogLevel ifEnabledLevel;

    @NonNull
    private LogLevel verboseLevel;

    @NonNull
    private Filter filter;

    @Override
    public LogLevel getLevel() {
        return level;
    }

    @Override
    public LogLevel getIfEnabledLevel() {
        return ifEnabledLevel;
    }

    @Override
    public LogLevel getVerboseLevel() {
        return verboseLevel;
    }

    public Set<Class<? extends Throwable>> getIncludes() {
        return filter.getIncludes();
    }

    public Set<Class<? extends Throwable>> getExcludes() {
        return filter.getExcludes();
    }

    @EqualsAndHashCode
    public static class Filter {

        private static final Comparator<Class> classComparator =
                comparing((Function<Class, Integer>) clazz -> RelationResolver.calculateInheritanceDistance(Throwable.class, clazz))
                        .thenComparing(Class::getName);

        private final Set<Class<? extends Throwable>> includes;
        private final Set<Class<? extends Throwable>> excludes;

        public Filter(Set<Class<? extends Throwable>> includes, Set<Class<? extends Throwable>> excludes) {
            this.includes = constructSortedUnmodifiableSet(includes);
            this.excludes = constructSortedUnmodifiableSet(excludes);
        }

        public Set<Class<? extends Throwable>> getIncludes() {
            return includes;
        }

        public Set<Class<? extends Throwable>> getExcludes() {
            return excludes;
        }

        private Set<Class<? extends Throwable>> constructSortedUnmodifiableSet(Set<Class<? extends Throwable>> input) {
            Set<Class<? extends Throwable>> result = new TreeSet<>(classComparator);
            result.addAll(input);
            return unmodifiableSet(result);
        }
    }
}
