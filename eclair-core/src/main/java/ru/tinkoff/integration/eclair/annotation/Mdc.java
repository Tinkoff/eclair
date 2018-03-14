package ru.tinkoff.integration.eclair.annotation;

import java.lang.annotation.*;

import static ru.tinkoff.integration.eclair.annotation.Scope.*;

/**
 * @author Viacheslav Klapatniuk
 */
@Repeatable(Mdcs.class)
@Target({ElementType.PARAMETER, ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Mdc {

    String key();

    /**
     * TODO: add method arguments to context root
     * SpEL literal
     */
    String value();

    Scope scope() default METHOD;
}
