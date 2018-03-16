package ru.tinkoff.eclair.validate.mdc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import ru.tinkoff.eclair.annotation.Mdc;

import java.util.Collection;

import static java.lang.String.format;
import static java.util.stream.Collectors.groupingBy;

/**
 * @author Viacheslav Klapatniuk
 */
@Component
public class MdcsValidator implements Validator {

    private final MdcValidator mdcValidator;

    @Autowired
    public MdcsValidator(MdcValidator mdcValidator) {
        this.mdcValidator = mdcValidator;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return Collection.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        @SuppressWarnings("unchecked")
        Collection<Mdc> mdcs = (Collection<Mdc>) target;

        mdcs.stream().collect(groupingBy(Mdc::key))
                .entrySet().stream()
                .filter(entry -> entry.getValue().size() > 1)
                .forEach(entry -> errors.reject("key.duplicate",
                        format("Annotations duplicated for key '%s': %s", entry.getKey(), entry.getValue())));

        mdcs.forEach(mdc -> mdcValidator.validate(mdc, errors));
    }
}
