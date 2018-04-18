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

import org.springframework.context.support.GenericApplicationContext;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import ru.tinkoff.eclair.core.AnnotationExtractor;
import ru.tinkoff.eclair.core.printer.PrinterResolver;
import ru.tinkoff.eclair.logger.EclairLogger;

import java.util.Map;
import java.util.stream.Stream;

/**
 * TODO: add more information about error location (class, method?)
 *
 * @author Vyacheslav Klapatnyuk
 */
public class BeanClassValidator implements Validator {

    private final AnnotationExtractor annotationExtractor = AnnotationExtractor.getInstance();

    private final BeanMethodValidator beanMethodValidator;

    public BeanClassValidator(GenericApplicationContext applicationContext,
                              Map<String, EclairLogger> loggers,
                              PrinterResolver printerResolver) {
        this.beanMethodValidator = new BeanMethodValidator(applicationContext, loggers, printerResolver);
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return annotationExtractor.getCandidateMethods(clazz).stream()
                .anyMatch(method ->
                        annotationExtractor.hasAnyAnnotation(method) ||
                                Stream.of(method.getParameters()).anyMatch(annotationExtractor::hasAnyAnnotation)

                );
    }

    @Override
    public void validate(Object target, Errors errors) {
        annotationExtractor.getCandidateMethods((Class<?>) target)
                .forEach(method -> beanMethodValidator.validate(method, errors));
    }
}
