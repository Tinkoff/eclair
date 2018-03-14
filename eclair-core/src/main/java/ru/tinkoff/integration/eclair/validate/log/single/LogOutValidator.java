package ru.tinkoff.integration.eclair.validate.log.single;

import org.springframework.stereotype.Component;
import ru.tinkoff.integration.eclair.annotation.Log;

@Component
public class LogOutValidator extends MethodTargetLogAnnotationValidator {

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz == Log.out.class;
    }
}
