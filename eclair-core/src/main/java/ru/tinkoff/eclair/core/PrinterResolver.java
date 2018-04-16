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

import org.springframework.beans.factory.ListableBeanFactory;
import ru.tinkoff.eclair.printer.Printer;
import ru.tinkoff.eclair.printer.ToStringPrinter;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.Collections.nCopies;
import static java.util.Objects.isNull;
import static java.util.stream.Collectors.toList;
import static org.springframework.util.StringUtils.hasText;

/**
 * @author Vyacheslav Klapatnyuk
 */
public class PrinterResolver {

    public static final Printer defaultPrinter = new ToStringPrinter();

    private static final BeanFactoryHelper beanFactoryHelper = BeanFactoryHelper.getInstance();

    private final Map<String, Printer> printers;
    private final Map<String, String> aliases;

    /**
     * @param orderedPrinters Printers in priority order.
     *                        This order is used when determining a suitable printer that is not explicitly specified.
     */
    public PrinterResolver(ListableBeanFactory beanFactory, List<Printer> orderedPrinters) {
        this.printers = beanFactoryHelper.collectToOrderedMap(beanFactory, Printer.class, orderedPrinters);
        this.aliases = beanFactoryHelper.getAliases(beanFactory, Printer.class);
    }

    List<Printer> resolve(String printerName, Class<?>[] parameterTypes) {
        if (hasText(printerName)) {
            return nCopies(parameterTypes.length, resolve(printerName));
        }
        return Stream.of(parameterTypes).map(this::resolve).collect(toList());
    }

    Printer resolve(String printerName, Class<?> parameterType) {
        return hasText(printerName) ? resolve(printerName) : resolve(parameterType);
    }

    private Printer resolve(Class<?> parameterType) {
        return printers.values().stream()
                .filter(item -> item.supports(parameterType))
                .findFirst()
                .orElse(defaultPrinter);
    }

    private Printer resolve(String printerName) {
        Printer printer = printers.get(printerName);
        return isNull(printer) ? printers.getOrDefault(aliases.get(printerName), defaultPrinter) : printer;
    }

    public Map<String, Printer> getPrinters() {
        return printers;
    }

    public Map<String, String> getAliases() {
        return aliases;
    }
}
