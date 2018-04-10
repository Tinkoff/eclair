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

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.ListableBeanFactory;
import ru.tinkoff.eclair.printer.Printer;
import ru.tinkoff.eclair.printer.ToStringPrinter;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.lang.String.*;
import static java.util.Objects.isNull;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static org.springframework.util.StringUtils.hasText;

/**
 * @author Viacheslav Klapatniuk
 */
public class PrinterResolver {

    public static final Printer defaultPrinter = new ToStringPrinter();

    private final Map<String, Printer> printers;
    private final Map<String, String> aliases;

    public PrinterResolver(ListableBeanFactory beanFactory, List<Printer> printerList) {
        Map<String, Printer> printerMap = beanFactory.getBeansOfType(Printer.class);
        this.printers = initPrinters(printerMap, printerList);
        this.aliases = initAliases(beanFactory, printerMap);
    }

    private Map<String, Printer> initPrinters(Map<String, Printer> printerMap, List<Printer> printerList) {
        return printerList.stream()
                .collect(toMap(
                        printer -> printerMap.entrySet().stream()
                                .filter(entry -> entry.getValue().equals(printer))
                                .findFirst()
                                .map(Map.Entry::getKey)
                                .orElseThrow(() -> new IllegalArgumentException("Printer bean not found on map")),
                        identity(),
                        (printer, printer2) -> {
                            throw new IllegalArgumentException(format("Printer names not equals: %s, %s", printer, printer2));
                        },
                        LinkedHashMap::new
                ));
    }

    private Map<String, String> initAliases(BeanFactory beanFactory, Map<String, Printer> printers) {
        return printers.keySet().stream()
                .map(name -> Stream.of(beanFactory.getAliases(name)).collect(toMap(identity(), alias -> name)).entrySet())
                .flatMap(Collection::stream)
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    Printer resolve(String printerName, Class<?> parameterType) {
        if (hasText(printerName)) {
            Printer printer = printers.get(printerName);
            return isNull(printer) ? printers.getOrDefault(aliases.get(printerName), defaultPrinter) : printer;
        }
        return printers.values().stream()
                .filter(item -> item.supports(parameterType))
                .findFirst()
                .orElse(defaultPrinter);
    }

    Map<String, Printer> getPrinters() {
        return printers;
    }

    Map<String, String> getAliases() {
        return aliases;
    }
}
