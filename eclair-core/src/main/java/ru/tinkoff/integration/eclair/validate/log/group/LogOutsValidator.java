package ru.tinkoff.integration.eclair.validate.log.group;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import ru.tinkoff.integration.eclair.logger.Logger;
import ru.tinkoff.integration.eclair.validate.log.single.LogOutValidator;

import java.util.Map;

@Component
public class LogOutsValidator extends LoggerSpecificLogAnnotationsValidator {

    private final LogOutValidator logOutValidator;

    @Autowired
    public LogOutsValidator(GenericApplicationContext applicationContext,
                            Map<String, Logger> loggers,
                            LogOutValidator logOutValidator) {
        super(applicationContext, loggers);
        this.logOutValidator = logOutValidator;
    }

    @Override
    public void validate(Object target, Errors errors) {
        super.validate(target, errors);
        ((Iterable<?>) target).forEach(logOut -> logOutValidator.validate(logOut, errors));
    }
}
