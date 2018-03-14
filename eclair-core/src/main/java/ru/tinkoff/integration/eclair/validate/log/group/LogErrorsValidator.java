package ru.tinkoff.integration.eclair.validate.log.group;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import ru.tinkoff.integration.eclair.annotation.Log;
import ru.tinkoff.integration.eclair.core.ErrorFilterFactory;
import ru.tinkoff.integration.eclair.definition.ErrorFilter;
import ru.tinkoff.integration.eclair.logger.Logger;
import ru.tinkoff.integration.eclair.validate.log.single.LogErrorValidator;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.lang.String.format;

/**
 * @author Viacheslav Klapatniuk
 */
@Component
public class LogErrorsValidator extends LoggerSpecificLogAnnotationsValidator {

    private final ErrorFilterFactory errorFilterFactory = ErrorFilterFactory.getInstance();
    private final LogErrorValidator logErrorValidator;

    @Autowired
    public LogErrorsValidator(GenericApplicationContext applicationContext,
                              Map<String, Logger> loggers,
                              LogErrorValidator logErrorValidator) {
        super(applicationContext, loggers);
        this.logErrorValidator = logErrorValidator;
    }

    @Override
    public void validate(Object target, Errors errors) {
        @SuppressWarnings("unchecked")
        Set<Log.error> logErrors = (Set<Log.error>) target;

        groupAnnotationsByLogger(logErrors, errors).entrySet().stream()
                .filter(entry -> entry.getValue().size() > 1)
                .forEach(entry -> {
                    List<Log.error> loggerLogErrors = entry.getValue();
                    Set<ErrorFilter> errorFilters = loggerLogErrors.stream()
                            .map(error -> errorFilterFactory.buildErrorFilter(error.ofType(), error.exclude()))
                            .collect(Collectors.toSet());
                    if (loggerLogErrors.size() > errorFilters.size()) {
                        errors.reject("errors.logger.duplicate",
                                format("Error filters duplicated for logger bean '%s': %s", entry.getKey(), loggerLogErrors));
                    }
                });

        logErrors.forEach(logError -> logErrorValidator.validate(logError, errors));
    }
}
