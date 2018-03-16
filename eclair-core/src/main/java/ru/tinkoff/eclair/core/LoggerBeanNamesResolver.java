package ru.tinkoff.eclair.core;

import org.springframework.context.support.GenericApplicationContext;
import ru.tinkoff.eclair.logger.Logger;

import java.util.HashSet;
import java.util.Set;

import static java.lang.String.format;
import static java.util.Arrays.asList;

/**
 * @author Viacheslav Klapatniuk
 */
public final class LoggerBeanNamesResolver {

    private static final LoggerBeanNamesResolver instance = new LoggerBeanNamesResolver();

    private LoggerBeanNamesResolver() {
    }

    public static LoggerBeanNamesResolver getInstance() {
        return instance;
    }

    public Set<String> resolve(GenericApplicationContext applicationContext, String beanName) {
        String[] beanNamesForType = applicationContext.getBeanNamesForType(Logger.class);
        if (!asList(beanNamesForType).contains(beanName)) {
            throw new IllegalArgumentException(format("Logger '%s' not found", beanName));
        }
        Set<String> result = new HashSet<>(asList(applicationContext.getAliases(beanName)));
        result.add(beanName);
        if (beanNamesForType.length == 1 || applicationContext.getBeanDefinition(beanName).isPrimary()) {
            result.add("");
        }
        return result;
    }
}
