package ru.tinkoff.eclair.validate.log.single;

import org.springframework.stereotype.Component;
import ru.tinkoff.eclair.annotation.Log;

/**
 * @author Viacheslav Klapatniuk
 */
@Component
public class LogInValidator extends MethodTargetLogAnnotationValidator {

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz == Log.in.class;
    }
}
