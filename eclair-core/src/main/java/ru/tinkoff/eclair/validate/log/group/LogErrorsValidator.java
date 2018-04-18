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

import org.springframework.context.support.GenericApplicationContext;
import org.springframework.validation.Errors;
import ru.tinkoff.eclair.annotation.Log;
import ru.tinkoff.eclair.core.ErrorFilterFactory;
import ru.tinkoff.eclair.printer.resolver.PrinterResolver;
import ru.tinkoff.eclair.definition.ErrorLog;
import ru.tinkoff.eclair.logger.EclairLogger;
import ru.tinkoff.eclair.validate.log.single.LogErrorValidator;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.lang.String.format;

/**
 * @author Vyacheslav Klapatnyuk
 */
public class LogErrorsValidator extends LoggerSpecificLogAnnotationsValidator {

    private final ErrorFilterFactory errorFilterFactory = ErrorFilterFactory.getInstance();

    private final LogErrorValidator logErrorValidator;

    public LogErrorsValidator(GenericApplicationContext applicationContext,
                              Map<String, EclairLogger> loggers,
                              PrinterResolver printerResolver) {
        super(applicationContext, loggers);
        logErrorValidator = new LogErrorValidator(printerResolver);
    }

    @Override
    public void validate(Object target, Errors errors) {
        @SuppressWarnings("unchecked")
        Set<Log.error> logErrors = (Set<Log.error>) target;

        groupAnnotationsByLogger(logErrors, errors).entrySet().stream()
                .filter(entry -> entry.getValue().size() > 1)
                .forEach(entry -> {
                    List<Log.error> loggerLogErrors = entry.getValue();
                    Set<ErrorLog.Filter> filters = loggerLogErrors.stream()
                            .map(error -> errorFilterFactory.buildErrorFilter(error.ofType(), error.exclude()))
                            .collect(Collectors.toSet());
                    if (loggerLogErrors.size() > filters.size()) {
                        errors.reject("errors.logger.duplicate",
                                format("Error filters duplicated for logger bean '%s': %s", entry.getKey(), loggerLogErrors));
                    }
                });

        logErrors.forEach(logError -> logErrorValidator.validate(logError, errors));
    }
}
