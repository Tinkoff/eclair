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

import org.aopalliance.aop.Advice;
import org.aopalliance.intercept.MethodInterceptor;
import org.springframework.aop.support.StaticMethodMatcherPointcutAdvisor;
import org.springframework.core.BridgeMethodResolver;
import ru.tinkoff.eclair.definition.method.MethodDefinition;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

/**
 * @author Vyacheslav Klapatnyuk
 */
abstract class AbstractAdvisor<T extends MethodDefinition> extends StaticMethodMatcherPointcutAdvisor implements MethodInterceptor {

    final Map<Method, T> methodDefinitions;

    AbstractAdvisor(List<T> methodDefinitions) {
        this.methodDefinitions = methodDefinitions.stream().collect(toMap(T::getMethod, identity()));
    }

    @Override
    public Advice getAdvice() {
        return this;
    }

    @Override
    public boolean matches(Method method, Class<?> targetClass) {
        return methodDefinitions.containsKey(method) || methodDefinitions.containsKey(BridgeMethodResolver.findBridgedMethod(method));
    }

    /**
     * Despite the fact that this method is not being used.
     */
    @Override
    public boolean isPerInstance() {
        return false;
    }

    Map<Method, T> getMethodDefinitions() {
        return methodDefinitions;
    }
}
