package ru.tinkoff.integration.eclair.core;

import java.lang.annotation.Annotation;

import static org.springframework.core.annotation.AnnotationUtils.getValue;

/**
 * @author Viacheslav Klapatniuk
 */
public enum AnnotationAttribute {
    LEVEL("level"),
    IF_ENABLED("ifEnabled"),
    LOGGER("logger"),
    PRINTER("printer");

    private final String name;

    AnnotationAttribute(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @SuppressWarnings("unchecked")
    public <T> T extract(Annotation annotation) {
        return (T) getValue(annotation, name);
    }
}
