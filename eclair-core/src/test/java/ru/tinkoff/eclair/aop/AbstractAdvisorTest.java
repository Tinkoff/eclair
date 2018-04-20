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

import org.junit.Before;
import org.junit.Test;
import ru.tinkoff.eclair.definition.method.MethodLog;
import ru.tinkoff.eclair.logger.EclairLogger;

import java.lang.reflect.Method;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Vyacheslav Klapatnyuk
 */
public class AbstractAdvisorTest {

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

    interface Parent<T> {

        void method(T input);
    }

    private static class Child implements Parent<String> {

        @Override
        public void method(String input) {
        }
    }
}
