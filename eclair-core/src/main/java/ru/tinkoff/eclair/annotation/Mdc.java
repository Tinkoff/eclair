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
import org.springframework.context.ApplicationContext;

import java.lang.annotation.*;

/**
 * Defines MDC (Mapped Diagnostic Context) entry.
 * MDC is {@link LogLevel}-insensitive.
 * Before method execution beginning, {@link Mdc} will be processed first and after ending cleared last.
 * So annotations @Log / @Log.in / @Log.out of the same method will be processed inside @Mdc processing.
 *
 * @author Vyacheslav Klapatnyuk
 */
@Repeatable(Mdcs.class)
@Target({ElementType.PARAMETER, ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Mdc {

    /**
     * Key of the MDC entry.
     * If empty, it will be synthesized by code meta-data: annotated method or parameter name.
     * Note: It is not always possible to obtain information about parameter names at runtime.
     * In that case, MDC keys will contain method name and parameter index.
     */
    String key() default "";

    /**
     * TODO: add method arguments to context root
     * Value of the MDC entry.
     * Can contain SpEL (Spring Expression Language) and invoke static methods or beans by id from the {@link ApplicationContext}.
     * If empty, it will be synthesized by code meta-data: annotated parameter value (or each parameter of annotated method).
     */
    String value() default "";

    /**
     * Key/value pair defined by this annotation automatically cleared after exit from the method by default.
     * 'global' MDC is available within {@link ThreadLocal} scope.
     */
    boolean global() default false;
}
