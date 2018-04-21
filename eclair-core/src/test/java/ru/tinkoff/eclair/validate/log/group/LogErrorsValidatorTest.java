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

package ru.tinkoff.eclair.validate.log.group;

import org.junit.Before;
import org.junit.Test;
import ru.tinkoff.eclair.annotation.Log;
import ru.tinkoff.eclair.printer.resolver.AliasedPrinterResolver;
import ru.tinkoff.eclair.validate.AnnotationUsageException;
import ru.tinkoff.eclair.validate.log.single.LogErrorValidator;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.core.annotation.AnnotationUtils.synthesizeAnnotation;

/**
 * @author Vyacheslav Klapatnyuk
 */
public class LogErrorsValidatorTest {

    private Method method;

    @Before
    public void init() throws NoSuchMethodException {
        method = LogErrorsValidatorTest.class.getMethod("init");
    }

    @Test(expected = AnnotationUsageException.class)
    public void validate() {
        // given
        Map<String, Set<String>> loggerNames = singletonMap("logger", new HashSet<>(asList("logger", "")));
        LogErrorValidator logErrorValidator = new LogErrorValidator(new AliasedPrinterResolver(emptyMap(), emptyMap()));
        LogErrorsValidator logErrorsValidator = new LogErrorsValidator(loggerNames, logErrorValidator);

        Map<String, Object> ofType = singletonMap("ofType", new Class<?>[]{StringIndexOutOfBoundsException.class, ArrayIndexOutOfBoundsException.class});
        Log.error logError = synthesizeAnnotation(ofType, Log.error.class, null);
        Map<String, Object> ofType1 = singletonMap("ofType", new Class<?>[]{ArrayIndexOutOfBoundsException.class, StringIndexOutOfBoundsException.class});
        Log.error logError1 = synthesizeAnnotation(ofType1, Log.error.class, null);
        Set<Log.error> target = new HashSet<>(asList(logError, logError1));
        // when
        logErrorsValidator.validate(method, target);
        // then expected exception
    }

    @Test
    public void isTransitive() {
        // given
        Map<String, Set<String>> loggerNames = singletonMap("logger", new HashSet<>(asList("logger", "")));
        LogErrorValidator logErrorValidator = mock(LogErrorValidator.class);
        LogErrorsValidator logErrorsValidator = new LogErrorsValidator(loggerNames, logErrorValidator);

        Map<String, Object> ofType = singletonMap("ofType", new Class<?>[]{Throwable.class});
        Log.error logError = synthesizeAnnotation(ofType, Log.error.class, null);
        Map<String, Object> ofType1 = singletonMap("ofType", new Class<?>[]{Exception.class});
        Log.error logError1 = synthesizeAnnotation(ofType1, Log.error.class, null);
        Set<Log.error> target = new HashSet<>(asList(logError, logError1));
        // when
        logErrorsValidator.validate(method, target);
        // then
        verify(logErrorValidator).validate(method, logError);
        verify(logErrorValidator).validate(method, logError1);
    }
}
