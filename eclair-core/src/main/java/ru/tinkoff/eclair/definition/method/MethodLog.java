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

package ru.tinkoff.eclair.definition.method;

import ru.tinkoff.eclair.annotation.Log;
import ru.tinkoff.eclair.core.ErrorLogResolver;
import ru.tinkoff.eclair.definition.ErrorLog;
import ru.tinkoff.eclair.definition.InLog;
import ru.tinkoff.eclair.definition.OutLog;
import ru.tinkoff.eclair.definition.ParameterLog;
import ru.tinkoff.eclair.definition.factory.ErrorLogFactory;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableSet;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.springframework.core.annotation.AnnotationUtils.synthesizeAnnotation;

/**
 * Composite DTO matches to set of @Log, @Log.in, @Log.out and @Log.error annotations
 * defined on {@link Method} and its {@link Parameter}s with one 'logger'.
 *
 * @author Vyacheslav Klapatnyuk
 * @see Log
 * @see ru.tinkoff.eclair.annotation.Log.in
 * @see ru.tinkoff.eclair.annotation.Log.out
 * @see ru.tinkoff.eclair.annotation.Log.error
 */
public class MethodLog implements MethodDefinition {

    private static final ErrorLog EMPTY = ErrorLogFactory.newInstance(synthesizeAnnotation(Log.error.class));

    private final ErrorLogResolver errorLogResolver = ErrorLogResolver.getInstance();
    private final Map<Class<? extends Throwable>, ErrorLog> errorLogCache = new ConcurrentHashMap<>();

    private final Method method;
    private final List<String> parameterNames;
    private final InLog inLog;
    private final List<ParameterLog> parameterLogs;
    private final OutLog outLog;
    private final Set<ErrorLog> errorLogs;

    public MethodLog(Method method,
                     List<String> parameterNames,
                     InLog inLog,
                     List<ParameterLog> parameterLogs,
                     OutLog outLog,
                     Set<ErrorLog> errorLogs) {
        this.method = method;
        this.parameterNames = unmodifiableList(parameterNames);
        this.inLog = inLog;
        this.parameterLogs = unmodifiableList(parameterLogs);
        this.outLog = outLog;
        this.errorLogs = unmodifiableSet(errorLogs);
    }

    @Override
    public Method getMethod() {
        return method;
    }

    public List<String> getParameterNames() {
        return parameterNames;
    }

    public InLog getInLog() {
        return inLog;
    }

    public List<ParameterLog> getParameterLogs() {
        return parameterLogs;
    }

    public OutLog getOutLog() {
        return outLog;
    }

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
