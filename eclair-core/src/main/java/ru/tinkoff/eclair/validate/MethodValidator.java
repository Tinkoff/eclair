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

import org.springframework.boot.logging.LogLevel;
import org.springframework.core.annotation.AnnotationUtils;
import ru.tinkoff.eclair.annotation.Log;
import ru.tinkoff.eclair.annotation.Mdc;
import ru.tinkoff.eclair.core.AnnotationAttribute;
import ru.tinkoff.eclair.core.AnnotationExtractor;
import ru.tinkoff.eclair.validate.log.group.*;
import ru.tinkoff.eclair.validate.mdc.group.MdcsValidator;
import ru.tinkoff.eclair.validate.mdc.group.MergedMdcsValidator;
import ru.tinkoff.eclair.validate.mdc.group.MethodMdcsValidator;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;

/**
 * @author Vyacheslav Klapatnyuk
 */
public class MethodValidator implements AnnotationUsageValidator<Method> {

    private final AnnotationExtractor annotationExtractor;

    private final LogsValidator logsValidator;
    private final LogInsValidator logInsValidator;
    private final LogOutsValidator logOutsValidator;
    private final LogErrorsValidator logErrorsValidator;
    private final ParameterLogsValidator parameterLogsValidator;

    private final MethodMdcsValidator methodMdcsValidator;
    private final MdcsValidator mdcsValidator;
    private final MergedMdcsValidator mergedMdcsValidator;

    public MethodValidator(AnnotationExtractor annotationExtractor,
                           LogsValidator logsValidator,
                           LogInsValidator logInsValidator,
                           LogOutsValidator logOutsValidator,
                           LogErrorsValidator logErrorsValidator,
                           ParameterLogsValidator parameterLogsValidator,
                           MethodMdcsValidator methodMdcsValidator,
                           MdcsValidator mdcsValidator,
                           MergedMdcsValidator mergedMdcsValidator) {
        this.annotationExtractor = annotationExtractor;
        this.logsValidator = logsValidator;
        this.logInsValidator = logInsValidator;
        this.logOutsValidator = logOutsValidator;
        this.logErrorsValidator = logErrorsValidator;
        this.parameterLogsValidator = parameterLogsValidator;
        this.methodMdcsValidator = methodMdcsValidator;
        this.mdcsValidator = mdcsValidator;
        this.mergedMdcsValidator = mergedMdcsValidator;
    }

    public void validate(Method method) throws AnnotationUsageException {
        validate(method, method);
    }

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
                throw new AnnotationUsageException(target,
                        "Annotation on the 'private' method cannot be processed",
                        "Change method access to 'default', 'protected' or 'public'");
            }
            if (argAnnotationFound) {
                throw new AnnotationUsageException(target,
                        "Annotation on the parameter of 'private' method cannot be processed",
                        "Change method access to 'default', 'protected' or 'public'");
            }
        }
        if (Modifier.isStatic(target.getModifiers())) {
            if (methodAnnotationFound) {
                throw new AnnotationUsageException(target,
                        "Annotation on the 'static' method cannot be processed",
                        "Remove 'static' modifier");
            }
            if (argAnnotationFound) {
                throw new AnnotationUsageException(target,
                        "Annotation on the parameter of 'static' method cannot be processed",
                        "Remove 'static' modifier");
            }
        }

        if (!methodAnnotationFound && !argAnnotationFound) {
            return;
        }
        validateLevelValue(target, Stream.of(logs, logIns, logOuts, logErrors)
                .flatMap(Collection::stream).collect(Collectors.toList()));
        validateLogInAndOutUsage(target, logs, logIns, logOuts);

        logsValidator.validate(target, logs);
        logInsValidator.validate(target, logIns);
        logOutsValidator.validate(target, logOuts);
        logErrorsValidator.validate(target, logErrors);
        parameterLogs.forEach(log -> parameterLogsValidator.validate(target, log));

        methodMdcsValidator.validate(target, mdcs);
        parameterMdcs.forEach(item -> mdcsValidator.validate(target, item));
        mergedMdcsValidator.validate(target, mergeMdcs(mdcs, parameterMdcs));
    }

    private void validateLevelValue(Method target, List<Annotation> annotations) {
        if (annotations.stream().anyMatch(annotation -> AnnotationAttribute.LEVEL.extract(annotation).equals(LogLevel.OFF))) {
            throw new AnnotationUsageException(target,
                    "Don't use level = LogLevel.OFF for methods. It's equivalent to no annotation",
                    "Remove unnecessary annotation");
        }
    }

    private Set<Mdc> mergeMdcs(Set<Mdc> mdcs, List<Set<Mdc>> parameterMdcs) {
        Set<Mdc> mergedMdcs = new HashSet<>(mdcs);
        mergedMdcs.addAll(parameterMdcs.stream().flatMap(Collection::stream).collect(toSet()));
        return mergedMdcs;
    }

    private void validateLogInAndOutUsage(Method target, Set<Log> logs, Set<Log.in> logIns, Set<Log.out> logOuts) {
        ArrayList<Annotation> annotations = new ArrayList<>();
        annotations.addAll(logIns);
        annotations.addAll(logOuts);
        annotations.addAll(logs);
        if (!logs.isEmpty() && (!logIns.isEmpty() || !logOuts.isEmpty())) {
            if (compareLogAttributes(annotations)) {
                throw new AnnotationUsageException(target,
                        "Don't use @Log, @Log.in and @Log.out together with the same config",
                        "Use only @Log to define the same behavior for beginning and ending logging");
            } else {
                throw new AnnotationUsageException(target,
                        "Don't use @Log with @Log.in or @Log.out",
                        "Use both @Log.in and @Log.out annotations to define different behavior" +
                                "for beginning and ending logging");
            }
        } else if (!logIns.isEmpty() && !logOuts.isEmpty() && compareLogAttributes(annotations)) {
            throw new AnnotationUsageException(target,
                    "Don't use @Log.in and @Log.out together with the same config",
                    "Use only @Log to define the same behavior for beginning and ending logging");
        }
    }

    private boolean compareLogAttributes(List<Annotation> logs) {
        Set<Annotation> set = new TreeSet<>((o1, o2) -> {
            Map<String, Object> first = AnnotationUtils.getAnnotationAttributes(o1);
            Map<String, Object> second = AnnotationUtils.getAnnotationAttributes(o2);
            if (first.equals(second)) {
                return 0;
            } else {
                return 1;
            }
        });
        set.addAll(logs);
        return set.size() == 1;
    }
}
