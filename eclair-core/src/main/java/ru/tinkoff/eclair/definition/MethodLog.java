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
import lombok.Getter;
import lombok.NonNull;
import lombok.Singular;
import ru.tinkoff.eclair.annotation.Log;
import ru.tinkoff.eclair.core.ErrorLogResolver;
import ru.tinkoff.eclair.definition.factory.ErrorLogFactory;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.springframework.core.annotation.AnnotationUtils.synthesizeAnnotation;

/**
 * @author Viacheslav Klapatniuk
 */
@Builder
public class MethodLog {

    private static final ErrorLog EMPTY = ErrorLogFactory.newInstance(synthesizeAnnotation(Log.error.class));

    @Getter
    @NonNull
    private Method method;

    @Getter
    @NonNull
    @Singular
    private List<String> parameterNames;

    @Getter
    private InLog inLog;

    @Getter
    @NonNull
    @Singular
    private List<ParameterLog> parameterLogs;

    @Getter
    private OutLog outLog;

    @NonNull
    @Singular
    private Set<ErrorLog> errorLogs;

    private final ErrorLogResolver errorLogResolver = ErrorLogResolver.getInstance();
    private final Map<Class<? extends Throwable>, ErrorLog> errorLogCache = new ConcurrentHashMap<>();

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
