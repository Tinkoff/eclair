package ru.tinkoff.eclair.validate.log.group;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import ru.tinkoff.eclair.logger.EclairLogger;
import ru.tinkoff.eclair.validate.log.single.ParameterLogValidator;

import java.util.Map;

/**
 * @author Viacheslav Klapatniuk
 */
@Component
public class ParameterLogsValidator extends LoggerSpecificLogAnnotationsValidator {

    private final ParameterLogValidator parameterLogValidator;

    @Autowired
    public ParameterLogsValidator(GenericApplicationContext applicationContext,
                                  Map<String, EclairLogger> loggers,
                                  ParameterLogValidator parameterLogValidator) {
        super(applicationContext, loggers);
        this.parameterLogValidator = parameterLogValidator;
    }

    @Override
    public void validate(Object target, Errors errors) {
        super.validate(target, errors);
        ((Iterable<?>) target).forEach(log -> parameterLogValidator.validate(log, errors));
    }
}
