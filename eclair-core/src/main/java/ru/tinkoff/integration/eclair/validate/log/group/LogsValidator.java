package ru.tinkoff.integration.eclair.validate.log.group;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import ru.tinkoff.integration.eclair.logger.Logger;
import ru.tinkoff.integration.eclair.validate.log.single.LogValidator;

import java.util.Map;

@Component
public class LogsValidator extends LoggerSpecificLogAnnotationsValidator {

    private final LogValidator logValidator;

    @Autowired
    public LogsValidator(GenericApplicationContext applicationContext,
                         Map<String, Logger> loggers,
                         LogValidator logValidator) {
        super(applicationContext, loggers);
        this.logValidator = logValidator;
    }

    @Override
    public void validate(Object target, Errors errors) {
        super.validate(target, errors);
        ((Iterable<?>) target).forEach(log -> logValidator.validate(log, errors));
    }
}
