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

import org.springframework.context.support.GenericApplicationContext;
import ru.tinkoff.eclair.logger.EclairLogger;

import java.util.HashSet;
import java.util.Set;

import static java.lang.String.format;
import static java.util.Arrays.asList;

/**
 * @author Vyacheslav Klapatnyuk
 */
public final class LoggerBeanNamesResolver {

    private static final LoggerBeanNamesResolver instance = new LoggerBeanNamesResolver();

    private LoggerBeanNamesResolver() {
    }

    public static LoggerBeanNamesResolver getInstance() {
        return instance;
    }

    /**
     * Resolve all possible aliases in application context for usage in annotation to specify given bean.
     * Note: primary bean has also '' (empty string) alias.
     * For example:
     * 'jaxb2Printer': {'jaxb2Printer', '', 'jaxb', 'xml'} // primary bean
     * 'jacksonPrinter': {'jacksonPrinter'} // bean without any aliases
     *
     * @param beanName resolvable bean name
     * @return resolved names and aliases, including bean name given by parameter
     */
    public Set<String> resolve(GenericApplicationContext applicationContext, String beanName) {
        String[] beanNamesForType = applicationContext.getBeanNamesForType(EclairLogger.class);
        if (!asList(beanNamesForType).contains(beanName)) {
            throw new IllegalArgumentException(format("EclairLogger '%s' not found", beanName));
        }
        Set<String> result = new HashSet<>(asList(applicationContext.getAliases(beanName)));
        result.add(beanName);
        if (beanNamesForType.length == 1 || applicationContext.getBeanDefinition(beanName).isPrimary()) {
            result.add("");
        }
        return result;
    }
}
