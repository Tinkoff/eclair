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

import ru.tinkoff.eclair.annotation.Log;
import ru.tinkoff.eclair.core.ErrorFilterFactory;
import ru.tinkoff.eclair.validate.AnnotationUsageException;
import ru.tinkoff.eclair.validate.log.single.LogErrorValidator;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.lang.String.format;
import static java.util.stream.Collectors.toSet;

/**
 * @author Vyacheslav Klapatnyuk
 */
public class LogErrorsValidator extends GroupLogValidator<Log.error> {

    private final ErrorFilterFactory errorFilterFactory = ErrorFilterFactory.getInstance();

    private final LogErrorValidator logErrorValidator;

    public LogErrorsValidator(Map<String, Set<String>> loggerNames,
                              LogErrorValidator logErrorValidator) {
        super(loggerNames);
        this.logErrorValidator = logErrorValidator;
    }

    @Override
    public void validate(Method method, Set<Log.error> target) throws AnnotationUsageException {
        groupAnnotationsByLogger(method, target).entrySet().stream()
                .filter(entry -> entry.getValue().size() > 1)
                .forEach(entry -> {
                    List<Log.error> logErrors = entry.getValue();
                    long filterCount = logErrors.stream()
                            .map(error -> errorFilterFactory.buildErrorFilter(error.ofType(), error.exclude()))
                            .collect(toSet())
                            .size();
                    if (logErrors.size() > filterCount) {
                        throw new AnnotationUsageException(method,
                                format("Duplicated error filters with 'logger = %s' on the method", entry.getKey()),
                                "Optimize error filters or specify a different logger names");
                    }
                });

        target.forEach(logError -> logErrorValidator.validate(method, logError));
    }
}
