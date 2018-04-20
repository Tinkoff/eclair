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

import ru.tinkoff.eclair.printer.Printer;

import java.util.List;

import static java.util.Collections.nCopies;

/**
 * @author Vyacheslav Klapatnyuk
 */
public class PrimitivePrinterResolver implements PrinterResolver {

    private final Printer printer;

    public PrimitivePrinterResolver() {
        this(defaultPrinter);
    }

    public PrimitivePrinterResolver(Printer printer) {
        this.printer = printer;
    }

    @Override
    public Printer resolve(String name) {
        return printer;
    }

    @Override
    public Printer resolve(String name, Class<?> printableClass) {
        return printer;
    }

    @Override
    public List<Printer> resolve(String name, Class<?>[] printableClasses) {
        return nCopies(printableClasses.length, printer);
    }
}
