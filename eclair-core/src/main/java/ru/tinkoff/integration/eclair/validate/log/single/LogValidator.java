package ru.tinkoff.integration.eclair.validate.log.single;

import org.springframework.stereotype.Component;
import ru.tinkoff.integration.eclair.annotation.Log;

@Component
public class LogValidator extends MethodTargetLogAnnotationValidator {

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz == Log.class;
    }
}
