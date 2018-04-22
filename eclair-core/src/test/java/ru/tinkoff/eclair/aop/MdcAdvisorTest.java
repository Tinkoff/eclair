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

package ru.tinkoff.eclair.aop;

import org.aopalliance.intercept.MethodInvocation;
import org.junit.Before;
import org.junit.Test;
import ru.tinkoff.eclair.core.ExpressionEvaluator;
import ru.tinkoff.eclair.definition.ParameterMdc;
import ru.tinkoff.eclair.definition.method.MethodMdc;

import java.lang.reflect.Method;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static java.util.Arrays.asList;
import static java.util.Collections.*;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author Vyacheslav Klapatnyuk
 */
public class MdcAdvisorTest {

    private final Set<String> localKeys = emptySet();

    private Method method;

    @Before
    public void init() throws NoSuchMethodException {
        method = MdcAdvisorTest.class.getDeclaredMethod("method", String.class, String.class, String.class);
    }

    @SuppressWarnings("unused")
    private void method(String a, String b, String c) {
    }

    @Test
    public void newInstance() {
        // given
        List<MethodMdc> methodMdcs = emptyList();
        ExpressionEvaluator expressionEvaluator = mock(ExpressionEvaluator.class);
        // when
        MdcAdvisor mdcAdvisor = MdcAdvisor.newInstance(methodMdcs, expressionEvaluator);
        // then
        assertNull(mdcAdvisor);
    }

    @Test
    public void processMethodDefinitions() {
        // given
        MethodMdc methodMdc = mock(MethodMdc.class);
        ParameterMdc parameterMdc = givenParameterMdc("key", "value");
        ParameterMdc parameterMdc1 = givenParameterMdc("key1", "value1");
        when(methodMdc.getMethodDefinitions()).thenReturn(new LinkedHashSet<>(asList(parameterMdc, parameterMdc1)));

        MdcAdvisor mdcAdvisor = MdcAdvisor.newInstance(singletonList(methodMdc), givenExpressionEvaluator("value", "value1"));
        assertNotNull(mdcAdvisor);
        MdcAdvisor mdcAdvisorStub = spy(mdcAdvisor);
        doNothing().when(mdcAdvisorStub).putMdc(any(), any(), any(), any());

        // when
        mdcAdvisorStub.processMethodDefinitions(givenMethodInvocation("0", "1", "2"), methodMdc, localKeys);

        // then
        verify(mdcAdvisorStub).putMdc("key", "value", parameterMdc, localKeys);
        verify(mdcAdvisorStub).putMdc("key1", "value1", parameterMdc1, localKeys);
    }

    @Test
    public void processMethodDefinitionsEmptyKey() {
        // given
        MethodMdc methodMdc = mock(MethodMdc.class);
        ParameterMdc parameterMdc = givenParameterMdc("", "value");
        when(methodMdc.getMethodDefinitions()).thenReturn(singleton(parameterMdc));

        MdcAdvisor mdcAdvisor = MdcAdvisor.newInstance(singletonList(methodMdc), givenExpressionEvaluator("value", null));
        assertNotNull(mdcAdvisor);
        MdcAdvisor mdcAdvisorStub = spy(mdcAdvisor);
        doNothing().when(mdcAdvisorStub).putMdc(any(), any(), any(), any());

        // when
        mdcAdvisorStub.processMethodDefinitions(givenMethodInvocation("0", "1", "2"), methodMdc, localKeys);

        // then
        verify(mdcAdvisorStub).putMdc("method", "value", parameterMdc, localKeys);
    }

    @Test
    public void processMethodDefinitionsEmptyExpressionString() {
        // given
        MethodMdc methodMdc = mock(MethodMdc.class);
        ParameterMdc parameterMdc = givenParameterMdc("key", "");
        ParameterMdc parameterMdc1 = givenParameterMdc("key1", "");
        when(methodMdc.getMethodDefinitions()).thenReturn(new LinkedHashSet<>(asList(parameterMdc, parameterMdc1)));
        when(methodMdc.getParameterNames()).thenReturn(asList("a", "b", "c"));

        MdcAdvisor mdcAdvisor = MdcAdvisor.newInstance(singletonList(methodMdc), mock(ExpressionEvaluator.class));
        assertNotNull(mdcAdvisor);
        MdcAdvisor mdcAdvisorStub = spy(mdcAdvisor);
        doNothing().when(mdcAdvisorStub).putMdc(any(), any(), any(), any());

        // when
        mdcAdvisorStub.processMethodDefinitions(givenMethodInvocation("0", "1", "2"), methodMdc, localKeys);

        // then
        verify(mdcAdvisorStub).putMdc("key[a]", "0", parameterMdc, localKeys);
        verify(mdcAdvisorStub).putMdc("key[b]", "1", parameterMdc, localKeys);
        verify(mdcAdvisorStub).putMdc("key[c]", "2", parameterMdc, localKeys);
        verify(mdcAdvisorStub).putMdc("key1[a]", "0", parameterMdc1, localKeys);
        verify(mdcAdvisorStub).putMdc("key1[b]", "1", parameterMdc1, localKeys);
        verify(mdcAdvisorStub).putMdc("key1[c]", "2", parameterMdc1, localKeys);
    }

    @Test
    public void processMethodDefinitionsEmptyKeyAndExpressionString() {
        // given
        MethodMdc methodMdc = mock(MethodMdc.class);
        ParameterMdc parameterMdc = givenParameterMdc("", "");
        when(methodMdc.getMethodDefinitions()).thenReturn(singleton(parameterMdc));
        when(methodMdc.getParameterNames()).thenReturn(asList("a", "b", "c"));

        MdcAdvisor mdcAdvisor = MdcAdvisor.newInstance(singletonList(methodMdc), mock(ExpressionEvaluator.class));
        assertNotNull(mdcAdvisor);
        MdcAdvisor mdcAdvisorStub = spy(mdcAdvisor);
        doNothing().when(mdcAdvisorStub).putMdc(any(), any(), any(), any());

        // when
        mdcAdvisorStub.processMethodDefinitions(givenMethodInvocation("0", "1", "2"), methodMdc, localKeys);

        // then
        verify(mdcAdvisorStub).putMdc("a", "0", parameterMdc, localKeys);
        verify(mdcAdvisorStub).putMdc("b", "1", parameterMdc, localKeys);
        verify(mdcAdvisorStub).putMdc("c", "2", parameterMdc, localKeys);
    }

    @Test
    public void processMethodDefinitionsEmptyKeyAndExpressionStringWithoutParameterNames() {
        // given
        MethodMdc methodMdc = mock(MethodMdc.class);
        ParameterMdc parameterMdc = givenParameterMdc("", "");
        when(methodMdc.getMethodDefinitions()).thenReturn(singleton(parameterMdc));
        when(methodMdc.getParameterNames()).thenReturn(asList(null, null, null));

        MdcAdvisor mdcAdvisor = MdcAdvisor.newInstance(singletonList(methodMdc), mock(ExpressionEvaluator.class));
        assertNotNull(mdcAdvisor);
        MdcAdvisor mdcAdvisorStub = spy(mdcAdvisor);
        doNothing().when(mdcAdvisorStub).putMdc(any(), any(), any(), any());

        // when
        mdcAdvisorStub.processMethodDefinitions(givenMethodInvocation("0", "1", "2"), methodMdc, localKeys);

        // then
        verify(mdcAdvisorStub).putMdc("method[0]", "0", parameterMdc, localKeys);
        verify(mdcAdvisorStub).putMdc("method[1]", "1", parameterMdc, localKeys);
        verify(mdcAdvisorStub).putMdc("method[2]", "2", parameterMdc, localKeys);
    }

    @Test
    public void processParameterDefinitions() {
        // given
        MethodMdc methodMdc = mock(MethodMdc.class);
        ParameterMdc parameterMdc = givenParameterMdc("key", "value");
        ParameterMdc parameterMdc1 = givenParameterMdc("key1", "value1");
        when(methodMdc.getParameterDefinitions()).thenReturn(asList(new LinkedHashSet<>(asList(parameterMdc, parameterMdc1)), emptySet(), emptySet()));

        MdcAdvisor mdcAdvisor = MdcAdvisor.newInstance(singletonList(methodMdc), givenExpressionEvaluator("value", "value1"));
        assertNotNull(mdcAdvisor);
        MdcAdvisor mdcAdvisorStub = spy(mdcAdvisor);
        doNothing().when(mdcAdvisorStub).putMdc(any(), any(), any(), any());

        // when
        mdcAdvisorStub.processParameterDefinitions(givenMethodInvocation("0", "1", "2"), methodMdc, localKeys);

        // then
        verify(mdcAdvisorStub).putMdc("key", "value", parameterMdc, localKeys);
        verify(mdcAdvisorStub).putMdc("key1", "value1", parameterMdc1, localKeys);
    }

    @Test
    public void processParameterDefinitionsEmptyKey() {
        // given
        MethodMdc methodMdc = mock(MethodMdc.class);
        ParameterMdc parameterMdc = givenParameterMdc("", "value");
        when(methodMdc.getParameterDefinitions()).thenReturn(asList(singleton(parameterMdc), emptySet(), emptySet()));
        when(methodMdc.getParameterNames()).thenReturn(asList("a", "b", "c"));

        MdcAdvisor mdcAdvisor = MdcAdvisor.newInstance(singletonList(methodMdc), givenExpressionEvaluator("value", null));
        assertNotNull(mdcAdvisor);
        MdcAdvisor mdcAdvisorStub = spy(mdcAdvisor);
        doNothing().when(mdcAdvisorStub).putMdc(any(), any(), any(), any());

        // when
        mdcAdvisorStub.processParameterDefinitions(givenMethodInvocation("0", "1", "2"), methodMdc, localKeys);

        // then
        verify(mdcAdvisorStub).putMdc("a", "value", parameterMdc, localKeys);
    }

    @Test
    public void processParameterDefinitionsEmptyExpressionString() {
        // given
        MethodMdc methodMdc = mock(MethodMdc.class);
        ParameterMdc parameterMdc = givenParameterMdc("key", "");
        ParameterMdc parameterMdc1 = givenParameterMdc("key1", "");
        when(methodMdc.getParameterDefinitions()).thenReturn(asList(new LinkedHashSet<>(asList(parameterMdc, parameterMdc1)), emptySet(), emptySet()));

        MdcAdvisor mdcAdvisor = MdcAdvisor.newInstance(singletonList(methodMdc), mock(ExpressionEvaluator.class));
        assertNotNull(mdcAdvisor);
        MdcAdvisor mdcAdvisorStub = spy(mdcAdvisor);
        doNothing().when(mdcAdvisorStub).putMdc(any(), any(), any(), any());

        // when
        mdcAdvisorStub.processParameterDefinitions(givenMethodInvocation("0", "1", "2"), methodMdc, localKeys);

        // then
        verify(mdcAdvisorStub).putMdc("key", "0", parameterMdc, localKeys);
        verify(mdcAdvisorStub).putMdc("key1", "0", parameterMdc1, localKeys);
    }

    @Test
    public void processParameterDefinitionsEmptyKeyAndExpressionString() {
        // given
        MethodMdc methodMdc = mock(MethodMdc.class);
        ParameterMdc parameterMdc = givenParameterMdc("", "");
        when(methodMdc.getParameterDefinitions()).thenReturn(asList(singleton(parameterMdc), emptySet(), emptySet()));
        when(methodMdc.getParameterNames()).thenReturn(asList("a", "b", "c"));

        MdcAdvisor mdcAdvisor = MdcAdvisor.newInstance(singletonList(methodMdc), mock(ExpressionEvaluator.class));
        assertNotNull(mdcAdvisor);
        MdcAdvisor mdcAdvisorStub = spy(mdcAdvisor);
        doNothing().when(mdcAdvisorStub).putMdc(any(), any(), any(), any());

        // when
        mdcAdvisorStub.processParameterDefinitions(givenMethodInvocation("0", "1", "2"), methodMdc, localKeys);

        // then
        verify(mdcAdvisorStub).putMdc("a", "0", parameterMdc, localKeys);
    }

    @Test
    public void processParameterDefinitionsEmptyKeyAndExpressionStringWithoutParameterNames() {
        // given
        MethodMdc methodMdc = mock(MethodMdc.class);
        ParameterMdc parameterMdc = givenParameterMdc("", "");
        when(methodMdc.getParameterDefinitions()).thenReturn(asList(singleton(parameterMdc), emptySet(), emptySet()));
        when(methodMdc.getParameterNames()).thenReturn(asList(null, null, null));

        MdcAdvisor mdcAdvisor = MdcAdvisor.newInstance(singletonList(methodMdc), mock(ExpressionEvaluator.class));
        assertNotNull(mdcAdvisor);
        MdcAdvisor mdcAdvisorStub = spy(mdcAdvisor);
        doNothing().when(mdcAdvisorStub).putMdc(any(), any(), any(), any());

        // when
        mdcAdvisorStub.processParameterDefinitions(givenMethodInvocation("0", "1", "2"), methodMdc, localKeys);

        // then
        verify(mdcAdvisorStub).putMdc("method[0]", "0", parameterMdc, localKeys);
    }

    private MethodInvocation givenMethodInvocation(Object... arguments) {
        MethodInvocation invocation = mock(MethodInvocation.class);
        when(invocation.getMethod()).thenReturn(method);
        when(invocation.getArguments()).thenReturn(arguments);
        return invocation;
    }

    private ParameterMdc givenParameterMdc(String key, String expressionString) {
        ParameterMdc parameterMdc = mock(ParameterMdc.class);
        when(parameterMdc.getKey()).thenReturn(key);
        when(parameterMdc.getExpressionString()).thenReturn(expressionString);
        return parameterMdc;
    }

    private ExpressionEvaluator givenExpressionEvaluator(String value, String value1) {
        ExpressionEvaluator expressionEvaluator = mock(ExpressionEvaluator.class);
        when(expressionEvaluator.evaluate(any())).thenReturn(value, value1);
        when(expressionEvaluator.evaluate(any(), any())).thenReturn(value, value1);
        return expressionEvaluator;
    }

    @Test
    public void synthesizeKey() {
        // given
        MdcAdvisor mdcAdvisor = MdcAdvisor.newInstance(singletonList(mock(MethodMdc.class)), mock(ExpressionEvaluator.class));
        String prefix = "prefix";
        String name = "name";
        // when
        assertNotNull(mdcAdvisor);
        String synthesizedKey = mdcAdvisor.synthesizeKey(prefix, name);
        // then
        assertThat(synthesizedKey, is("prefix[name]"));
    }
}
