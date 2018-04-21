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
import ru.tinkoff.eclair.validate.log.single.LogValidator;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonMap;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.core.annotation.AnnotationUtils.synthesizeAnnotation;

/**
 * @author Vyacheslav Klapatnyuk
 */
public class ParameterLogsValidatorTest {

    private Method method;

    @Before
    public void init() throws NoSuchMethodException {
        method = ParameterLogsValidatorTest.class.getMethod("init");
    }

    @Test
    public void isTransitive() {
        // given
        Map<String, Set<String>> loggerNames = singletonMap("logger", new HashSet<>(asList("logger", "")));
        @SuppressWarnings("unchecked")
        LogValidator<Log> logValidator = (LogValidator<Log>) mock(LogValidator.class);
        ParameterLogsValidator parameterLogsValidator = new ParameterLogsValidator(loggerNames, logValidator);
        Log log = synthesizeAnnotation(Log.class);
        Set<Log> annotations = singleton(log);
        // when
        parameterLogsValidator.validate(method, annotations);
        // then
        verify(logValidator).validate(method, log);
    }
}
