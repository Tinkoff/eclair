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

package ru.tinkoff.eclair.validate;

import org.springframework.context.support.GenericApplicationContext;
import ru.tinkoff.eclair.annotation.Log;
import ru.tinkoff.eclair.annotation.Mdc;
import ru.tinkoff.eclair.core.AnnotationExtractor;
import ru.tinkoff.eclair.core.LoggerBeanNamesResolver;
import ru.tinkoff.eclair.exception.AnnotationUsageException;
import ru.tinkoff.eclair.logger.EclairLogger;
import ru.tinkoff.eclair.printer.resolver.PrinterResolver;
import ru.tinkoff.eclair.validate.log.group.*;
import ru.tinkoff.eclair.validate.mdc.MdcsValidator;
import ru.tinkoff.eclair.validate.mdc.MergedMdcsValidator;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

/**
 * @author Vyacheslav Klapatnyuk
 */
public class MethodValidator implements AnnotationUsageValidator<Method> {

    private final AnnotationExtractor annotationExtractor = AnnotationExtractor.getInstance();

    private final LogsValidator logsValidator;
    private final LogInsValidator logInsValidator;
    private final LogOutsValidator logOutsValidator;
    private final LogErrorsValidator logErrorsValidator;
    private final ParameterLogsValidator parameterLogsValidator;

    private final MdcsValidator mdcsValidator = new MdcsValidator();
    private final MergedMdcsValidator mergedMdcsValidator = new MergedMdcsValidator();

    public MethodValidator(GenericApplicationContext applicationContext,
                           Map<String, EclairLogger> loggers,
                           PrinterResolver printerResolver) {
        Map<String, Set<String>> loggerNames = loggers.keySet().stream()
                .collect(toMap(
                        identity(),
                        loggerName -> LoggerBeanNamesResolver.getInstance().resolve(applicationContext, loggerName)
                ));
        this.logsValidator = new LogsValidator(loggerNames, printerResolver);
        this.logInsValidator = new LogInsValidator(loggerNames, printerResolver);
        this.logOutsValidator = new LogOutsValidator(loggerNames, printerResolver);
        this.logErrorsValidator = new LogErrorsValidator(loggerNames, printerResolver);
        this.parameterLogsValidator = new ParameterLogsValidator(loggerNames, printerResolver);
    }

    /**
     * TODO: Log + Log.in           -> Log.in + Log.out ?-> Log
     * TODO: Log + Log.out          -> Log.in + Log.out ?-> Log
     * TODO: Log + Log.in + Log.out -> Log.in + Log.out ?-> Log
     * TODO: Log.in + Log.out      ?-> Log
     */
    @Override
    public void validate(Method method, Method target) throws AnnotationUsageException {
        Set<Log> logs = annotationExtractor.getLogs(target);
        boolean methodAnnotationFound = !logs.isEmpty();

        Set<Log.in> logIns = annotationExtractor.getLogIns(target);
        methodAnnotationFound |= !logIns.isEmpty();

        Set<Log.out> logOuts = annotationExtractor.getLogOuts(target);
        methodAnnotationFound |= !logOuts.isEmpty();

        Set<Log.error> logErrors = annotationExtractor.getLogErrors(target);
        methodAnnotationFound |= !logErrors.isEmpty();

        Set<Mdc> mdcs = annotationExtractor.getMdcs(target);
        methodAnnotationFound |= !mdcs.isEmpty();

        List<Set<Log>> parameterLogs = annotationExtractor.getParameterLogs(target);
        boolean argAnnotationFound = !parameterLogs.stream().allMatch(Set::isEmpty);

        List<Set<Mdc>> parameterMdcs = annotationExtractor.getParametersMdcs(target);
        argAnnotationFound |= !parameterMdcs.stream().allMatch(Set::isEmpty);

        if (Modifier.isPrivate(target.getModifiers())) {
            if (methodAnnotationFound) {
                throw new AnnotationUsageException("Annotated method could not be private", target);
            }
            if (argAnnotationFound) {
                throw new AnnotationUsageException("Method with annotated parameters could not be private", target);
            }
        }
        if (Modifier.isStatic(target.getModifiers())) {
            if (methodAnnotationFound) {
                throw new AnnotationUsageException("Annotated method could not be static", target);
            }
            if (argAnnotationFound) {
                throw new AnnotationUsageException("Method with annotated parameters could not be static", target);
            }
        }

        if (!methodAnnotationFound && !argAnnotationFound) {
            return;
        }

        logsValidator.validate(target, logs);
        logInsValidator.validate(target, logIns);
        logOutsValidator.validate(target, logOuts);
        logErrorsValidator.validate(target, logErrors);
        parameterLogs.forEach(log -> parameterLogsValidator.validate(target, log));

        mdcsValidator.validate(target, mdcs);
        parameterMdcs.forEach(item -> mdcsValidator.validate(target, item));
        Set<Mdc> mergedMdcs = new HashSet<>(mdcs);
        mergedMdcs.addAll(parameterMdcs.stream().flatMap(Collection::stream).collect(toSet()));
        mergedMdcsValidator.validate(target, mergedMdcs);
    }
}
