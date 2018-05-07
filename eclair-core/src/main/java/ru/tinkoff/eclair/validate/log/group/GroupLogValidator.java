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

package ru.tinkoff.eclair.validate.log.group;

import ru.tinkoff.eclair.core.AnnotationAttribute;
import ru.tinkoff.eclair.core.LoggerBeanNamesResolver;
import ru.tinkoff.eclair.validate.AnnotationUsageException;
import ru.tinkoff.eclair.validate.AnnotationUsageValidator;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.lang.String.format;
import static java.util.stream.Collectors.groupingBy;

/**
 * @author Vyacheslav Klapatnyuk
 */
abstract class GroupLogValidator<T extends Annotation> implements AnnotationUsageValidator<Set<T>> {

    private final Map<String, Set<String>> loggerNames;

    /**
     * @param loggerNames logger aliases grouped by its bean name
     * @see LoggerBeanNamesResolver#resolve(org.springframework.context.support.GenericApplicationContext, java.lang.String)
     */
    GroupLogValidator(Map<String, Set<String>> loggerNames) {
        this.loggerNames = loggerNames;
    }

    @Override
    public void validate(Method method, Set<T> target) throws AnnotationUsageException {
        groupAnnotationsByLogger(method, target).entrySet().stream()
                .filter(entry -> entry.getValue().size() > 1)
                .findFirst()
                .ifPresent(entry -> {
                    throw new AnnotationUsageException(method,
                            format("%s annotations with 'logger = %s' on the method", entry.getValue().size(), entry.getKey()),
                            "Use one annotation per 'logger'");
                });
    }

    Map<String, List<T>> groupAnnotationsByLogger(Method method, Set<T> annotations) {
        return annotations.stream()
                .collect(groupingBy(annotation -> getLoggerName(method, annotation)));
    }

    private String getLoggerName(Method method, T annotation) {
        String loggerName = AnnotationAttribute.LOGGER.extract(annotation);
        return loggerNames.entrySet().stream()
                .filter(entry -> entry.getValue().contains(loggerName))
                .findFirst()
                .map(Map.Entry::getKey)
                .orElseThrow(() -> {
                    if (loggerName.isEmpty()) {
                        return new AnnotationUsageException(method,
                                "Primary logger not found among candidates",
                                "Annotate the needed logger by '@Primary' or specify the logger name explicitly",
                                annotation);
                    }
                    return new AnnotationUsageException(method,
                            format("Unknown logger '%s'", loggerName),
                            "Use correct bean name or alias to specify 'logger'",
                            annotation);
                });
    }
}
