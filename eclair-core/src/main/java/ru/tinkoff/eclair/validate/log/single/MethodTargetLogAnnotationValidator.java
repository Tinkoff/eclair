package ru.tinkoff.eclair.validate.log.single;

import org.springframework.boot.logging.LogLevel;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import ru.tinkoff.eclair.core.AnnotationAttribute;

import java.lang.annotation.Annotation;

import static java.lang.String.format;
import static org.springframework.boot.logging.LogLevel.OFF;

/**
 * TODO: verbose, printer for empty parameter array (or (v|V)oid return type)
 *
 * @author Viacheslav Klapatniuk
 */
abstract class MethodTargetLogAnnotationValidator implements Validator {

    @Override
    public void validate(Object target, Errors errors) {
        Annotation annotation = (Annotation) target;

        LogLevel expectedLevel = AnnotationAttribute.LEVEL.extract(annotation);
        LogLevel ifEnabledLevel = AnnotationAttribute.IF_ENABLED.extract(annotation);
        if (ifEnabledLevel.ordinal() >= expectedLevel.ordinal() && ifEnabledLevel != OFF) {
            errors.reject("ifEnabled",
                    format("'If enabled' level is higher or equals to expected level: %s", annotation));
        }
    }
}
