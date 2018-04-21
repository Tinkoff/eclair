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

package ru.tinkoff.eclair.validate.log.single;

import org.junit.Before;
import org.junit.Test;
import ru.tinkoff.eclair.annotation.Log;
import ru.tinkoff.eclair.printer.resolver.AliasedPrinterResolver;
import ru.tinkoff.eclair.printer.resolver.PrinterResolver;
import ru.tinkoff.eclair.validate.AnnotationUsageException;

import java.lang.reflect.Method;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static org.springframework.core.annotation.AnnotationUtils.synthesizeAnnotation;

/**
 * @author Vyacheslav Klapatnyuk
 */
public class LogErrorValidatorTest {

    private Method method;

    @Before
    public void init() throws NoSuchMethodException {
        method = LogErrorValidatorTest.class.getMethod("init");
    }

    @Test(expected = AnnotationUsageException.class)
    public void validateOfTypeEmpty() {
        // given
        PrinterResolver printerResolver = new AliasedPrinterResolver(emptyMap(), emptyMap());
        LogErrorValidator logErrorValidator = new LogErrorValidator(printerResolver);
        Log.error logError = synthesizeAnnotation(singletonMap("ofType", new Class<?>[0]), Log.error.class, null);
        // when
        logErrorValidator.validate(method, logError);
        // then expected exception
    }

    @Test(expected = AnnotationUsageException.class)
    public void validateOfTypeDuplicates() {
        // given
        PrinterResolver printerResolver = new AliasedPrinterResolver(emptyMap(), emptyMap());
        LogErrorValidator logErrorValidator = new LogErrorValidator(printerResolver);
        Log.error logError = synthesizeAnnotation(singletonMap("ofType", new Class<?>[]{Throwable.class, Throwable.class}), Log.error.class, null);
        // when
        logErrorValidator.validate(method, logError);
        // then expected exception
    }

    @Test(expected = AnnotationUsageException.class)
    public void validateExcludeDuplicates() {
        // given
        PrinterResolver printerResolver = new AliasedPrinterResolver(emptyMap(), emptyMap());
        LogErrorValidator logErrorValidator = new LogErrorValidator(printerResolver);
        Log.error logError = synthesizeAnnotation(singletonMap("exclude", new Class<?>[]{Throwable.class, Throwable.class}), Log.error.class, null);
        // when
        logErrorValidator.validate(method, logError);
        // then expected exception
    }
}
