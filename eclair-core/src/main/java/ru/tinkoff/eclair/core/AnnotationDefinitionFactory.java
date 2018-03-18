package ru.tinkoff.eclair.core;

import ru.tinkoff.eclair.annotation.Log;
import ru.tinkoff.eclair.annotation.Mdc;
import ru.tinkoff.eclair.definition.*;
import ru.tinkoff.eclair.printer.Printer;

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

    public InLog buildInLog(Set<String> loggerNames, Method method) {
        Log.in logIn = annotationExtractor.findLogIn(method, loggerNames);
        List<Log.arg> logArgs = annotationExtractor.findLogArgs(method, loggerNames);
        if (nonNull(logIn)) {
            return InLog.newInstance(logIn, buildArgLogs(logArgs, method, logIn));
        }
        Log log = annotationExtractor.findLog(method, loggerNames);
        if (nonNull(log)) {
            logIn = annotationExtractor.synthesizeLogIn(log);
            return InLog.newInstance(logIn, buildArgLogs(logArgs, method, logIn));
        }
        if (!logArgs.isEmpty() && logArgs.stream().anyMatch(Objects::nonNull)) {
            return InLog.newInstance(DEFAULT_LOG_IN, buildArgLogs(logArgs, method, null));
        }
        return null;
    }

    private List<ArgLog> buildArgLogs(List<Log.arg> logArgs, Method method, Log.in logIn) {
        Iterator<Log.arg> logArgIterator = logArgs.iterator();
        return Stream.of(method.getParameterTypes())
                .map(clazz -> buildArgLog(logArgIterator.next(), clazz, logIn))
                .collect(toList());
    }

    private ArgLog buildArgLog(Log.arg logArg, Class<?> parameterType, Log.in logIn) {
        if (isNull(logArg)) {
            if (isNull(logIn)) {
                return null;
            }
            logArg = annotationExtractor.synthesizeLogArg(logIn);
        }
        Printer printer = printerResolver.resolve(logArg.printer(), parameterType);
        return new ArgLog(logArg, printer);
    }

    public OutLog buildOutLog(Set<String> loggerNames, Method method) {
        Log.out logOut = annotationExtractor.findLogOut(method, loggerNames);
        if (nonNull(logOut)) {
            Printer printer = printerResolver.resolve(logOut.printer(), method.getReturnType());
            return new OutLog(logOut, printer);
        }
        Log log = annotationExtractor.findLog(method, loggerNames);
        if (nonNull(log)) {
            Log.out syntheticLogOut = annotationExtractor.synthesizeLogOut(log);
            Printer printer = printerResolver.resolve(syntheticLogOut.printer(), method.getReturnType());
            return new OutLog(syntheticLogOut, printer);
        }
        return null;
    }

    public Set<ErrorLog> buildErrorLogs(Set<String> loggerNames, Method method) {
        return annotationExtractor.findLogErrors(method, loggerNames).stream()
                .map(ErrorLog::new)
                .collect(toCollection(LinkedHashSet::new));
    }

    public MdcPack buildMdcPack(Method method) {
        Set<Mdc> methodMdcs = annotationExtractor.getMdcs(method);
        List<Set<Mdc>> parametersMdcs = annotationExtractor.getParametersMdcs(method);
        return MdcPack.newInstance(method, methodMdcs, parametersMdcs);
    }
}
