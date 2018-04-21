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
import org.springframework.core.annotation.AnnotationUtils;
import ru.tinkoff.eclair.annotation.Log;
import ru.tinkoff.eclair.printer.resolver.AliasedPrinterResolver;
import ru.tinkoff.eclair.printer.resolver.PrinterResolver;
import ru.tinkoff.eclair.validate.AnnotationUsageException;

import java.lang.reflect.Method;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static org.springframework.boot.logging.LogLevel.DEBUG;

/**
 * @author Vyacheslav Klapatnyuk
 */
public class LogValidatorTest {

    private Method method;

    @Before
    public void init() throws NoSuchMethodException {
        method = LogValidatorTest.class.getMethod("init");
    }

    @Test(expected = AnnotationUsageException.class)
    public void validateLevels() {
        // given
        PrinterResolver printerResolver = new AliasedPrinterResolver(emptyMap(), emptyMap());
        LogValidator<Log> logValidator = new LogValidator<>(printerResolver);
        Log log = AnnotationUtils.synthesizeAnnotation(singletonMap("ifEnabled", DEBUG), Log.class, null);
        // when
        logValidator.validate(method, log);
        // then expected exception
    }

    @Test(expected = AnnotationUsageException.class)
    public void validatePrinter() {
        // given
        PrinterResolver printerResolver = new AliasedPrinterResolver(emptyMap(), emptyMap());
        LogValidator<Log> logValidator = new LogValidator<>(printerResolver);
        Log log = AnnotationUtils.synthesizeAnnotation(singletonMap("printer", "unknown"), Log.class, null);
        // when
        logValidator.validate(method, log);
        // then expected exception
    }
}
