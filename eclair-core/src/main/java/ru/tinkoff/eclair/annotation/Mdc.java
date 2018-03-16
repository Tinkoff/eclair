package ru.tinkoff.eclair.annotation;

import java.lang.annotation.*;

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

    boolean global() default false;
}
