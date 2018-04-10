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

import org.springframework.util.ReflectionUtils;
import ru.tinkoff.eclair.annotation.Log;
import ru.tinkoff.eclair.annotation.Mdc;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.*;
import static org.springframework.core.annotation.AnnotatedElementUtils.findMergedRepeatableAnnotations;
import static org.springframework.core.annotation.AnnotationUtils.getAnnotationAttributes;
import static org.springframework.core.annotation.AnnotationUtils.synthesizeAnnotation;
import static ru.tinkoff.eclair.core.AnnotationAttribute.LOGGER;

/**
 * @author Vyacheslav Klapatnyuk
 */
public final class AnnotationExtractor {

    /**
     * In order of supposed popularity
     */
    private static final List<Class<? extends Annotation>> METHOD_TARGET_ANNOTATION_CLASSES = asList(
            Log.class,
            Log.in.class,
            Log.out.class,
            Log.error.class,
            Mdc.class
    );

    /**
     * In order of supposed popularity
     */
    private static final List<Class<? extends Annotation>> PARAMETER_TARGET_ANNOTATION_CLASSES = asList(
            Log.class,
            Mdc.class
    );

    private static final ReversedBridgeMethodResolver bridgeMethodResolver = ReversedBridgeMethodResolver.getInstance();
    private static final AnnotationExtractor instance = new AnnotationExtractor();

    private AnnotationExtractor() {
    }

    public static AnnotationExtractor getInstance() {
        return instance;
    }

    public Set<Method> getCandidateMethods(Class<?> clazz) {
        return Stream.of(ReflectionUtils.getUniqueDeclaredMethods(clazz))
                .filter(method -> method.getDeclaringClass() != Object.class)
                .filter(method -> !(method.isBridge() || method.isSynthetic()))
                .collect(toSet());
    }

    public boolean hasAnyAnnotation(Method method) {
        return METHOD_TARGET_ANNOTATION_CLASSES.stream()
                .anyMatch(annotationClass -> !findMergedRepeatableAnnotations(method, annotationClass).isEmpty());
    }

    public boolean hasAnyAnnotation(Parameter parameter) {
        return PARAMETER_TARGET_ANNOTATION_CLASSES.stream()
                .anyMatch(annotationClass -> !findMergedRepeatableAnnotations(parameter, annotationClass).isEmpty());
    }

    public Set<Log> getLogs(Method method) {
        return findAnnotationOnMethodOrBridge(method, Log.class);
    }

    public Set<Log.in> getLogIns(Method method) {
        return findAnnotationOnMethodOrBridge(method, Log.in.class);
    }

    public Set<Log.out> getLogOuts(Method method) {
        return findAnnotationOnMethodOrBridge(method, Log.out.class);
    }

    public Set<Log.error> getLogErrors(Method method) {
        return findAnnotationOnMethodOrBridge(method, Log.error.class);
    }

    public Set<Mdc> getMdcs(Method method) {
        return findAnnotationOnMethodOrBridge(method, Mdc.class);
    }

    private <T extends Annotation> Set<T> findAnnotationOnMethodOrBridge(Method method, Class<T> annotationClass) {
        Set<T> logs = findAnnotationOnMethod(method, annotationClass);
        if (logs.isEmpty()) {
            Method bridgeMethod = bridgeMethodResolver.findBridgeMethod(method);
            if (nonNull(bridgeMethod)) {
                return findAnnotationOnMethod(bridgeMethod, annotationClass);
            }
        }
        return logs;
    }

    private <T extends Annotation> Set<T> findAnnotationOnMethod(Method method, Class<T> annotationClass) {
        return findMergedRepeatableAnnotations(method, annotationClass);
    }

    public List<Set<Log>> getParameterLogs(Method method) {
        return Stream.of(method.getParameters())
                .map(parameter -> findAnnotationOnParameter(parameter, Log.class))
                .collect(toList());
    }

    public List<Set<Mdc>> getParametersMdcs(Method method) {
        return Stream.of(method.getParameters())
                .map(parameter -> findAnnotationOnParameter(parameter, Mdc.class))
                .collect(toList());
    }

    private <T extends Annotation> Set<T> findAnnotationOnParameter(Parameter parameter, Class<T> annotationClass) {
        return findMergedRepeatableAnnotations(parameter, annotationClass);
    }

    Log findLog(Method method, Set<String> loggers) {
        return filterAndFindFirstAnnotation(getLogs(method), loggers);
    }

    Log.in findLogIn(Method method, Set<String> loggers) {
        return filterAndFindFirstAnnotation(getLogIns(method), loggers);
    }

    Log.out findLogOut(Method method, Set<String> loggers) {
        return filterAndFindFirstAnnotation(getLogOuts(method), loggers);
    }

    Set<Log.error> findLogErrors(Method method, Set<String> loggers) {
        return filterAnnotations(getLogErrors(method), loggers);
    }

    List<Log> findParameterLogs(Method method, Set<String> loggers) {
        return getParameterLogs(method).stream()
                .map(logs -> filterAndFindFirstAnnotation(logs, loggers))
                .collect(toList());
    }

    private <T extends Annotation> T filterAndFindFirstAnnotation(Collection<T> annotations, Set<?> loggers) {
        return annotations.stream()
                .filter(getLoggerPredicate(loggers))
                .findFirst()
                .orElse(null);
    }

    private <T extends Annotation> Set<T> filterAnnotations(Collection<T> annotations, Set<?> loggers) {
        return annotations.stream()
                .filter(getLoggerPredicate(loggers))
                .collect(toCollection(LinkedHashSet::new));
    }

    private <T extends Annotation> Predicate<T> getLoggerPredicate(Set<?> loggers) {
        return annotation -> loggers.contains(LOGGER.extract(annotation));
    }

    Log.in synthesizeLogIn(Log log) {
        return synthesizeAnnotation(getAnnotationAttributes(log), Log.in.class, null);
    }

    Log.out synthesizeLogOut(Log log) {
        return synthesizeAnnotation(getAnnotationAttributes(log), Log.out.class, null);
    }
}
