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

/**
 * @author Vyacheslav Klapatnyuk
 */
public class OverriddenToStringPrinter extends ToStringPrinter {

    @Override
    public boolean supports(Class<?> clazz) {
        if (clazz.isPrimitive()) {
            return true;
        }
        if (clazz.isInterface()) {
            return clazz.isAnnotation();
        }
        if (clazz.isEnum()) {
            return true;
        }
        return checkToStringIsOverriden(clazz);
    }

    private boolean checkToStringIsOverriden(Class<?> clazz) {
        try {
            // getMethod will traverse the entire hierarchy of superclasses on its own
            return clazz.getMethod("toString").getDeclaringClass() != Object.class;
        } catch (NoSuchMethodException e) {
            // non-interface class must have toString method
            throw new AssertionError(e);
        }
    }

}
