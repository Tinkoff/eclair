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

package ru.tinkoff.eclair.core;

import org.junit.Test;
import ru.tinkoff.eclair.definition.ErrorLog;
import ru.tinkoff.eclair.definition.factory.TestErrorLogFactory;

import java.util.LinkedHashSet;
import java.util.Set;

import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * @author Vyacheslav Klapatnyuk
 */
public class ErrorLogResolverTest {

    private final ErrorLogResolver errorLogResolver = ErrorLogResolver.getInstance();

    @Test
    public void resolve() {
        // given
        ErrorLog aErrorLog = TestErrorLogFactory.newInstance(
                new Class<?>[]{NullPointerException.class, IndexOutOfBoundsException.class},
                new Class<?>[]{StringIndexOutOfBoundsException.class});
        Set<ErrorLog> errorLogs = singleton(aErrorLog);
        Class<?> causeClass = ArrayIndexOutOfBoundsException.class;
        // when
        ErrorLog errorLog = errorLogResolver.resolve(errorLogs, causeClass);
        // then
        assertThat(errorLog, is(aErrorLog));
    }

    @Test
    public void resolveExclude() {
        // given
        ErrorLog aErrorLog = TestErrorLogFactory.newInstance(new Class<?>[]{Throwable.class}, new Class<?>[]{RuntimeException.class});
        Set<ErrorLog> errorLogs = singleton(aErrorLog);
        Class<?> causeClass = NullPointerException.class;
        // when
        ErrorLog errorLog = errorLogResolver.resolve(errorLogs, causeClass);
        // then
        assertThat(errorLog, nullValue());
    }

    @Test
    public void resolveNotFound() {
        // given
        ErrorLog aErrorLog = TestErrorLogFactory.newInstance(new Class<?>[]{Exception.class}, new Class<?>[]{RuntimeException.class});
        Set<ErrorLog> errorLogs = singleton(aErrorLog);
        Class<?> causeClass = Throwable.class;
        // when
        ErrorLog errorLog = errorLogResolver.resolve(errorLogs, causeClass);
        // then
        assertThat(errorLog, nullValue());
    }

    @Test
    public void resolveMostSpecific() {
        // given
        ErrorLog aErrorLog = TestErrorLogFactory.newInstance(
                new Class<?>[]{NullPointerException.class, IndexOutOfBoundsException.class},
                new Class<?>[]{StringIndexOutOfBoundsException.class});
        ErrorLog bErrorLog = TestErrorLogFactory.newInstance(
                new Class<?>[]{ArrayIndexOutOfBoundsException.class},
                new Class<?>[]{});
        Set<ErrorLog> errorLogs = new LinkedHashSet<>(asList(aErrorLog, bErrorLog));
        Class<?> causeClass = ArrayIndexOutOfBoundsException.class;
        // when
        ErrorLog errorLog = errorLogResolver.resolve(errorLogs, causeClass);
        // then
        assertThat(errorLog, is(bErrorLog));
    }
}
