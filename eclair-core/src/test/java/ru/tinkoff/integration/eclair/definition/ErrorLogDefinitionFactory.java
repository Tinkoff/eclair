package ru.tinkoff.integration.eclair.definition;

import ru.tinkoff.integration.eclair.annotation.Log;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.core.annotation.AnnotationUtils.synthesizeAnnotation;

/**
 * @author Viacheslav Klapatniuk
 */
public class ErrorLogDefinitionFactory {

    public static ErrorLogDefinition newInstance(Class<?>[] includes, Class<?>[] excludes) {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("ofType", includes);
        attributes.put("exclude", excludes);
        Log.error logError = synthesizeAnnotation(attributes, Log.error.class, null);
        return new ErrorLogDefinition(logError);
    }
}
