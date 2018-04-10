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
import org.springframework.core.ParameterNameDiscoverer;

import java.lang.reflect.Method;
import java.util.List;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Viacheslav Klapatniuk
 */
public class ParameterNameResolverTest {

    private final Method method = ParameterNameResolverTest.class.getMethod("method", String.class, int.class);
    private final ParameterNameResolver parameterNameResolver = new ParameterNameResolver();

    public ParameterNameResolverTest() throws NoSuchMethodException {
    }

    @Test
    public void tryToResolve() {
        // given
        ParameterNameDiscoverer discoverer = mock(ParameterNameDiscoverer.class);
        when(discoverer.getParameterNames(eq(method))).thenReturn(new String[]{"s", "i"});
        parameterNameResolver.setParameterNameDiscoverer(discoverer);
        // when
        List<String> parameterNames = parameterNameResolver.tryToResolve(method);
        // then
        assertThat(parameterNames, hasSize(2));
        assertThat(parameterNames.get(0), is("s"));
        assertThat(parameterNames.get(1), is("i"));
    }

    @Test
    public void tryToResolveNull() {
        // given
        ParameterNameDiscoverer discoverer = mock(ParameterNameDiscoverer.class);
        when(discoverer.getParameterNames(eq(method))).thenReturn(null);
        parameterNameResolver.setParameterNameDiscoverer(discoverer);
        // when
        List<String> parameterNames = parameterNameResolver.tryToResolve(method);
        // then
        assertThat(parameterNames, hasSize(2));
        assertThat(parameterNames.get(0), nullValue());
        assertThat(parameterNames.get(1), nullValue());
    }

    @SuppressWarnings("unused")
    public void method(String s, int i) {
    }
}
