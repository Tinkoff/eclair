package ru.tinkoff.eclair.annotation;

import org.springframework.boot.logging.LogLevel;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

import static org.springframework.boot.logging.LogLevel.*;

/**
 * TODO: use meta-annotation for @Log declaration
 *
 * @author Viacheslav Klapatniuk
 */
@Repeatable(Logs.class)
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Log {

    @AliasFor("level")
    LogLevel value() default DEBUG;

    @AliasFor("value")
    LogLevel level() default DEBUG;

    LogLevel ifEnabled() default OFF;

    LogLevel verbose() default DEBUG;

    String printer() default "";

    /**
     * TODO: convert to logger array everywhere?
     */
    String logger() default "";

    @Repeatable(ins.class)
    @Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @interface in {

        @AliasFor("level")
        LogLevel value() default DEBUG;

        @AliasFor("value")
        LogLevel level() default DEBUG;

        LogLevel ifEnabled() default OFF;

        LogLevel verbose() default DEBUG;

        String printer() default "";

        String logger() default "";
    }

    @Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @interface ins {

        @SuppressWarnings("unused")
        in[] value();
    }

    @Repeatable(outs.class)
    @Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @interface out {

        @AliasFor("level")
        LogLevel value() default DEBUG;

        @AliasFor("value")
        LogLevel level() default DEBUG;

        LogLevel ifEnabled() default OFF;

        LogLevel verbose() default DEBUG;

        String printer() default "";

        String logger() default "";
    }

    @Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @interface outs {

        @SuppressWarnings("unused")
        out[] value();
    }

    @Repeatable(errors.class)
    @Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @interface error {

        @AliasFor("level")
        LogLevel value() default WARN;

        @AliasFor("value")
        LogLevel level() default WARN;

        LogLevel ifEnabled() default OFF;

        /**
         * TODO: add example for 'verbose' attribute
         */
        LogLevel verbose() default WARN;

        Class<? extends Throwable>[] ofType() default Throwable.class;

        Class<? extends Throwable>[] exclude() default {};

        String logger() default "";
    }

    @Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @interface errors {

        @SuppressWarnings("unused")
        error[] value();
    }

    @Repeatable(args.class)
    @Target({ElementType.PARAMETER, ElementType.ANNOTATION_TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @interface arg {

        @AliasFor("ifEnabled")
        LogLevel value() default DEBUG;

        @AliasFor("value")
        LogLevel ifEnabled() default DEBUG;

        String printer() default "";

        String logger() default "";
    }

    @Target({ElementType.PARAMETER, ElementType.ANNOTATION_TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @interface args {

        @SuppressWarnings("unused")
        arg[] value();
    }
}
