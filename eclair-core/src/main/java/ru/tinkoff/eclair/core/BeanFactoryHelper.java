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

import java.util.*;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

/**
 * TODO: refactor
 *
 * @author Vyacheslav Klapatnyuk
 */
public final class BeanFactoryHelper {

    private static final BeanFactoryHelper instance = new BeanFactoryHelper();

    public static BeanFactoryHelper getInstance() {
        return instance;
    }

    public <T> Map<String, T> collectToOrderedMap(ListableBeanFactory beanFactory, Class<T> clazz, List<T> orderedBeans) {
        Map<String, T> beansOfType = beanFactory.getBeansOfType(clazz);
        return orderedBeans.stream()
                .collect(toMap(
                        bean -> buildKey(beansOfType, bean),
                        identity(),
                        this::mergeBeans,
                        LinkedHashMap::new
                ));
    }

    private <T> String buildKey(Map<String, T> beansOfType, T bean) {
        return beansOfType.entrySet().stream()
                .filter(entry -> entry.getValue().equals(bean))
                .findFirst()
                .map(Map.Entry::getKey)
                .orElseThrow(() -> new IllegalArgumentException("Bean not found on map"));
    }

    private <T> T mergeBeans(T bean, T bean2) {
        throw new IllegalArgumentException(format("Bean names not equals: %s, %s", bean, bean2));
    }

    public <T> Map<String, String> getAliases(ListableBeanFactory beanFactory, Class<T> clazz) {
        return beanFactory.getBeansOfType(clazz).keySet().stream()
                .map(name -> getAliasEntrySet(beanFactory, name))
                .flatMap(Collection::stream)
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private Set<Map.Entry<String, String>> getAliasEntrySet(ListableBeanFactory beanFactory, String name) {
        return Stream.of(beanFactory.getAliases(name))
                .collect(toMap(identity(), alias -> name))
                .entrySet();
    }
}
