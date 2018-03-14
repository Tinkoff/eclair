package ru.tinkoff.integration.eclair.validate.log.single;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import ru.tinkoff.integration.eclair.annotation.Log;

@Component
public class LogArgValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz == Log.arg.class;
    }

    /**
     * TODO: implement or remove
     */
    @Override
    public void validate(Object target, Errors errors) {
        // do nothing
    }
}
