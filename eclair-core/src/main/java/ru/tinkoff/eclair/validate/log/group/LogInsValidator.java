package ru.tinkoff.eclair.validate.log.group;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import ru.tinkoff.eclair.logger.Logger;
import ru.tinkoff.eclair.validate.log.single.LogInValidator;

import java.util.Map;

/**
 * @author Viacheslav Klapatniuk
 */
@Component
public class LogInsValidator extends LoggerSpecificLogAnnotationsValidator {

    private final LogInValidator logInValidator;

    @Autowired
    public LogInsValidator(GenericApplicationContext applicationContext,
                           Map<String, Logger> loggers,
                           LogInValidator logInValidator) {
        super(applicationContext, loggers);
        this.logInValidator = logInValidator;
    }

    @Override
    public void validate(Object target, Errors errors) {
        super.validate(target, errors);
        ((Iterable<?>) target).forEach(logIn -> logInValidator.validate(logIn, errors));
    }
}
