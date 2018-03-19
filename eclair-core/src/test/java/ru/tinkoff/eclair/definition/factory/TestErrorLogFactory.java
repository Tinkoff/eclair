package ru.tinkoff.eclair.definition.factory;

import ru.tinkoff.eclair.annotation.Log;
import ru.tinkoff.eclair.definition.ErrorLog;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.core.annotation.AnnotationUtils.synthesizeAnnotation;

/**
 * @author Viacheslav Klapatniuk
 */
public class TestErrorLogFactory {

    public static ErrorLog newInstance(Class<?>[] includes, Class<?>[] excludes) {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("ofType", includes);
        attributes.put("exclude", excludes);
        Log.error logError = synthesizeAnnotation(attributes, Log.error.class, null);
        return ErrorLogFactory.newInstance(logError);
    }
}
