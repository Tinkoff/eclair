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
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import ru.tinkoff.eclair.annotation.Log;
import ru.tinkoff.eclair.annotation.Mdc;
import ru.tinkoff.eclair.core.AnnotationExtractor;
import ru.tinkoff.eclair.core.printer.PrinterResolver;
import ru.tinkoff.eclair.logger.EclairLogger;
import ru.tinkoff.eclair.validate.log.group.*;
import ru.tinkoff.eclair.validate.mdc.MdcsValidator;
import ru.tinkoff.eclair.validate.mdc.MergedMdcsValidator;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

import static java.lang.String.format;
import static java.util.stream.Collectors.toSet;

/**
 * @author Vyacheslav Klapatnyuk
 */
class BeanMethodValidator implements Validator {

    private final AnnotationExtractor annotationExtractor = AnnotationExtractor.getInstance();

    private final LogsValidator logsValidator;
    private final LogInsValidator logInsValidator;
    private final LogOutsValidator logOutsValidator;
    private final LogErrorsValidator logErrorsValidator;
    private final ParameterLogsValidator parameterLogsValidator;

    private final MdcsValidator mdcsValidator = new MdcsValidator();
    private final MergedMdcsValidator mergedMdcsValidator = new MergedMdcsValidator();

    BeanMethodValidator(GenericApplicationContext applicationContext,
                        Map<String, EclairLogger> loggers,
                        PrinterResolver printerResolver) {
        this.logsValidator = new LogsValidator(applicationContext, loggers, printerResolver);
        this.logInsValidator = new LogInsValidator(applicationContext, loggers, printerResolver);
        this.logOutsValidator = new LogOutsValidator(applicationContext, loggers, printerResolver);
        this.logErrorsValidator = new LogErrorsValidator(applicationContext, loggers, printerResolver);
        this.parameterLogsValidator = new ParameterLogsValidator(applicationContext, loggers, printerResolver);
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz == Method.class;
    }

    /**
     * TODO: Log + Log.in           -> Log.in + Log.out ?-> Log
     * TODO: Log + Log.out          -> Log.in + Log.out ?-> Log
     * TODO: Log + Log.in + Log.out -> Log.in + Log.out ?-> Log
     * TODO: Log.in + Log.out      ?-> Log
     */
    @Override
    public void validate(Object target, Errors errors) {
        Method method = (Method) target;

        Set<Log> logs = annotationExtractor.getLogs(method);
        boolean methodAnnotationFound = !logs.isEmpty();

        Set<Log.in> logIns = annotationExtractor.getLogIns(method);
        methodAnnotationFound |= !logIns.isEmpty();

        Set<Log.out> logOuts = annotationExtractor.getLogOuts(method);
        methodAnnotationFound |= !logOuts.isEmpty();

        Set<Log.error> logErrors = annotationExtractor.getLogErrors(method);
        methodAnnotationFound |= !logErrors.isEmpty();

        Set<Mdc> mdcs = annotationExtractor.getMdcs(method);
        methodAnnotationFound |= !mdcs.isEmpty();

        List<Set<Log>> parameterLogs = annotationExtractor.getParameterLogs(method);
        boolean argAnnotationFound = !parameterLogs.stream().allMatch(Set::isEmpty);

        List<Set<Mdc>> parameterMdcs = annotationExtractor.getParametersMdcs(method);
        argAnnotationFound |= !parameterMdcs.stream().allMatch(Set::isEmpty);

        if (Modifier.isPrivate(method.getModifiers())) {
            if (methodAnnotationFound) {
                errors.reject("method.private", format("Annotated method could not be private: %s", method));
            }
            if (argAnnotationFound) {
                errors.reject("method.args.private", format("Method with annotated parameters could not be private: %s", method));
            }
        }
        if (Modifier.isStatic(method.getModifiers())) {
            if (methodAnnotationFound) {
                errors.reject("method.static", format("Annotated method could not be static: %s", method));
            }
            if (argAnnotationFound) {
                errors.reject("method.args.static", format("Method with annotated parameters could not be static: %s", method));
            }
        }

        if (!methodAnnotationFound && !argAnnotationFound) {
            return;
        }

        logsValidator.validate(logs, errors);
        logInsValidator.validate(logIns, errors);
        logOutsValidator.validate(logOuts, errors);
        logErrorsValidator.validate(logErrors, errors);
        parameterLogs.forEach(log -> parameterLogsValidator.validate(log, errors));

        mdcsValidator.validate(mdcs, errors);
        parameterMdcs.forEach(item -> mdcsValidator.validate(item, errors));
        Set<Mdc> mergedMdcs = new HashSet<>(mdcs);
        mergedMdcs.addAll(parameterMdcs.stream().flatMap(Collection::stream).collect(toSet()));
        mergedMdcsValidator.validate(mergedMdcs, errors);
    }
}
