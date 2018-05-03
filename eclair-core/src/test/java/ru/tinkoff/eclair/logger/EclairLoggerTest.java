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

package ru.tinkoff.eclair.logger;

import org.aopalliance.intercept.MethodInvocation;
import org.junit.Test;
import ru.tinkoff.eclair.definition.ErrorLog;
import ru.tinkoff.eclair.definition.InLog;
import ru.tinkoff.eclair.definition.OutLog;
import ru.tinkoff.eclair.definition.ParameterLog;
import ru.tinkoff.eclair.definition.method.MethodLog;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.mockito.Mockito.*;

/**
 * @author Vyacheslav Klapatnyuk
 */
public class EclairLoggerTest {

    private final MethodInvocation invocation = mock(MethodInvocation.class);
    private final Object result = mock(Object.class);
    private final Throwable throwable = mock(Throwable.class);

    @Test
    public void logInIfNecessaryWithInLog() {
        // given
        EclairLogger eclairLogger = spy(EclairLogger.class);
        MethodLog methodLog = mock(MethodLog.class);
        when(methodLog.getInLog()).thenReturn(mock(InLog.class));
        // when
        eclairLogger.logInIfNecessary(invocation, methodLog);
        // then
        verify(eclairLogger).logIn(eq(invocation), eq(methodLog));
    }

    @Test
    public void logInIfNecessaryWithParameterLogs() {
        // given
        EclairLogger eclairLogger = spy(EclairLogger.class);
        MethodLog methodLog = mock(MethodLog.class);
        when(methodLog.getInLog()).thenReturn(null);
        when(methodLog.getParameterLogs()).thenReturn(singletonList(mock(ParameterLog.class)));
        // when
        eclairLogger.logInIfNecessary(invocation, methodLog);
        // then
        verify(eclairLogger).logIn(eq(invocation), eq(methodLog));
    }

    @Test
    public void logInIfNecessaryEmpty() {
        // given
        EclairLogger eclairLogger = spy(EclairLogger.class);
        MethodLog methodLog = mock(MethodLog.class);
        when(methodLog.getInLog()).thenReturn(null);
        when(methodLog.getParameterLogs()).thenReturn(emptyList());
        // when
        eclairLogger.logInIfNecessary(invocation, methodLog);
        // then
        verify(eclairLogger, never()).logIn(any(), any());
    }

    @Test
    public void logOutIfNecessary() {
        // given
        EclairLogger eclairLogger = spy(EclairLogger.class);
        MethodLog methodLog = mock(MethodLog.class);
        when(methodLog.getOutLog()).thenReturn(mock(OutLog.class));
        // when
        eclairLogger.logOutIfNecessary(invocation, methodLog, result);
        // then
        verify(eclairLogger).logOut(eq(invocation), eq(methodLog), eq(result));
    }

    @Test
    public void logOutIfNecessaryEmpty() {
        // given
        EclairLogger eclairLogger = spy(EclairLogger.class);
        MethodLog methodLog = mock(MethodLog.class);
        when(methodLog.getOutLog()).thenReturn(null);
        // when
        eclairLogger.logOutIfNecessary(invocation, methodLog, result);
        // then
        verify(eclairLogger, never()).logOut(any(), any(), any());
    }

    @Test
    public void logErrorIfNecessaryWithErrorLog() {
        // given
        EclairLogger eclairLogger = spy(EclairLogger.class);
        MethodLog methodLog = mock(MethodLog.class);
        when(methodLog.findErrorLog(any())).thenReturn(mock(ErrorLog.class));
        // when
        eclairLogger.logErrorIfNecessary(invocation, methodLog, throwable);
        // then
        verify(eclairLogger).logError(eq(invocation), eq(methodLog), eq(throwable));
    }

    @Test
    public void logErrorIfNecessaryWithOutLog() {
        // given
        EclairLogger eclairLogger = spy(EclairLogger.class);
        MethodLog methodLog = mock(MethodLog.class);
        when(methodLog.findErrorLog(any())).thenReturn(null);
        when(methodLog.getOutLog()).thenReturn(mock(OutLog.class));
        // when
        eclairLogger.logErrorIfNecessary(invocation, methodLog, throwable);
        // then
        verify(eclairLogger).logError(eq(invocation), eq(methodLog), eq(throwable));
    }

    @Test
    public void logErrorIfNecessaryEmpty() {
        // given
        EclairLogger eclairLogger = spy(EclairLogger.class);
        MethodLog methodLog = mock(MethodLog.class);
        when(methodLog.findErrorLog(any())).thenReturn(null);
        when(methodLog.getOutLog()).thenReturn(null);
        // when
        eclairLogger.logErrorIfNecessary(invocation, methodLog, throwable);
        // then
        verify(eclairLogger, never()).logError(any(), any(), any());
    }
}
