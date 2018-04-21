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

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;
import static org.springframework.boot.logging.LogLevel.DEBUG;

/**
 * @author Vyacheslav Klapatnyuk
 */
public class LevelSensitiveLoggerTest {

    @Test
    public void isLogInNecessaryByMethodEnabledAndParameterNull() {
        // given
        MethodInvocation invocation = mock(MethodInvocation.class);
        MethodLog methodLog = givenMethodLog(givenInLog(), null, null, null);

        LevelSensitiveLogger levelSensitiveLogger = spy(LevelSensitiveLogger.class);
        when(levelSensitiveLogger.isLogEnabled(any(), any())).thenReturn(true);
        // when
        boolean necessary = levelSensitiveLogger.isLogInNecessary(invocation, methodLog);
        // then
        verify(levelSensitiveLogger).getLoggerName(invocation);
        verify(levelSensitiveLogger).isLogEnabled(any(), any());
        assertTrue(necessary);
    }

    @Test
    public void isLogInNecessaryByMethodDisabledAndByParameterNull() {
        // given
        MethodInvocation invocation = mock(MethodInvocation.class);
        MethodLog methodLog = givenMethodLog(givenInLog(), null, null, null);

        LevelSensitiveLogger levelSensitiveLogger = spy(LevelSensitiveLogger.class);
        when(levelSensitiveLogger.isLogEnabled(any(), any())).thenReturn(false);
        // when
        boolean necessary = levelSensitiveLogger.isLogInNecessary(invocation, methodLog);
        // then
        verify(levelSensitiveLogger).getLoggerName(invocation);
        verify(levelSensitiveLogger).isLogEnabled(any(), any());
        assertFalse(necessary);
    }

    @Test
    public void isLogInNecessaryByMethodDisabledAndByParameterEnabled() {
        // given
        MethodInvocation invocation = mock(MethodInvocation.class);
        MethodLog methodLog = givenMethodLog(givenInLog(), givenParameterLog(), null, null);

        LevelSensitiveLogger levelSensitiveLogger = spy(LevelSensitiveLogger.class);
        when(levelSensitiveLogger.isLogEnabled(any(), any())).thenReturn(false, true);
        // when
        boolean necessary = levelSensitiveLogger.isLogInNecessary(invocation, methodLog);
        // then
        verify(levelSensitiveLogger).getLoggerName(invocation);
        verify(levelSensitiveLogger, times(2)).isLogEnabled(any(), any());
        assertTrue(necessary);
    }

    @Test
    public void isLogInNecessaryByMethodDisabledAndByParameterDisabled() {
        // given
        MethodInvocation invocation = mock(MethodInvocation.class);
        MethodLog methodLog = givenMethodLog(givenInLog(), givenParameterLog(), null, null);

        LevelSensitiveLogger levelSensitiveLogger = spy(LevelSensitiveLogger.class);
        when(levelSensitiveLogger.isLogEnabled(any(), any())).thenReturn(false, false);
        // when
        boolean necessary = levelSensitiveLogger.isLogInNecessary(invocation, methodLog);
        // then
        verify(levelSensitiveLogger).getLoggerName(invocation);
        verify(levelSensitiveLogger, times(2)).isLogEnabled(any(), any());
        assertFalse(necessary);
    }

    @Test
    public void isLogInNecessaryByMethodNullAndByParameterEnabled() {
        // given
        MethodInvocation invocation = mock(MethodInvocation.class);
        MethodLog methodLog = givenMethodLog(null, givenParameterLog(), null, null);

        LevelSensitiveLogger levelSensitiveLogger = spy(LevelSensitiveLogger.class);
        when(levelSensitiveLogger.isLogEnabled(any(), any())).thenReturn(true);
        // when
        boolean necessary = levelSensitiveLogger.isLogInNecessary(invocation, methodLog);
        // then
        verify(levelSensitiveLogger).getLoggerName(invocation);
        verify(levelSensitiveLogger).isLogEnabled(any(), any());
        assertTrue(necessary);
    }

    @Test
    public void isLogInNecessaryByMethodNullAndByParameterNull() {
        // given
        MethodInvocation invocation = mock(MethodInvocation.class);
        MethodLog methodLog = givenMethodLog(null, null, null, null);

        LevelSensitiveLogger levelSensitiveLogger = spy(LevelSensitiveLogger.class);
        // when
        boolean necessary = levelSensitiveLogger.isLogInNecessary(invocation, methodLog);
        // then
        verify(levelSensitiveLogger).getLoggerName(invocation);
        verify(levelSensitiveLogger, never()).isLogEnabled(any(), any());
        assertFalse(necessary);
    }

    @Test
    public void isLogOutNecessaryNotNullEnabled() {
        // given
        MethodInvocation invocation = mock(MethodInvocation.class);
        MethodLog methodLog = givenMethodLog(null, null, givenOutLog(), null);

        LevelSensitiveLogger levelSensitiveLogger = spy(LevelSensitiveLogger.class);
        when(levelSensitiveLogger.isLogEnabled(any(), any())).thenReturn(true);
        // when
        boolean necessary = levelSensitiveLogger.isLogOutNecessary(invocation, methodLog);
        // then
        verify(levelSensitiveLogger).getLoggerName(invocation);
        verify(levelSensitiveLogger).isLogEnabled(any(), any());
        assertTrue(necessary);
    }

    @Test
    public void isLogOutNecessaryNotNullDisabled() {
        // given
        MethodInvocation invocation = mock(MethodInvocation.class);
        MethodLog methodLog = givenMethodLog(null, null, givenOutLog(), null);

        LevelSensitiveLogger levelSensitiveLogger = spy(LevelSensitiveLogger.class);
        when(levelSensitiveLogger.isLogEnabled(any(), any())).thenReturn(false);
        // when
        boolean necessary = levelSensitiveLogger.isLogOutNecessary(invocation, methodLog);
        // then
        verify(levelSensitiveLogger).getLoggerName(invocation);
        verify(levelSensitiveLogger).isLogEnabled(any(), any());
        assertFalse(necessary);
    }

    @Test
    public void isLogOutNecessaryNull() {
        // given
        MethodInvocation invocation = mock(MethodInvocation.class);
        MethodLog methodLog = givenMethodLog(null, null, null, null);

        LevelSensitiveLogger levelSensitiveLogger = spy(LevelSensitiveLogger.class);
        // when
        boolean necessary = levelSensitiveLogger.isLogOutNecessary(invocation, methodLog);
        // then
        verify(levelSensitiveLogger, never()).getLoggerName(invocation);
        verify(levelSensitiveLogger, never()).isLogEnabled(any(), any());
        assertFalse(necessary);
    }

    @Test
    public void isLogErrorNecessaryNotNullEnabled() {
        // given
        MethodInvocation invocation = mock(MethodInvocation.class);
        MethodLog methodLog = givenMethodLog(null, null, null, givenErrorLog());

        LevelSensitiveLogger levelSensitiveLogger = spy(LevelSensitiveLogger.class);
        when(levelSensitiveLogger.isLogEnabled(any(), any())).thenReturn(true);

        Throwable throwable = mock(Throwable.class);
        // when
        boolean necessary = levelSensitiveLogger.isLogErrorNecessary(invocation, methodLog, throwable);
        // then
        verify(levelSensitiveLogger).getLoggerName(invocation);
        verify(levelSensitiveLogger).isLogEnabled(any(), any());
        assertTrue(necessary);
    }

    @Test
    public void isLogErrorNecessaryNotNullDisabled() {
        // given
        MethodInvocation invocation = mock(MethodInvocation.class);
        MethodLog methodLog = givenMethodLog(null, null, null, givenErrorLog());

        LevelSensitiveLogger levelSensitiveLogger = spy(LevelSensitiveLogger.class);
        when(levelSensitiveLogger.isLogEnabled(any(), any())).thenReturn(false);

        Throwable throwable = mock(Throwable.class);
        // when
        boolean necessary = levelSensitiveLogger.isLogErrorNecessary(invocation, methodLog, throwable);
        // then
        verify(levelSensitiveLogger).getLoggerName(invocation);
        verify(levelSensitiveLogger).isLogEnabled(any(), any());
        assertFalse(necessary);
    }

    @Test
    public void isLogErrorNecessaryNull() {
        // given
        MethodInvocation invocation = mock(MethodInvocation.class);
        MethodLog methodLog = givenMethodLog(null, null, null, null);

        LevelSensitiveLogger levelSensitiveLogger = spy(LevelSensitiveLogger.class);

        Throwable throwable = mock(Throwable.class);
        // when
        boolean necessary = levelSensitiveLogger.isLogErrorNecessary(invocation, methodLog, throwable);
        // then
        verify(levelSensitiveLogger, never()).getLoggerName(invocation);
        verify(levelSensitiveLogger, never()).isLogEnabled(any(), any());
        assertFalse(necessary);
    }

    private MethodLog givenMethodLog(InLog inLog, ParameterLog parameterLog, OutLog outLog, ErrorLog errorLog) {
        MethodLog methodLog = mock(MethodLog.class);
        when(methodLog.getInLog()).thenReturn(inLog);
        when(methodLog.getParameterLogs()).thenReturn(singletonList(parameterLog));
        when(methodLog.getOutLog()).thenReturn(outLog);
        when(methodLog.findErrorLog(any())).thenReturn(errorLog);
        return methodLog;
    }

    private InLog givenInLog() {
        InLog inLog = mock(InLog.class);
        when(inLog.getLevel()).thenReturn(DEBUG);
        when(inLog.getIfEnabledLevel()).thenReturn(DEBUG);
        return inLog;
    }

    private ParameterLog givenParameterLog() {
        ParameterLog parameterLog = mock(ParameterLog.class);
        when(parameterLog.getLevel()).thenReturn(DEBUG);
        when(parameterLog.getIfEnabledLevel()).thenReturn(DEBUG);
        return parameterLog;
    }

    private OutLog givenOutLog() {
        OutLog outLog = mock(OutLog.class);
        when(outLog.getLevel()).thenReturn(DEBUG);
        when(outLog.getIfEnabledLevel()).thenReturn(DEBUG);
        return outLog;
    }

    private ErrorLog givenErrorLog() {
        ErrorLog errorLog = mock(ErrorLog.class);
        when(errorLog.getLevel()).thenReturn(DEBUG);
        when(errorLog.getIfEnabledLevel()).thenReturn(DEBUG);
        return errorLog;
    }
}
