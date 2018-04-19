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

package ru.tinkoff.eclair.validate;

import lombok.Getter;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * @author Vyacheslav Klapatnyuk
 */
@Getter
public class AnnotationUsageException extends RuntimeException {

    private final Method method;
    private final String action;

    private Annotation annotation;

    public AnnotationUsageException(Method method, String message, String action) {
        this(method, message, action, null);
    }

    public AnnotationUsageException(Method method, String message, String action, Annotation annotation) {
        super(message);
        this.method = method;
        this.annotation = annotation;
        this.action = action;
    }
}
