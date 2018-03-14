package ru.tinkoff.integration.eclair.validate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import ru.tinkoff.integration.eclair.annotation.Log;
import ru.tinkoff.integration.eclair.annotation.Mdc;
import ru.tinkoff.integration.eclair.core.AnnotationExtractor;
import ru.tinkoff.integration.eclair.validate.log.group.*;
import ru.tinkoff.integration.eclair.validate.mdc.MdcsValidator;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.lang.String.format;
import static java.util.stream.Collectors.toSet;

@Component
class BeanMethodValidator implements Validator {

    private final LogsValidator logsValidator;
    private final LogInsValidator logInsValidator;
    private final LogOutsValidator logOutsValidator;
    private final LogErrorsValidator logErrorsValidator;
    private final LogArgsValidator logArgsValidator;
    private final MdcsValidator mdcsValidator;

    private final AnnotationExtractor annotationExtractor = AnnotationExtractor.getInstance();

    @Autowired
    BeanMethodValidator(LogsValidator logsValidator,
                        LogInsValidator logInsValidator,
                        LogOutsValidator logOutsValidator,
                        LogErrorsValidator logErrorsValidator,
                        LogArgsValidator logArgsValidator,
                        MdcsValidator mdcsValidator) {
        this.logsValidator = logsValidator;
        this.logInsValidator = logInsValidator;
        this.logOutsValidator = logOutsValidator;
        this.logErrorsValidator = logErrorsValidator;
        this.logArgsValidator = logArgsValidator;
        this.mdcsValidator = mdcsValidator;
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

        List<Set<Log.arg>> parameterLogArgs = annotationExtractor.getLogArgs(method);
        boolean argAnnotationFound = !parameterLogArgs.stream().allMatch(Set::isEmpty);

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
        parameterLogArgs.forEach(logArgs -> logArgsValidator.validate(logArgs, errors));

        Set<Mdc> mergedMdcs = new HashSet<>(mdcs);
        mergedMdcs.addAll(parameterMdcs.stream().flatMap(Collection::stream).collect(toSet()));
        mdcsValidator.validate(mergedMdcs, errors);
    }
}
