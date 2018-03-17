package ru.tinkoff.eclair.core;

import ru.tinkoff.eclair.annotation.Log;
import ru.tinkoff.eclair.annotation.Mdc;
import ru.tinkoff.eclair.definition.*;
import ru.tinkoff.eclair.format.printer.Printer;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Stream;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toList;
import static org.springframework.core.annotation.AnnotationUtils.synthesizeAnnotation;

/**
 * @author Viacheslav Klapatniuk
 */
public final class AnnotationDefinitionFactory {

    private static final Log.in DEFAULT_LOG_IN = synthesizeAnnotation(Log.in.class);

    private final AnnotationExtractor annotationExtractor = AnnotationExtractor.getInstance();
    private final PrinterResolver printerResolver;

    public AnnotationDefinitionFactory(PrinterResolver printerResolver) {
        this.printerResolver = printerResolver;
    }

    public InLogDefinition buildInLogDefinition(Set<String> loggerNames, Method method) {
        Log.in logIn = annotationExtractor.findLogIn(method, loggerNames);
        List<Log.arg> logArgs = annotationExtractor.findLogArgs(method, loggerNames);
        if (nonNull(logIn)) {
            return InLogDefinition.newInstance(logIn, buildArgLogDefinitions(logArgs, method, logIn));
        }
        Log log = annotationExtractor.findLog(method, loggerNames);
        if (nonNull(log)) {
            logIn = annotationExtractor.synthesizeLogIn(log);
            return InLogDefinition.newInstance(logIn, buildArgLogDefinitions(logArgs, method, logIn));
        }
        if (logArgs.isEmpty() || logArgs.stream().noneMatch(Objects::nonNull)) {
            return null;
        }
        return InLogDefinition.newInstance(DEFAULT_LOG_IN, buildArgLogDefinitions(logArgs, method, null));
    }

    private List<ArgLogDefinition> buildArgLogDefinitions(List<Log.arg> logArgs, Method method, Log.in logIn) {
        Iterator<Log.arg> logArgIterator = logArgs.iterator();
        return Stream.of(method.getParameterTypes())
                .map(clazz -> buildArgLogDefinition(logArgIterator.next(), clazz, logIn))
                .collect(toList());
    }

    private ArgLogDefinition buildArgLogDefinition(Log.arg logArg, Class<?> parameterType, Log.in logIn) {
        if (isNull(logArg)) {
            if (isNull(logIn)) {
                return null;
            }
            logArg = annotationExtractor.synthesizeLogArg(logIn);
        }
        Printer printer = printerResolver.resolve(logArg.printer(), parameterType);
        return new ArgLogDefinition(logArg, printer);
    }

    public OutLogDefinition buildOutLogDefinition(Set<String> loggerNames, Method method) {
        Log.out logOut = annotationExtractor.findLogOut(method, loggerNames);
        if (nonNull(logOut)) {
            Printer printer = printerResolver.resolve(logOut.printer(), method.getReturnType());
            return new OutLogDefinition(logOut, printer);
        }
        Log log = annotationExtractor.findLog(method, loggerNames);
        if (nonNull(log)) {
            Log.out syntheticLogOut = annotationExtractor.synthesizeLogOut(log);
            return new OutLogDefinition(syntheticLogOut, printerResolver.getDefaultPrinter());
        }
        return null;
    }

    public Set<ErrorLogDefinition> buildErrorLogDefinitions(Set<String> loggerNames, Method method) {
        return annotationExtractor.findLogErrors(method, loggerNames).stream()
                .map(ErrorLogDefinition::new)
                .collect(toCollection(LinkedHashSet::new));
    }

    public MdcPackDefinition buildMdcPackDefinition(Method method) {
        Set<Mdc> methodMdcAnnotations = annotationExtractor.getMdcs(method);
        List<Set<Mdc>> argumentsMdcAnnotations = annotationExtractor.getParametersMdcs(method);
        return MdcPackDefinition.newInstance(method, methodMdcAnnotations, argumentsMdcAnnotations);
    }
}
