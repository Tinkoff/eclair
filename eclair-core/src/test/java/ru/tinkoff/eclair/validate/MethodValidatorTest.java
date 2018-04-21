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

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.GenericApplicationContext;
import ru.tinkoff.eclair.annotation.Log;
import ru.tinkoff.eclair.core.AnnotationExtractor;
import ru.tinkoff.eclair.logger.EclairLogger;
import ru.tinkoff.eclair.printer.resolver.PrinterResolver;

import java.lang.reflect.Method;
import java.util.Map;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.core.annotation.AnnotationUtils.synthesizeAnnotation;

/**
 * @author Vyacheslav Klapatnyuk
 */
public class MethodValidatorTest {

    private final GenericApplicationContext applicationContext = mock(GenericApplicationContext.class);
    private final Map<String, EclairLogger> loggers = emptyMap();
    private final PrinterResolver printerResolver = mock(PrinterResolver.class);

    private Method privateMethod;
    private Method staticMethod;

    @Before
    public void init() throws NoSuchMethodException {
        privateMethod = MethodValidatorTest.class.getDeclaredMethod("privateMethod");
        staticMethod = MethodValidatorTest.class.getDeclaredMethod("staticMethod");
    }

    @SuppressWarnings("unused")
    private void privateMethod() {
    }

    @SuppressWarnings("unused")
    static void staticMethod() {
    }

    @Test(expected = AnnotationUsageException.class)
    public void validatePrivateAnnotatedMethod() {
        // given
        AnnotationExtractor annotationExtractor = mock(AnnotationExtractor.class);
        when(annotationExtractor.getLogs(eq(privateMethod))).thenReturn(singleton(synthesizeAnnotation(Log.class)));
        MethodValidator methodValidator = new MethodValidator(annotationExtractor, applicationContext, loggers, printerResolver);
        // when
        methodValidator.validate(privateMethod);
        // then expected exception
    }

    @Test(expected = AnnotationUsageException.class)
    public void validatePrivateAnnotatedParameter() {
        // given
        AnnotationExtractor annotationExtractor = mock(AnnotationExtractor.class);
        when(annotationExtractor.getParameterLogs(eq(privateMethod))).thenReturn(singletonList(singleton(synthesizeAnnotation(Log.class))));
        MethodValidator methodValidator = new MethodValidator(annotationExtractor, applicationContext, loggers, printerResolver);
        // when
        methodValidator.validate(privateMethod);
        // then expected exception
    }

    @Test(expected = AnnotationUsageException.class)
    public void validateStaticAnnotatedMethod() {
        // given
        AnnotationExtractor annotationExtractor = mock(AnnotationExtractor.class);
        when(annotationExtractor.getLogs(eq(staticMethod))).thenReturn(singleton(synthesizeAnnotation(Log.class)));
        MethodValidator methodValidator = new MethodValidator(annotationExtractor, applicationContext, loggers, printerResolver);
        // when
        methodValidator.validate(staticMethod);
        // then expected exception
    }

    @Test(expected = AnnotationUsageException.class)
    public void validateStaticAnnotatedParameter() {
        // given
        AnnotationExtractor annotationExtractor = mock(AnnotationExtractor.class);
        when(annotationExtractor.getParameterLogs(eq(staticMethod))).thenReturn(singletonList(singleton(synthesizeAnnotation(Log.class))));
        MethodValidator methodValidator = new MethodValidator(annotationExtractor, applicationContext, loggers, printerResolver);
        // when
        methodValidator.validate(staticMethod);
        // then expected exception
    }
}
