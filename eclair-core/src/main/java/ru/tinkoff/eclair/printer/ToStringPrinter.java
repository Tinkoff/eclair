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

package ru.tinkoff.eclair.printer;

import java.util.Arrays;

/**
 * @author Vyacheslav Klapatnyuk
 */
public class ToStringPrinter extends Printer {

    @Override
    protected String serialize(Object input) {
        if (input instanceof String) {
            return "\"" + input + "\"";
        }
        if (input.getClass().isArray()) {
            // in the expected descending order of popularity
            if (input instanceof byte[]) {
                return Arrays.toString((byte[]) input);
            }
            if (input instanceof char[]) {
                return Arrays.toString((char[]) input);
            }
            if (input instanceof int[]) {
                return Arrays.toString((int[]) input);
            }
            if (input instanceof boolean[]) {
                return Arrays.toString((boolean[]) input);
            }
            if (input instanceof short[]) {
                return Arrays.toString((short[]) input);
            }
            if (input instanceof long[]) {
                return Arrays.toString((long[]) input);
            }
            if (input instanceof float[]) {
                return Arrays.toString((float[]) input);
            }
            if (input instanceof double[]) {
                return Arrays.toString((double[]) input);
            }
            return Arrays.toString((Object[]) input);
        }
        return input.toString();
    }
}
