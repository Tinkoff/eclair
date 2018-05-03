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

import ru.tinkoff.eclair.annotation.Log;
import ru.tinkoff.eclair.definition.*;
import ru.tinkoff.eclair.definition.factory.*;
import ru.tinkoff.eclair.printer.Printer;
import ru.tinkoff.eclair.printer.resolver.PrinterResolver;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.Collections.unmodifiableSet;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.*;

/**
 * @author Vyacheslav Klapatnyuk
 */
public final class AnnotationDefinitionFactory {

    private final AnnotationExtractor annotationExtractor = AnnotationExtractor.getInstance();
    private final PrinterResolver printerResolver;

    public AnnotationDefinitionFactory(PrinterResolver printerResolver) {
        this.printerResolver = printerResolver;
    }

    public InLog buildInLog(Set<String> loggerNames, Method method) {
        Log.in logIn = annotationExtractor.findLogIn(method, loggerNames);
        if (nonNull(logIn)) {
            return buildInLog(method, logIn);
        }
        Log log = annotationExtractor.findLog(method, loggerNames);
        if (nonNull(log)) {
            return buildInLog(method, annotationExtractor.synthesizeLogIn(log));
        }
        return null;
    }

    private InLog buildInLog(Method method, Log.in logIn) {
        List<Printer> printers = printerResolver.resolveOrDefault(logIn.printer(), method.getParameterTypes());
        return InLogFactory.newInstance(logIn, printers);
    }

    public List<ParameterLog> buildParameterLogs(Set<String> loggerNames, Method method) {
        List<Log> logs = annotationExtractor.findParameterLogs(method, loggerNames);
        Iterator<Log> logIterator = logs.iterator();
        return Stream.of(method.getParameterTypes())
                .map(clazz -> buildParameterLog(logIterator.next(), clazz))
                .collect(toList());
    }

    private ParameterLog buildParameterLog(Log log, Class<?> parameterType) {
        if (isNull(log)) {
            return null;
        }
        Printer printer = printerResolver.resolveOrDefault(log.printer(), parameterType);
        return ParameterLogFactory.newInstance(log, printer);
    }

    public OutLog buildOutLog(Set<String> loggerNames, Method method) {
        Log.out logOut = annotationExtractor.findLogOut(method, loggerNames);
        if (nonNull(logOut)) {
            return buildOutLog(method, logOut);
        }
        Log log = annotationExtractor.findLog(method, loggerNames);
        if (nonNull(log)) {
            return buildOutLog(method, annotationExtractor.synthesizeLogOut(log));
        }
        return null;
    }

    private OutLog buildOutLog(Method method, Log.out logOut) {
        Printer printer = printerResolver.resolveOrDefault(logOut.printer(), method.getReturnType());
        return OutLogFactory.newInstance(logOut, printer);
    }

    public Set<ErrorLog> buildErrorLogs(Set<String> loggerNames, Method method) {
        return annotationExtractor.findLogErrors(method, loggerNames).stream()
                .map(ErrorLogFactory::newInstance)
                .collect(toCollection(LinkedHashSet::new));
    }

    public Set<ParameterMdc> buildMethodParameterMdcs(Method method) {
        return annotationExtractor.getMdcs(method).stream()
                .map(ParameterMdcFactory::newInstance)
                .collect(toSet());
    }

    public List<Set<ParameterMdc>> buildParameterMdcs(Method method) {
        return annotationExtractor.getParametersMdcs(method).stream()
                .map(mdcs -> unmodifiableSet(mdcs.stream().map(ParameterMdcFactory::newInstance).collect(toSet())))
                .collect(toList());
    }
}
