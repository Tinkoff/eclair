package ru.tinkoff.eclair.validate.log.group;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import ru.tinkoff.eclair.logger.Logger;
import ru.tinkoff.eclair.validate.log.single.LogArgValidator;

import java.util.Map;

/**
 * @author Viacheslav Klapatniuk
 */
@Component
public class LogArgsValidator extends LoggerSpecificLogAnnotationsValidator {

    private final LogArgValidator logArgValidator;

    @Autowired
    public LogArgsValidator(GenericApplicationContext applicationContext,
                            Map<String, Logger> loggers,
                            LogArgValidator logArgValidator) {
        super(applicationContext, loggers);
        this.logArgValidator = logArgValidator;
    }

    @Override
    public void validate(Object target, Errors errors) {
        super.validate(target, errors);
        ((Iterable<?>) target).forEach(logArg -> logArgValidator.validate(logArg, errors));
    }
}
