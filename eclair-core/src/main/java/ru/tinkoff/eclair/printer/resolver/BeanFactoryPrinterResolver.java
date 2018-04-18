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

import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.util.StringUtils;
import ru.tinkoff.eclair.core.BeanFactoryHelper;
import ru.tinkoff.eclair.printer.Printer;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toList;

/**
 * @author Vyacheslav Klapatnyuk
 */
public class BeanFactoryPrinterResolver implements PrinterResolver {

    private static final BeanFactoryHelper beanFactoryHelper = BeanFactoryHelper.getInstance();

    private final Map<String, Printer> printers;
    private final Map<String, String> aliases;

    /**
     * @param orderedPrinters Printers in priority order.
     *                        This order is used when determining a suitable printer that is not explicitly specified.
     */
    public BeanFactoryPrinterResolver(ListableBeanFactory beanFactory, List<Printer> orderedPrinters) {
        this.printers = beanFactoryHelper.collectToOrderedMap(beanFactory, Printer.class, orderedPrinters);
        this.aliases = beanFactoryHelper.getAliases(beanFactory, Printer.class);
    }

    /**
     * Try to resolve {@link Printer} by name or alias.
     */
    @Override
    public Printer resolve(String name) {
        Printer printer = printers.get(name);
        return nonNull(printer) ? printer : printers.get(aliases.get(name));
    }

    /**
     * Try to resolve supported {@link Printer} by name or alias or printable parameter's class.
     *
     * @see Printer#supports(Class)
     */
    @Override
    public Printer resolve(String name, Class<?> printableClass) {
        if (StringUtils.hasText(name)) {
            Printer printer = resolve(name);
            if (nonNull(printer) && printer.supports(printableClass)) {
                return printer;
            }
        }
        return resolve(printableClass);
    }

    /**
     * Try to resolve supported {@link Printer} by name or alias or printable parameter's class for each parameter.
     *
     * @see Printer#supports(Class)
     */
    @Override
    public List<Printer> resolve(String name, Class<?>[] printableClasses) {
        Printer printer = resolve(name);
        if (nonNull(printer)) {
            return Stream.of(printableClasses)
                    .map(printableClass -> printer.supports(printableClass) ? printer : resolve(printableClass))
                    .collect(toList());
        }
        return Stream.of(printableClasses)
                .map(this::resolve)
                .collect(toList());
    }

    /**
     * Try to resolve supported {@link Printer} by printable parameter's class.
     *
     * @see Printer#supports(Class)
     */
    private Printer resolve(Class<?> printableClass) {
        return printers.values().stream()
                .filter(item -> item.supports(printableClass))
                .findFirst()
                .orElse(null);
    }

    public void putAlias(String alias, String original) {
        if (printers.containsKey(original)) {
            aliases.put(alias, original);
        }
    }
}
