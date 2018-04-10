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

import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

/**
 * @author Viacheslav Klapatniuk
 */
public final class ReversedBridgeMethodResolver {

    private static final ReversedBridgeMethodResolver instance = new ReversedBridgeMethodResolver();

    public static ReversedBridgeMethodResolver getInstance() {
        return instance;
    }

    private ReversedBridgeMethodResolver() {
    }

    Method findBridgeMethod(Method original) {
        List<Method> candidates = Stream.of(original.getDeclaringClass().getDeclaredMethods())
                .filter(Method::isBridge)
                .filter(candidate -> byName(original, candidate))
                .filter(candidate -> byParameters(original, candidate))
                .filter(candidate -> byReturnType(original, candidate))
                .collect(toList());
        if (candidates.isEmpty()) {
            return null;
        }
        if (candidates.size() > 1) {
            throw new IllegalArgumentException("More than one bridge candidate methods found: " + candidates);
        }
        return candidates.get(0);
    }

    private boolean byName(Method original, Method candidate) {
        return candidate.getName().equals(original.getName());
    }

    private boolean byParameters(Method original, Method candidate) {
        Class<?>[] parameterTypes = original.getParameterTypes();
        Class<?>[] candidateParameterTypes = candidate.getParameterTypes();
        if (parameterTypes.length != candidateParameterTypes.length) {
            return false;
        }
        for (int a = 0; a < parameterTypes.length; a++) {
            if (!candidateParameterTypes[a].isAssignableFrom(parameterTypes[a])) {
                return false;
            }
        }
        return true;
    }

    private boolean byReturnType(Method original, Method candidate) {
        return candidate.getReturnType().isAssignableFrom(original.getReturnType());
    }
}
