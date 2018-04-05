package ru.tinkoff.eclair.annotation;

import java.lang.annotation.*;

/**
 * Container for annotate method/parameter by multiple repeatable {@link Mdc} annotations.
 *
 * @author Viacheslav Klapatniuk
 * @see Repeatable
 */
@Target({ElementType.METHOD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Mdcs {

    Mdc[] value();
}
