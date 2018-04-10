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

package ru.tinkoff.eclair.definition.factory;

import org.junit.Test;
import ru.tinkoff.eclair.annotation.Mdc;
import ru.tinkoff.eclair.definition.ParameterMdc;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.springframework.core.annotation.AnnotationUtils.synthesizeAnnotation;

/**
 * @author Viacheslav Klapatniuk
 */
public class ParameterMdcFactoryTest {

    @Test
    public void newInstance() {
        // given
        Mdc mdc = givenMdc();
        // when
        ParameterMdc definition = ParameterMdcFactory.newInstance(mdc);
        // then
        assertThat(definition.getKey(), is("key"));
        assertThat(definition.getExpressionString(), is("value"));
        assertTrue(definition.isGlobal());
    }

    private Mdc givenMdc() {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("key", "key");
        attributes.put("value", "value");
        attributes.put("global", true);
        return synthesizeAnnotation(attributes, Mdc.class, null);
    }
}
