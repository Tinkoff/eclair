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
import ru.tinkoff.eclair.definition.MethodMdc;
import ru.tinkoff.eclair.definition.ParameterMdc;

import java.lang.reflect.Method;
import java.util.*;

import static java.util.Arrays.asList;
import static java.util.Collections.*;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.springframework.core.annotation.AnnotationUtils.synthesizeAnnotation;

/**
 * @author Viacheslav Klapatniuk
 */
public class MethodMdcFactoryTest {

    @Test
    public void newInstance() throws NoSuchMethodException {
        // given
        Method method = givenMethod();
        List<String> parameterNames = emptyList();
        Set<ParameterMdc> methodMdcs = givenMethodMdcs();
        List<Set<ParameterMdc>> argumentMdcs = givenArgumentMdcs();
        // when
        MethodMdc methodMdc = MethodMdcFactory.newInstance(method, parameterNames, methodMdcs, argumentMdcs);
        // then
        assertThat(methodMdc.getMethod(), is(method));
        thenMethodDefinition(methodMdc.getMethodDefinitions());
        thenParameterDefinitions(methodMdc.getParameterDefinitions());
    }

    private Method givenMethod() throws NoSuchMethodException {
        return MethodMdcFactoryTest.class.getMethod("annotatedMethod", String.class, String.class);
    }

    @SuppressWarnings("unused")
    public void annotatedMethod(String a, String b) {
    }

    private Set<ParameterMdc> givenMethodMdcs() {
        return singleton(ParameterMdcFactory.newInstance(givenMdc("method")));
    }

    private List<Set<ParameterMdc>> givenArgumentMdcs() {
        return asList(
                singleton(ParameterMdcFactory.newInstance(givenMdc("a"))),
                singleton(ParameterMdcFactory.newInstance(givenMdc("b")))
        );
    }

    private Mdc givenMdc(String key) {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("key", key);
        attributes.put("value", "value");
        return synthesizeAnnotation(attributes, Mdc.class, null);
    }

    private void thenMethodDefinition(Set<ParameterMdc> definitions) {
        assertThat(definitions, hasSize(1));
        assertThat(definitions.iterator().next(), notNullValue());
    }

    private void thenParameterDefinitions(List<Set<ParameterMdc>> definitions) {
        assertThat(definitions, hasSize(2));
        thenAParameterDefinition(definitions.get(0));
        thenBParameterDefinition(definitions.get(1));
    }

    private void thenAParameterDefinition(Set<ParameterMdc> definitions) {
        assertThat(definitions, hasSize(1));
        assertThat(definitions.iterator().next().getKey(), is("a"));
    }

    private void thenBParameterDefinition(Set<ParameterMdc> definitions) {
        assertThat(definitions, hasSize(1));
        assertThat(definitions.iterator().next().getKey(), is("b"));
    }

    @Test
    public void newInstanceNull() throws NoSuchMethodException {
        // given
        Method method = givenMethod();
        List<String> parameterNames = emptyList();
        Set<ParameterMdc> methodMdcs = emptySet();
        List<Set<ParameterMdc>> argumentMdcs = asList(emptySet(), emptySet());
        // when
        MethodMdc methodMdc = MethodMdcFactory.newInstance(method, parameterNames, methodMdcs, argumentMdcs);
        // then
        assertThat(methodMdc, nullValue());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void newInstanceImmutableMethodDefinitions() throws NoSuchMethodException {
        // given
        Method method = givenMethod();
        List<String> parameterNames = emptyList();
        Set<ParameterMdc> methodMdcs = givenMethodMdcs();
        List<Set<ParameterMdc>> argumentMdcs = givenArgumentMdcs();
        // when
        MethodMdc methodMdc = MethodMdcFactory.newInstance(method, parameterNames, methodMdcs, argumentMdcs);
        // then
        methodMdc.getMethodDefinitions().add(ParameterMdcFactory.newInstance(givenMdc("")));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void newInstanceImmutableParameterDefinitions() throws NoSuchMethodException {
        // given
        Method method = givenMethod();
        List<String> parameterNames = emptyList();
        Set<ParameterMdc> methodMdcs = givenMethodMdcs();
        List<Set<ParameterMdc>> argumentMdcs = givenArgumentMdcs();
        // when
        MethodMdc methodMdc = MethodMdcFactory.newInstance(method, parameterNames, methodMdcs, argumentMdcs);
        // then
        methodMdc.getParameterDefinitions().add(emptySet());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void newInstanceImmutableParameterNames() throws NoSuchMethodException {
        // given
        Method method = givenMethod();
        List<String> parameterNames = new ArrayList<>(singletonList("parameterName"));
        Set<ParameterMdc> methodMdcs = givenMethodMdcs();
        List<Set<ParameterMdc>> argumentMdcs = givenArgumentMdcs();
        // when
        MethodMdc methodMdc = MethodMdcFactory.newInstance(method, parameterNames, methodMdcs, argumentMdcs);
        // then
        methodMdc.getParameterNames().add("");
    }
}
