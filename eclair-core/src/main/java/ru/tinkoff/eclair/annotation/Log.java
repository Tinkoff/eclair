/*
 * Copyright 2018 Tinkoff Bank
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ru.tinkoff.eclair.annotation;

import org.springframework.boot.logging.LogLevel;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.AliasFor;
import org.springframework.core.annotation.Order;
import ru.tinkoff.eclair.core.printer.PrinterResolver;
import ru.tinkoff.eclair.logger.EclairLogger;
import ru.tinkoff.eclair.printer.Printer;

import java.lang.annotation.*;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import static org.springframework.boot.logging.LogLevel.*;

/**
 * Annotated {@link Method} is able to log beginning and ending (except the emergency ending) of execution.
 * Works the same as both {@link in} and {@link out} annotations with all matching attribute values.
 * Note: emergency ending of the method execution should be specified separately by {@link error} annotation.
 *
 * Can be defined on {@link Parameter} and specify logging settings for it.
 *
 * Should have unique {@link #logger} value per annotated element.
 * {@link Parameter}-level annotation has higher priority settings than {@link Method}-level with same {@link #logger} value.
 *
 * @author Vyacheslav Klapatnyuk
 */
@Repeatable(Logs.class)
// TODO: implement {@link ElementType#TYPE}
@Target({ElementType.METHOD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Log {

    /**
     * Alias for {@link #level}.
     */
    @AliasFor("level")
    LogLevel value() default DEBUG;

    /**
     * Expected level to log beginning and ending of method execution.
     */
    @AliasFor("value")
    LogLevel level() default DEBUG;

    /**
     * Enables logging with {@link #level} only if specified here level is enabled for the current {@link #logger} too.
     * Ignored by default.
     */
    LogLevel ifEnabled() default OFF;

    /**
     * If specified log-level is enabled for the current {@link #logger} activates detailed logging.
     * For annotated {@link Method} verbose log includes argument/return values.
     * For annotated {@link Parameter} verbose log includes argument name.
     * Note: it is assumed that {@link LogLevel#OFF} deactivates verbose logging of annotated element for any level.
     */
    LogLevel verbose() default DEBUG;

    /**
     * Determines {@link Printer} implementation by specified bean name (or alias).
     * The printer will be used to convert argument/return values from raw type to {@link String}.
     * Note: if not specified highest priority compatible printer or {@link PrinterResolver#defaultPrinter} will be used.
     *
     * @see Order
     */
    String printer() default "";

    /**
     * Determines {@link EclairLogger} implementation by specified bean name (or alias) which should process this annotation.
     * Note: if not specified single candidate or {@link Primary} bean will be used for processing.
     */
    // TODO: convert to logger array everywhere?
    String logger() default "";

    /**
     * Annotated {@link Method} is able to log only beginning of execution.
     * Should be specified with unique {@link #logger} value per method.
     */
    @Repeatable(ins.class)
    @Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @interface in {

        /**
         * Alias for {@link #level}.
         */
        @AliasFor("level")
        LogLevel value() default DEBUG;

        /**
         * Expected level to log beginning of method execution.
         */
        @AliasFor("value")
        LogLevel level() default DEBUG;

        /**
         * Enables logging with {@link #level} only if specified here level is enabled for the current {@link #logger} too.
         * Ignored by default.
         */
        LogLevel ifEnabled() default OFF;

        /**
         * If specified log-level is enabled for the current {@link #logger} activates detailed logging.
         * Verbose log includes method argument values.
         * Note: it is assumed that {@link LogLevel#OFF} deactivates verbose logging of annotated method for any level.
         */
        LogLevel verbose() default DEBUG;

        /**
         * Determines {@link Printer} implementation by specified bean name (or alias).
         * The printer will be used to convert method argument values from raw type to {@link String}.
         * Note: if not specified highest priority compatible printer or {@link PrinterResolver#defaultPrinter} will be used.
         *
         * @see Order
         */
        String printer() default "";

        /**
         * Determines {@link EclairLogger} implementation by specified bean name (or alias) which should process this annotation.
         * Note: if not specified single candidate or {@link Primary} bean will be used for processing.
         */
        String logger() default "";
    }

    /**
     * Annotated {@link Method} is able to log only ending of execution.
     * Should be specified with unique {@link #logger} value per method.
     */
    @Repeatable(outs.class)
    @Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @interface out {

        /**
         * Alias for {@link #level}.
         */
        @AliasFor("level")
        LogLevel value() default DEBUG;

        /**
         * Expected level to log ending of method execution.
         */
        @AliasFor("value")
        LogLevel level() default DEBUG;

        /**
         * Enables logging with {@link #level} only if specified here level is enabled for the current {@link #logger} too.
         * Ignored by default.
         */
        LogLevel ifEnabled() default OFF;

        /**
         * If specified log-level is enabled for the current {@link #logger} activates detailed logging.
         * Verbose log includes method return value.
         * Note: it is assumed that {@link LogLevel#OFF} deactivates verbose logging of annotated method for any level.
         */
        LogLevel verbose() default DEBUG;

        /**
         * Determines {@link Printer} implementation by specified bean name (or alias).
         * The printer will be used to convert method return value from raw type to {@link String}.
         * Note: if not specified highest priority compatible printer or {@link PrinterResolver#defaultPrinter} will be used.
         *
         * @see Order
         */
        String printer() default "";

        /**
         * Determines {@link EclairLogger} implementation by specified bean name (or alias) which should process this annotation.
         * Note: if not specified single candidate or {@link Primary} bean will be used for processing.
         */
        String logger() default "";
    }

    /**
     * Specifies need to log emergency ending (but not normal beginning or ending) of annotated {@link Method} execution.
     * Could be specified multiple times with same {@link #logger} value per method.
     */
    @Repeatable(errors.class)
    @Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @interface error {

        /**
         * Alias for {@link #level}.
         */
        @AliasFor("level")
        LogLevel value() default ERROR;

        /**
         * Expected level to log emergency ending of method execution (interrupted by thrown {@link Throwable} or its child).
         */
        @AliasFor("value")
        LogLevel level() default ERROR;

        /**
         * Enables logging with {@link #level} only if specified here level is enabled for the current {@link #logger} too.
         * Ignored by default.
         */
        LogLevel ifEnabled() default OFF;

        /**
         * If specified log-level is enabled for the current {@link #logger} activates detailed logging.
         * Verbose log includes {@link Throwable#toString()} value.
         * Note: it is assumed that {@link LogLevel#OFF} deactivates verbose logging of annotated method for any level.
         */
        LogLevel verbose() default ERROR;

        /**
         * TODO: complement about detection of the most specific ancestor
         * Determines array of {@link Throwable} types (with children) which should be processed by this annotation.
         * Every thrown {@link Throwable} could be processed by only one annotation per {@link #logger}.
         * Tries to process any {@link Throwable} by default.
         */
        Class<? extends Throwable>[] ofType() default Throwable.class;

        /**
         * Determines filter of {@link Throwable} types (with children) which should NOT be processed by this annotation.
         * Note: 'exclude' filter has higher priority than 'ofType' defined by {@link #ofType}:
         * so type contained in both {@link #ofType} and 'exclude' arrays will NOT be processed by this annotation.
         */
        Class<? extends Throwable>[] exclude() default {};

        /**
         * Determines {@link EclairLogger} implementation by specified bean name (or alias) which should process this annotation.
         * Note: if not specified single candidate or {@link Primary} bean will be used for processing.
         */
        String logger() default "";
    }

    /**
     * Container to annotate method by multiple repeatable {@link in} annotations.
     *
     * @see Repeatable
     */
    @Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @interface ins {

        in[] value();
    }

    /**
     * Container to annotate method by multiple repeatable {@link out} annotations.
     *
     * @see Repeatable
     */
    @Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @interface outs {

        out[] value();
    }

    /**
     * Container to annotate method by multiple repeatable {@link error} annotations.
     *
     * @see Repeatable
     */
    @Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @interface errors {

        error[] value();
    }
}
