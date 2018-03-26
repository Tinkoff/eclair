package ru.tinkoff.eclair.validate.log.single;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import ru.tinkoff.eclair.annotation.Log;

/**
 * @author Viacheslav Klapatniuk
 */
@Component
public class LogArgValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz == Log.class;
    }

    /**
     * TODO: implement or remove
     */
    @Override
    public void validate(Object target, Errors errors) {
        // do nothing
    }
}
