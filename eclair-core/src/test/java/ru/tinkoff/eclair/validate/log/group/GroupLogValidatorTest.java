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
import ru.tinkoff.eclair.validate.AnnotationUsageException;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonMap;
import static org.springframework.core.annotation.AnnotationUtils.synthesizeAnnotation;

/**
 * @author Vyacheslav Klapatnyuk
 */
public class GroupLogValidatorTest {

    private Method method;

    @Before
    public void init() throws NoSuchMethodException {
        method = GroupLogValidatorTest.class.getMethod("init");
    }

    @Test(expected = AnnotationUsageException.class)
    public void validateNameAndPrimary() {
        // given
        Map<String, Set<String>> loggerNames = singletonMap("logger", new HashSet<>(asList("logger", "")));
        TestGroupLogValidator groupLogValidator = new TestGroupLogValidator(loggerNames);
        Log log = synthesizeAnnotation(singletonMap("logger", ""), Log.class, null);
        Log log1 = synthesizeAnnotation(singletonMap("logger", "logger"), Log.class, null);
        Set<Log> annotations = new HashSet<>(asList(log, log1));
        // when
        groupLogValidator.validate(method, annotations);
        // then expected exception
    }

    @Test(expected = AnnotationUsageException.class)
    public void validateTwoAliases() {
        // given
        Map<String, Set<String>> loggerNames = singletonMap("logger", new HashSet<>(asList("logger", "alias")));
        TestGroupLogValidator groupLogValidator = new TestGroupLogValidator(loggerNames);
        Log log = synthesizeAnnotation(singletonMap("logger", "logger"), Log.class, null);
        Log log1 = synthesizeAnnotation(singletonMap("logger", "alias"), Log.class, null);
        Set<Log> annotations = new HashSet<>(asList(log, log1));
        // when
        groupLogValidator.validate(method, annotations);
        // then expected exception
    }

    @Test(expected = AnnotationUsageException.class)
    public void groupAnnotationsByLoggerUnknownLogger() {
        // given
        Map<String, Set<String>> loggerNames = singletonMap("logger", new HashSet<>(asList("logger", "")));
        TestGroupLogValidator groupLogValidator = new TestGroupLogValidator(loggerNames);
        Log log = synthesizeAnnotation(singletonMap("logger", "unknown"), Log.class, null);
        Log log1 = synthesizeAnnotation(singletonMap("logger", "unknown1"), Log.class, null);
        Set<Log> annotations = new HashSet<>(asList(log, log1));
        // when
        groupLogValidator.groupAnnotationsByLogger(method, annotations);
        // then expected exception
    }

    @Test(expected = AnnotationUsageException.class)
    public void groupAnnotationsByLoggerEmptyLogger() {
        // given
        Map<String, Set<String>> loggerNames = singletonMap("logger", singleton("logger"));
        TestGroupLogValidator groupLogValidator = new TestGroupLogValidator(loggerNames);
        Log log = synthesizeAnnotation(singletonMap("logger", ""), Log.class, null);
        Log log1 = synthesizeAnnotation(singletonMap("logger", ""), Log.class, null);
        Set<Log> annotations = new HashSet<>(asList(log, log1));
        // when
        groupLogValidator.groupAnnotationsByLogger(method, annotations);
        // then expected exception
    }

    private static class TestGroupLogValidator extends GroupLogValidator<Log> {

        TestGroupLogValidator(Map<String, Set<String>> loggerNames) {
            super(loggerNames);
        }
    }
}
