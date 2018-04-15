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

import org.junit.Test;
import org.springframework.boot.logging.LogLevel;

import java.io.Serializable;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Vyacheslav Klapatnyuk
 */
public class OverriddenToStringPrinterTest {

    private final OverriddenToStringPrinter printer = new OverriddenToStringPrinter();

    @Test
    public void supportsPrimitives() {
        // given
        Class<?> booleanClass = boolean.class;
        Class<?> byteClass = byte.class;
        Class<?> shortClass = short.class;
        Class<?> charClass = char.class;
        Class<?> intClass = int.class;
        Class<?> longClass = long.class;
        Class<?> floatClass = float.class;
        Class<?> doubleClass = double.class;
        // when
        boolean supportsBoolean = printer.supports(booleanClass);
        boolean supportsByte = printer.supports(byteClass);
        boolean supportsShort = printer.supports(shortClass);
        boolean supportsChar = printer.supports(charClass);
        boolean supportsInt = printer.supports(intClass);
        boolean supportsLong = printer.supports(longClass);
        boolean supportsFloat = printer.supports(floatClass);
        boolean supportsDouble = printer.supports(doubleClass);
        // then
        assertTrue(supportsBoolean);
        assertTrue(supportsByte);
        assertTrue(supportsShort);
        assertTrue(supportsChar);
        assertTrue(supportsInt);
        assertTrue(supportsLong);
        assertTrue(supportsFloat);
        assertTrue(supportsDouble);
    }

    @Test
    public void supportsPrimitiveWrappers() {
        // given
        Class<?> booleanClass = Boolean.class;
        Class<?> byteClass = Byte.class;
        Class<?> shortClass = Short.class;
        Class<?> charClass = Character.class;
        Class<?> intClass = Integer.class;
        Class<?> longClass = Long.class;
        Class<?> floatClass = Float.class;
        Class<?> doubleClass = Double.class;
        // when
        boolean supportsBoolean = printer.supports(booleanClass);
        boolean supportsByte = printer.supports(byteClass);
        boolean supportsShort = printer.supports(shortClass);
        boolean supportsChar = printer.supports(charClass);
        boolean supportsInt = printer.supports(intClass);
        boolean supportsLong = printer.supports(longClass);
        boolean supportsFloat = printer.supports(floatClass);
        boolean supportsDouble = printer.supports(doubleClass);
        // then
        assertTrue(supportsBoolean);
        assertTrue(supportsByte);
        assertTrue(supportsShort);
        assertTrue(supportsChar);
        assertTrue(supportsInt);
        assertTrue(supportsLong);
        assertTrue(supportsFloat);
        assertTrue(supportsDouble);
    }

    @Test
    public void supportsVoid() {
        // given
        Class<?> clazz = void.class;
        // when
        boolean supports = printer.supports(clazz);
        // then
        assertTrue(supports);
    }

    @Test
    public void supportsVoidObject() {
        // given
        Class<?> clazz = Void.class;
        // when
        boolean supports = printer.supports(clazz);
        // then
        assertFalse(supports);
    }

    @Test
    public void supportsString() {
        // given
        Class<?> clazz = String.class;
        // when
        boolean supports = printer.supports(clazz);
        // then
        assertTrue(supports);
    }

    @Test
    public void supportsObject() {
        // given
        Class<?> clazz = Object.class;
        // when
        boolean supports = printer.supports(clazz);
        // then
        assertFalse(supports);
    }

    @Test
    public void supportsInterface() {
        // given
        Class<?> clazz = Serializable.class;
        // when
        boolean supports = printer.supports(clazz);
        // then
        assertFalse(supports);
    }

    @Test
    public void supportsEnum() {
        // given
        Class<?> clazz = LogLevel.class;
        // when
        boolean supports = printer.supports(clazz);
        // then
        assertTrue(supports);
    }

    @Test
    public void supportsArray() {
        // given
        Class<?> clazz = String[].class;
        // when
        boolean supports = printer.supports(clazz);
        // then
        assertFalse(supports);
    }

    @Test
    public void supportsAbstractClass() {
        // given
        Class<?> clazz = AbstractClass.class;
        // when
        boolean supports = printer.supports(clazz);
        // then
        assertTrue(supports);
    }

    private static class AbstractClass {

        @Override
        public String toString() {
            return super.toString();
        }
    }

    @Test
    public void supportsAnnotation() throws NoSuchMethodException {
        // given
        Class<?> clazz = Test.class;
        // when
        boolean supports = printer.supports(clazz);
        // then
        assertTrue(supports);
    }
}
