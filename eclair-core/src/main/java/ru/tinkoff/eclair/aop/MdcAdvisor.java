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
import org.slf4j.MDC;
import ru.tinkoff.eclair.core.ExpressionEvaluator;
import ru.tinkoff.eclair.definition.ParameterMdc;
import ru.tinkoff.eclair.definition.method.MethodMdc;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.Objects.isNull;

/**
 * @author Vyacheslav Klapatnyuk
 */
class MdcAdvisor extends AbstractAdvisor<MethodMdc> {

    private final ExpressionEvaluator expressionEvaluator;

    private MdcAdvisor(List<MethodMdc> methodMdcs,
                       ExpressionEvaluator expressionEvaluator) {
        super(methodMdcs);
        this.expressionEvaluator = expressionEvaluator;
    }

    static MdcAdvisor newInstance(List<MethodMdc> methodMdcs,
                                  ExpressionEvaluator expressionEvaluator) {
        return methodMdcs.isEmpty() ? null : new MdcAdvisor(methodMdcs, expressionEvaluator);
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        Set<String> localKeys = new HashSet<>();
        try (LocalKeysHolder ignored = new LocalKeysHolder(localKeys)) {
            MethodMdc methodMdc = methodDefinitions.get(invocation.getMethod());
            processMethodDefinitions(invocation, methodMdc, localKeys);
            processParameterDefinitions(invocation, methodMdc, localKeys);
            return invocation.proceed();
        }
    }

    void processMethodDefinitions(MethodInvocation invocation, MethodMdc methodMdc, Set<String> localKeys) {
        for (ParameterMdc definition : methodMdc.getMethodDefinitions()) {
            String key = definition.getKey();
            if (key.isEmpty()) {
                key = invocation.getMethod().getName();
            }
            String expressionString = definition.getExpressionString();
            if (expressionString.isEmpty()) {
                Object[] arguments = invocation.getArguments();
                List<String> parameterNames = methodMdc.getParameterNames();
                for (int a = 0; a < arguments.length; a++) {
                    String parameterName = parameterNames.get(a);
                    if (isNull(parameterName)) {
                        putMdc(synthesizeKey(key, a), arguments[a], definition, localKeys);
                    } else if (definition.getKey().isEmpty()) {
                        putMdc(parameterName, arguments[a], definition, localKeys);
                    } else {
                        putMdc(synthesizeKey(key, parameterName), arguments[a], definition, localKeys);
                    }
                }
            } else {
                Object value = expressionEvaluator.evaluate(expressionString);
                putMdc(key, value, definition, localKeys);
            }
        }
    }

    void processParameterDefinitions(MethodInvocation invocation, MethodMdc methodMdc, Set<String> localKeys) {
        Object[] arguments = invocation.getArguments();
        for (int a = 0; a < arguments.length; a++) {
            for (ParameterMdc definition : methodMdc.getParameterDefinitions().get(a)) {
                String key = definition.getKey();
                if (key.isEmpty()) {
                    key = methodMdc.getParameterNames().get(a);
                    if (isNull(key)) {
                        key = synthesizeKey(invocation.getMethod().getName(), a);
                    }
                }
                String expressionString = definition.getExpressionString();
                Object value = expressionString.isEmpty() ? arguments[a] : expressionEvaluator.evaluate(expressionString, arguments[a]);
                putMdc(key, value, definition, localKeys);
            }
        }
    }

    String synthesizeKey(String prefix, int index) {
        return synthesizeKey(prefix, String.valueOf(index));
    }

    String synthesizeKey(String prefix, String name) {
        return prefix + "[" + name + "]";
    }

    void putMdc(String key, Object value, ParameterMdc definition, Set<String> localKeys) {
        if (!definition.isGlobal()) {
            localKeys.add(key);
        }
        MDC.put(key, isNull(value) ? null : value.toString());
    }

    private static final class LocalKeysHolder implements AutoCloseable {

        private final Set<String> localKeys;

        private LocalKeysHolder(Set<String> localKeys) {
            this.localKeys = localKeys;
        }

        @Override
        public void close() throws Exception {
            localKeys.forEach(MDC::remove);
        }
    }
}
