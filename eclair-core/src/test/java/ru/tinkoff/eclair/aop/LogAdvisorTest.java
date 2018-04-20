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
import ru.tinkoff.eclair.definition.MethodLog;
import ru.tinkoff.eclair.logger.EclairLogger;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author Vyacheslav Klapatnyuk
 */
public class LogAdvisorTest {

    private final Object result = new Object();
    private final RuntimeException throwable = new RuntimeException();

    private Method method;
    private Method bridgeMethod;
    private MethodLog methodLog;

    @Before
    public void init() throws NoSuchMethodException {
        method = Child.class.getMethod("method", String.class);
        bridgeMethod = Child.class.getMethod("method", Object.class);
        methodLog = mock(MethodLog.class);
        when(methodLog.getMethod()).thenReturn(method);
    }

    @Test
    public void matches() {
        // given
        EclairLogger eclairLogger = mock(EclairLogger.class);
        LogAdvisor logAdvisor = LogAdvisor.newInstance(eclairLogger, singletonList(methodLog));
        // when
        assertNotNull(logAdvisor);
        boolean matches = logAdvisor.matches(method, null);
        boolean bridgeMatches = logAdvisor.matches(bridgeMethod, null);
        // then
        assertTrue(matches);
        assertTrue(bridgeMatches);
    }

    @Test
    public void newInstanceNull() {
        // given
        EclairLogger eclairLogger = mock(EclairLogger.class);
        List<MethodLog> methodLogs = emptyList();
        // when
        LogAdvisor logAdvisor = LogAdvisor.newInstance(eclairLogger, methodLogs);
        // then
        assertNull(logAdvisor);
    }

    @Test
    public void newInstance() throws NoSuchMethodException {
        // given
        EclairLogger eclairLogger = mock(EclairLogger.class);
        // when
        LogAdvisor logAdvisor = LogAdvisor.newInstance(eclairLogger, singletonList(methodLog));
        // then
        assertNotNull(logAdvisor);
        assertThat(logAdvisor.getEclairLogger(), is(eclairLogger));

        Set<Map.Entry<Method, MethodLog>> entries = logAdvisor.getMethodLogs().entrySet();
        assertThat(entries, hasSize(1));

        Map.Entry<Method, MethodLog> entry = entries.iterator().next();
        assertThat(entry.getKey(), is(method));
        assertThat(entry.getValue(), is(methodLog));
    }

    @Test
    public void invokeLogInOut() throws Throwable {
        // given
        EclairLogger eclairLogger = mock(EclairLogger.class);
        LogAdvisor logAdvisor = LogAdvisor.newInstance(eclairLogger, singletonList(methodLog));

        MethodInvocation invocation = mock(MethodInvocation.class);
        when(invocation.getMethod()).thenReturn(method);
        when(invocation.proceed()).thenReturn(result);
        // when
        assertNotNull(logAdvisor);
        Object actualResult = logAdvisor.invoke(invocation);
        // then
        verify(eclairLogger).logInIfNecessary(invocation, methodLog);
        verify(eclairLogger).logOutIfNecessary(invocation, methodLog, result);
        assertThat(actualResult, is(result));
    }

    @Test
    public void invokeLogInError() throws Throwable {
        // given
        EclairLogger eclairLogger = mock(EclairLogger.class);
        LogAdvisor logAdvisor = LogAdvisor.newInstance(eclairLogger, singletonList(methodLog));

        MethodInvocation invocation = mock(MethodInvocation.class);
        when(invocation.getMethod()).thenReturn(method);
        when(invocation.proceed()).thenThrow(throwable);
        try {
            // when
            assertNotNull(logAdvisor);
            logAdvisor.invoke(invocation);
        } catch (Exception e) {
            // then
            verify(eclairLogger).logInIfNecessary(invocation, methodLog);
            verify(eclairLogger).logErrorIfNecessary(invocation, methodLog, throwable);
            assertThat(e, is(throwable));
        }
    }

    interface Parent<T> {

        void method(T input);
    }

    private static class Child implements Parent<String> {

        @Override
        public void method(String input) {
        }
    }
}
