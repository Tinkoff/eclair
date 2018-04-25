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

package ru.tinkoff.eclair.definition;

/**
 * @author Vyacheslav Klapatnyuk
 */
public class ParameterMdc {

    private final String key;
    private final String expressionString;
    private final boolean global;

    public ParameterMdc(String key, String expressionString, boolean global) {
        this.key = key;
        this.expressionString = expressionString;
        this.global = global;
    }

    public String getKey() {
        return key;
    }

    public String getExpressionString() {
        return expressionString;
    }

    public boolean isGlobal() {
        return global;
    }
}
