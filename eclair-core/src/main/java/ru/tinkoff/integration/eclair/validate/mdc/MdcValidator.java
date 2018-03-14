package ru.tinkoff.integration.eclair.validate.mdc;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import ru.tinkoff.integration.eclair.annotation.Mdc;

@Component
public class MdcValidator implements Validator {

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
