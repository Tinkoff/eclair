package ru.tinkoff.eclair.core;

import java.lang.annotation.Annotation;

import static org.springframework.core.annotation.AnnotationUtils.getValue;

/**
 * @author Viacheslav Klapatniuk
 */
public enum AnnotationAttribute {
    LEVEL("level"),
    IF_ENABLED("ifEnabled"),
    LOGGER("logger");

    private final String name;

    AnnotationAttribute(String name) {
        this.name = name;
    }

    @SuppressWarnings("unchecked")
    public <T> T extract(Annotation annotation) {
        return (T) getValue(annotation, name);
    }
}
