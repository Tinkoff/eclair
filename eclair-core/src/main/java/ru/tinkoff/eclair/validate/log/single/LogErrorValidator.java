package ru.tinkoff.eclair.validate.log.single;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import ru.tinkoff.eclair.annotation.Log;
import ru.tinkoff.eclair.core.ErrorFilterFactory;
import ru.tinkoff.eclair.definition.ErrorLog;

import java.util.Set;

import static java.lang.String.format;

/**
 * @author Viacheslav Klapatniuk
 */
@Component
public class LogErrorValidator extends MethodTargetLogAnnotationValidator {

    private final ErrorFilterFactory errorFilterFactory = ErrorFilterFactory.getInstance();

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz == Log.error.class;
    }

    @Override
    public void validate(Object target, Errors errors) {
        super.validate(target, errors);

        Log.error logError = (Log.error) target;

        Class<? extends Throwable>[] ofType = logError.ofType();
        Class<? extends Throwable>[] exclude = logError.exclude();
        ErrorLog.Filter filter = errorFilterFactory.buildErrorFilter(ofType, exclude);

        Set<Class<? extends Throwable>> includes = filter.getIncludes();
        if (includes.isEmpty()) {
            errors.reject("error.set.empty", "Empty error set defined by annotation: " + logError);
        } else {
            Set<Class<? extends Throwable>> excludes = filter.getExcludes();
            if (ofType.length > includes.size() || exclude.length > excludes.size()) {
                errors.reject("error.set.non.optimal",
                        format("Error set defined by annotation should be optimized: ofType=%s, exclude=%s", includes, excludes));
            }
        }
    }
}
