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

package ru.tinkoff.eclair.validate.mdc.group;

import org.junit.Before;
import org.junit.Test;
import org.springframework.core.annotation.AnnotationUtils;
import ru.tinkoff.eclair.annotation.Mdc;
import ru.tinkoff.eclair.validate.AnnotationUsageException;
import ru.tinkoff.eclair.validate.log.single.LogValidatorTest;

import java.lang.reflect.Method;
import java.util.Collection;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonMap;

/**
 * @author Vyacheslav Klapatnyuk
 */
public class MdcsValidatorTest {

    private final MdcsValidator mdcsValidator = new MdcsValidator();

    private Method method;

    @Before
    public void init() throws NoSuchMethodException {
        method = LogValidatorTest.class.getMethod("init");
    }

    @Test(expected = AnnotationUsageException.class)
    public void validateDuplicate() {
        // given
        Mdc mdc = AnnotationUtils.synthesizeAnnotation(singletonMap("key", "key"), Mdc.class, null);
        Mdc mdc1 = AnnotationUtils.synthesizeAnnotation(singletonMap("key", "key"), Mdc.class, null);
        Collection<Mdc> target = asList(mdc, mdc1);
        // when
        mdcsValidator.validate(method, target);
        // then expected exception
    }

    @Test(expected = AnnotationUsageException.class)
    public void validateEmptyDuplicate() {
        // given
        Mdc mdc = AnnotationUtils.synthesizeAnnotation(singletonMap("key", ""), Mdc.class, null);
        Mdc mdc1 = AnnotationUtils.synthesizeAnnotation(singletonMap("key", ""), Mdc.class, null);
        Collection<Mdc> target = asList(mdc, mdc1);
        // when
        mdcsValidator.validate(method, target);
        // then expected exception
    }
}
