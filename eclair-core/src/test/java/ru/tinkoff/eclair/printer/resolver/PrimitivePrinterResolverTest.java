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

package ru.tinkoff.eclair.printer.resolver;

import org.junit.Test;
import ru.tinkoff.eclair.printer.Printer;
import ru.tinkoff.eclair.printer.ToStringPrinter;

import java.util.List;

import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * @author Vyacheslav Klapatnyuk
 */
public class PrimitivePrinterResolverTest {

    private final PrinterResolver defaultPrinterResolver = new PrimitivePrinterResolver();
    private final ToStringPrinter printer = new ToStringPrinter();
    private final PrinterResolver printerResolver = new PrimitivePrinterResolver(printer);

    @Test
    public void resolveDefault() {
        // given
        String name = "any";
        // when
        Printer resolvedPrinter = defaultPrinterResolver.resolve(name);
        // then
        assertThat(resolvedPrinter, is(PrinterResolver.defaultPrinter));
    }

    @Test
    public void resolveByName() {
        // given
        String name = "any";
        // when
        Printer resolvedPrinter = printerResolver.resolve(name);
        // then
        assertThat(resolvedPrinter, is(printer));
    }

    @Test
    public void resolveByNameAndPrintableClass() {
        // given
        String name = "any";
        Class<?> printableClass = Object.class;
        // when
        Printer resolvedPrinter = printerResolver.resolve(name, printableClass);
        // then
        assertThat(resolvedPrinter, is(printer));
    }

    @Test
    public void resolveByNameAndPrintableClasses() {
        // given
        String name = "any";
        Class<?>[] printableClasses = new Class[]{Object.class, Object.class};
        // when
        List<Printer> resolvePrinters = printerResolver.resolve(name, printableClasses);
        // then
        assertThat(resolvePrinters, hasSize(2));
        assertThat(resolvePrinters.get(0), is(printer));
    }
}
