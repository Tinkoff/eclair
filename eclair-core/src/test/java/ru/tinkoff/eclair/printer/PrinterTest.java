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
import ru.tinkoff.eclair.printer.processor.PrinterPostProcessor;
import ru.tinkoff.eclair.printer.processor.PrinterPreProcessor;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * @author Vyacheslav Klapatnyuk
 */
public class PrinterTest {

    @Test
    public void supports() {
        // given
        Printer printer = new NoOpPrinter();
        Class<?> clazz = Object.class;
        // when
        boolean supports = printer.supports(clazz);
        // then
        assertTrue(supports);
    }

    @Test
    public void print() {
        // given
        Printer printer = new NoOpPrinter();

        PrinterPreProcessor preProcessor = mock(PrinterPreProcessor.class);
        when(preProcessor.process(printer)).thenReturn(printer);

        PrinterPreProcessor preProcessor1 = mock(PrinterPreProcessor.class);
        when(preProcessor1.process(printer)).thenReturn(printer);

        PrinterPostProcessor postProcessor = mock(PrinterPostProcessor.class);
        when(postProcessor.process("")).thenReturn("");

        PrinterPostProcessor postProcessor1 = mock(PrinterPostProcessor.class);
        when(postProcessor1.process("")).thenReturn("");

        printer.addPreProcessor(preProcessor).addPreProcessor(preProcessor1);
        printer.addPostProcessor(postProcessor).addPostProcessor(postProcessor1);
        Object input = new Object();

        // when
        String string = printer.print(input);

        // then
        verify(preProcessor).process(any());
        verify(preProcessor1).process(any());
        verify(postProcessor).process(any());
        verify(postProcessor1).process(any());
        assertThat(string, is(""));
    }

    private static class NoOpPrinter extends Printer {

        @Override
        protected String serialize(Object input) throws RuntimeException {
            return "";
        }
    }
}
