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
import ru.tinkoff.eclair.printer.ToStringPrinter;

import java.util.List;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

/**
 * @author Vyacheslav Klapatnyuk
 */
public interface PrinterResolver {

    Printer defaultPrinter = new ToStringPrinter();

    Printer resolve(String name);

    Printer resolve(String name, Class<?> printableClass);

    default Printer resolveOrDefault(String name, Class<?> printableClass) {
        return ofNullable(resolve(name, printableClass)).orElse(defaultPrinter);
    }

    List<Printer> resolve(String name, Class<?>[] printableClasses);

    default List<Printer> resolveOrDefault(String name, Class<?>[] printableClasses) {
        List<Printer> resolve = resolve(name, printableClasses);
        return resolve.stream()
                .map(printer -> ofNullable(printer).orElse(defaultPrinter))
                .collect(toList());
    }
}
