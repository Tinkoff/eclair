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

package ru.tinkoff.eclair.autoconfigure;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.tinkoff.eclair.validate.AnnotationUsageException;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.springframework.core.annotation.AnnotationUtils.synthesizeAnnotation;

/**
 * @author Vyacheslav Klapatnyuk
 */
public class EclairFailureAnalyzerTest {

    private final EclairFailureAnalyzer eclairFailureAnalyzer = new EclairFailureAnalyzer();
    private final String message = "message";
    private final String action = "action";

    private Method method;

    @Before
    public void init() throws NoSuchMethodException {
        method = EclairFailureAnalyzerTest.class.getMethod("method");
    }

    @SuppressWarnings("unused")
    public void method() {
    }

    @Test
    public void buildDescription() throws NoSuchMethodException {
        // given
        Annotation annotation = synthesizeAnnotation(Autowired.class);
        AnnotationUsageException exception = new AnnotationUsageException(method, message, action, annotation);
        // when
        String description = eclairFailureAnalyzer.buildDescription(exception);
        // then
        String expected = "message\n" +
                "    - method: public void ru.tinkoff.eclair.autoconfigure.EclairFailureAnalyzerTest.method()\n" +
                "    - annotation: @org.springframework.beans.factory.annotation.Autowired(required=true)";
        assertThat(description, is(expected));
    }

    @Test
    public void buildDescriptionWithoutAnnotation() throws NoSuchMethodException {
        // given
        AnnotationUsageException exception = new AnnotationUsageException(method, message, action);
        // when
        String description = eclairFailureAnalyzer.buildDescription(exception);
        // then
        String expected = "message\n" +
                "    - method: public void ru.tinkoff.eclair.autoconfigure.EclairFailureAnalyzerTest.method()";
        assertThat(description, is(expected));
    }

    @Test(expected = NullPointerException.class)
    public void buildDescriptionMethodNull() {
        // given, when
        new AnnotationUsageException(null, "message", "action");
        // then expected exception
    }

    @Test(expected = NullPointerException.class)
    public void buildDescriptionMessageNull() {
        // given, when
        new AnnotationUsageException(method, null, "action");
        // then expected exception
    }

    @Test(expected = NullPointerException.class)
    public void buildDescriptionActionNull() {
        // given, when
        new AnnotationUsageException(method, "message", null);
        // then expected exception
    }
}
