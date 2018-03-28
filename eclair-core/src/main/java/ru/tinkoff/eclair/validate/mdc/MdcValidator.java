package ru.tinkoff.eclair.validate.mdc;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import ru.tinkoff.eclair.annotation.Mdc;

/**
 * @author Viacheslav Klapatniuk
 */
@Component
class MdcValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz == Mdc.class;
    }

    /**
     * TODO: implement or remove
     */
    @Override
    public void validate(Object target, Errors errors) {
        // do nothing
    }
}
