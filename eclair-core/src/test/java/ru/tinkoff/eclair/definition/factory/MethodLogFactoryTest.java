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
import ru.tinkoff.eclair.annotation.Log;
import ru.tinkoff.eclair.definition.*;
import ru.tinkoff.eclair.printer.ToStringPrinter;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.Arrays.asList;
import static java.util.Collections.*;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.springframework.core.annotation.AnnotationUtils.synthesizeAnnotation;

/**
 * @author Viacheslav Klapatniuk
 */
public class MethodLogFactoryTest {

    @Test
    public void newInstance() throws NoSuchMethodException {
        // given
        Method method = MethodLogFactoryTest.class.getMethod("newInstance");
        List<String> parameterNames = singletonList("parameterName");
        InLog inLog = givenInLog();
        ParameterLog parameterLog = givenParameterLog();
        List<ParameterLog> parameterLogs = singletonList(parameterLog);
        OutLog outLog = givenOutLog();
        Set<ErrorLog> errorLogs = singleton(TestErrorLogFactory.newInstance(new Class<?>[]{Throwable.class}, new Class<?>[]{}));
        // when
        MethodLog methodLog = MethodLogFactory.newInstance(method, parameterNames, inLog, parameterLogs, outLog, errorLogs);
        // then
        assertThat(methodLog.getMethod(), is(method));
        assertThat(methodLog.getParameterNames(), hasSize(1));
        assertThat(methodLog.getParameterNames().get(0), is("parameterName"));
        assertThat(methodLog.getInLog(), is(inLog));
        assertThat(methodLog.getParameterLogs(), hasSize(1));
        assertThat(methodLog.getParameterLogs().get(0), is(parameterLog));
        assertThat(methodLog.getOutLog(), is(outLog));
    }

    @Test
    public void newInstanceNull() throws NoSuchMethodException {
        // given
        Method method = MethodLogFactoryTest.class.getMethod("newInstance");
        List<String> parameterNames = emptyList();
        InLog inLog = null;
        List<ParameterLog> parameterLogs = asList(null, null, null);
        OutLog outLog = null;
        Set<ErrorLog> errorLogs = emptySet();
        // when
        MethodLog methodLog = MethodLogFactory.newInstance(method, parameterNames, inLog, parameterLogs, outLog, errorLogs);
        // then
        assertThat(methodLog, nullValue());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void newInstanceParameterLogsImmutable() throws NoSuchMethodException {
        // given
        Method method = MethodLogFactoryTest.class.getMethod("newInstance");
        List<String> parameterNames = emptyList();
        InLog inLog = givenInLog();
        List<ParameterLog> parameterLogs = new ArrayList<>();
        OutLog outLog = givenOutLog();
        Set<ErrorLog> errorLogs = new HashSet<>();
        // when
        MethodLog methodLog = MethodLogFactory.newInstance(method, parameterNames, inLog, parameterLogs, outLog, errorLogs);
        methodLog.getParameterLogs().add(givenParameterLog());
        // then expected exception
    }

    @Test(expected = UnsupportedOperationException.class)
    public void newInstanceParameterNamesImmutable() throws NoSuchMethodException {
        // given
        Method method = MethodLogFactoryTest.class.getMethod("newInstance");
        List<String> parameterNames = emptyList();
        InLog inLog = givenInLog();
        List<ParameterLog> parameterLogs = new ArrayList<>();
        OutLog outLog = givenOutLog();
        Set<ErrorLog> errorLogs = new HashSet<>();
        // when
        MethodLog methodLog = MethodLogFactory.newInstance(method, parameterNames, inLog, parameterLogs, outLog, errorLogs);
        methodLog.getParameterNames().add("name");
        // then expected exception
    }

    @Test
    public void findErrorLog() throws NoSuchMethodException {
        // given
        Method method = MethodLogFactoryTest.class.getMethod("newInstance");
        List<String> parameterNames = emptyList();
        InLog inLog = givenInLog();
        List<ParameterLog> parameterLogs = emptyList();
        OutLog outLog = givenOutLog();
        ErrorLog errorLog = TestErrorLogFactory.newInstance(new Class<?>[]{Exception.class}, new Class<?>[]{Error.class});
        Set<ErrorLog> errorLogs = singleton(errorLog);
        // when
        MethodLog methodLog = MethodLogFactory.newInstance(method, parameterNames, inLog, parameterLogs, outLog, errorLogs);
        // then
        assertThat(methodLog.findErrorLog(Throwable.class), nullValue());
        assertThat(methodLog.findErrorLog(RuntimeException.class), is(errorLog));
        assertThat(methodLog.findErrorLog(RuntimeException.class), is(errorLog));
        assertThat(methodLog.findErrorLog(OutOfMemoryError.class), nullValue());
    }

    private InLog givenInLog() {
        Log.in logIn = synthesizeAnnotation(Log.in.class);
        return InLogFactory.newInstance(logIn, new ToStringPrinter());
    }

    private ParameterLog givenParameterLog() {
        return ParameterLogFactory.newInstance(synthesizeAnnotation(Log.class), new ToStringPrinter());
    }

    private OutLog givenOutLog() {
        Log.out logOut = synthesizeAnnotation(Log.out.class);
        return OutLogFactory.newInstance(logOut, new ToStringPrinter());
    }
}
