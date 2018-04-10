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

import java.lang.reflect.Method;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * @author Viacheslav Klapatniuk
 */
public class ReversedBridgeMethodResolverTest {

    private final ReversedBridgeMethodResolver reversedBridgeMethodResolver = ReversedBridgeMethodResolver.getInstance();

    @Test
    public void findBridgeMethod() throws NoSuchMethodException {
        // given
        Method method = Child.class.getMethod("method", String.class);
        // when
        Method bridgeMethod = reversedBridgeMethodResolver.findBridgeMethod(method);
        // then
        assertThat(bridgeMethod, notNullValue());
        assertThat(bridgeMethod.getParameterTypes()[0], isA(Object.class));
        assertThat(bridgeMethod.getReturnType(), isA(Object.class));
    }

    @SuppressWarnings("unused")
    private interface Parent<T> {

        T method(T input);
    }

    private static class Child implements Parent<String> {

        @Override
        public String method(String input) {
            return null;
        }
    }

    @Test
    public void findBridgeMethodExtends() throws NoSuchMethodException {
        // given
        Method method = ExtendsChild.class.getMethod("method", Exception.class);
        // when
        Method bridgeMethod = reversedBridgeMethodResolver.findBridgeMethod(method);
        // then
        assertThat(bridgeMethod, notNullValue());
        assertTrue(Throwable.class.isAssignableFrom(bridgeMethod.getParameterTypes()[0]));
        assertTrue(Object.class.isAssignableFrom(bridgeMethod.getReturnType()));
    }

    @SuppressWarnings("unused")
    private interface ExtendsParent<T extends Throwable> {

        T method(T input);
    }

    private static class ExtendsChild implements ExtendsParent<Exception> {

        @Override
        public Exception method(Exception input) {
            return null;
        }
    }

    @Test
    public void notBridge() throws NoSuchMethodException {
        // given
        Method method = NotBridge.class.getMethod("method", RuntimeException.class);
        // when
        Method bridgeMethod = reversedBridgeMethodResolver.findBridgeMethod(method);
        // then
        assertThat(bridgeMethod, nullValue());
    }

    @SuppressWarnings("unused")
    private static class NotBridge {

        public Exception method(Exception input) {
            return null;
        }

        public RuntimeException method(RuntimeException input) {
            return null;
        }
    }
}
