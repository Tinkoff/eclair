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

package ru.tinkoff.eclair.validate;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.runners.MockitoJUnitRunner;
import ru.tinkoff.eclair.annotation.Log;
import ru.tinkoff.eclair.annotation.Mdc;
import ru.tinkoff.eclair.core.AnnotationExtractor;
import ru.tinkoff.eclair.validate.log.group.*;
import ru.tinkoff.eclair.validate.mdc.group.MdcsValidator;
import ru.tinkoff.eclair.validate.mdc.group.MergedMdcsValidator;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Set;

import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.core.annotation.AnnotationUtils.synthesizeAnnotation;

/**
 * @author Vyacheslav Klapatnyuk
 */
@RunWith(MockitoJUnitRunner.class)
public class MethodValidatorTest {

    private Method method;
    private Method privateMethod;
    private Method staticMethod;

    @Captor
    private ArgumentCaptor<Set<Log>> parameterLogsCaptor;
    @Captor
    private ArgumentCaptor<Set<Mdc>> parameterMdcsCaptor;

    @Before
    public void init() throws NoSuchMethodException {
        method = MethodValidatorTest.class.getDeclaredMethod("method");
        privateMethod = MethodValidatorTest.class.getDeclaredMethod("privateMethod");
        staticMethod = MethodValidatorTest.class.getDeclaredMethod("staticMethod");
    }

    @SuppressWarnings("unused")
    void method() {
    }

    @SuppressWarnings("unused")
    private void privateMethod() {
    }

    @SuppressWarnings("unused")
    static void staticMethod() {
    }

    @Test(expected = AnnotationUsageException.class)
    public void validatePrivateAnnotatedMethod() {
        // given
        AnnotationExtractor annotationExtractor = mock(AnnotationExtractor.class);
        when(annotationExtractor.getLogs(eq(privateMethod))).thenReturn(singleton(synthesizeAnnotation(Log.class)));
        MethodValidator methodValidator = givenMethodValidator(annotationExtractor);
        // when
        methodValidator.validate(privateMethod);
        // then expected exception
    }

    @Test(expected = AnnotationUsageException.class)
    public void validatePrivateAnnotatedParameter() {
        // given
        AnnotationExtractor annotationExtractor = mock(AnnotationExtractor.class);
        when(annotationExtractor.getParameterLogs(eq(privateMethod))).thenReturn(singletonList(singleton(synthesizeAnnotation(Log.class))));
        MethodValidator methodValidator = givenMethodValidator(annotationExtractor);
        // when
        methodValidator.validate(privateMethod);
        // then expected exception
    }

    @Test(expected = AnnotationUsageException.class)
    public void validateStaticAnnotatedMethod() {
        // given
        AnnotationExtractor annotationExtractor = mock(AnnotationExtractor.class);
        when(annotationExtractor.getLogs(eq(staticMethod))).thenReturn(singleton(synthesizeAnnotation(Log.class)));
        MethodValidator methodValidator = givenMethodValidator(annotationExtractor);
        // when
        methodValidator.validate(staticMethod);
        // then expected exception
    }

    @Test(expected = AnnotationUsageException.class)
    public void validateStaticAnnotatedParameter() {
        // given
        AnnotationExtractor annotationExtractor = mock(AnnotationExtractor.class);
        when(annotationExtractor.getParameterLogs(eq(staticMethod))).thenReturn(singletonList(singleton(synthesizeAnnotation(Log.class))));
        MethodValidator methodValidator = givenMethodValidator(annotationExtractor);
        // when
        methodValidator.validate(staticMethod);
        // then expected exception
    }

    private MethodValidator givenMethodValidator(AnnotationExtractor annotationExtractor) {
        return new MethodValidator(annotationExtractor,
                mock(LogsValidator.class),
                mock(LogInsValidator.class),
                mock(LogOutsValidator.class),
                mock(LogErrorsValidator.class),
                mock(ParameterLogsValidator.class),
                mock(MdcsValidator.class),
                mock(MergedMdcsValidator.class));
    }

    @Test
    public void isTransitive() {
        // given
        AnnotationExtractor annotationExtractor = mock(AnnotationExtractor.class);

        Set<Log> logs = singleton(synthesizeAnnotation(Log.class));
        Set<Log.in> logIns = singleton(synthesizeAnnotation(Log.in.class));
        Set<Log.out> logOuts = singleton(synthesizeAnnotation(Log.out.class));
        Set<Log.error> logErrors = singleton(synthesizeAnnotation(Log.error.class));
        Set<Log> parameterLogs = singleton(synthesizeAnnotation(Log.class));
        Set<Log> parameterLogs1 = singleton(synthesizeAnnotation(Log.class));
        Set<Mdc> mdcs = singleton(synthesizeAnnotation(Mdc.class));
        Set<Mdc> parameterMdcs = singleton(synthesizeAnnotation(Mdc.class));
        Set<Mdc> parameterMdcs1 = singleton(synthesizeAnnotation(Mdc.class));

        when(annotationExtractor.getLogs(method)).thenReturn(logs);
        when(annotationExtractor.getLogIns(method)).thenReturn(logIns);
        when(annotationExtractor.getLogOuts(method)).thenReturn(logOuts);
        when(annotationExtractor.getLogErrors(method)).thenReturn(logErrors);
        when(annotationExtractor.getParameterLogs(method)).thenReturn(asList(parameterLogs, parameterLogs1));
        when(annotationExtractor.getMdcs(method)).thenReturn(mdcs);
        when(annotationExtractor.getParametersMdcs(method)).thenReturn(asList(parameterMdcs, parameterMdcs1));

        LogsValidator logsValidator = mock(LogsValidator.class);
        LogInsValidator logInsValidator = mock(LogInsValidator.class);
        LogOutsValidator logOutsValidator = mock(LogOutsValidator.class);
        LogErrorsValidator logErrorsValidator = mock(LogErrorsValidator.class);
        ParameterLogsValidator parameterLogsValidator = mock(ParameterLogsValidator.class);
        MdcsValidator mdcsValidator = mock(MdcsValidator.class);
        MergedMdcsValidator mergedMdcsValidator = mock(MergedMdcsValidator.class);

        MethodValidator methodValidator = new MethodValidator(annotationExtractor,
                logsValidator,
                logInsValidator,
                logOutsValidator,
                logErrorsValidator,
                parameterLogsValidator,
                mdcsValidator,
                mergedMdcsValidator);

        // when
        methodValidator.validate(method);

        // then
        verify(logsValidator).validate(method, logs);
        verify(logInsValidator).validate(method, logIns);
        verify(logOutsValidator).validate(method, logOuts);
        verify(logErrorsValidator).validate(method, logErrors);
        verifyParameterLogs(parameterLogsValidator, parameterLogs, parameterLogs1);
        verifyMdcs(mdcsValidator, mdcs, parameterMdcs, parameterMdcs1);
        verify(mergedMdcsValidator).validate(eq(method), any());
    }

    private void verifyParameterLogs(ParameterLogsValidator parameterLogsValidator, Set<Log> parameterLogs, Set<Log> parameterLogs1) {
        verify(parameterLogsValidator, times(2)).validate(eq(method), parameterLogsCaptor.capture());
        Iterator<Set<Log>> parameterLogsIterator = parameterLogsCaptor.getAllValues().iterator();
        assertThat(parameterLogsIterator.next(), is(parameterLogs));
        assertThat(parameterLogsIterator.next(), is(parameterLogs1));
    }

    private void verifyMdcs(MdcsValidator mdcsValidator, Set<Mdc> mdcs, Set<Mdc> parameterMdcs, Set<Mdc> parameterMdcs1) {
        verify(mdcsValidator, times(3)).validate(eq(method), parameterMdcsCaptor.capture());
        Iterator<Set<Mdc>> parameterMdcsIterator = parameterMdcsCaptor.getAllValues().iterator();
        assertThat(parameterMdcsIterator.next(), is(mdcs));
        assertThat(parameterMdcsIterator.next(), is(parameterMdcs));
        assertThat(parameterMdcsIterator.next(), is(parameterMdcs1));
    }
}
