package ru.tinkoff.eclair.core;

import org.junit.Test;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.context.support.StaticApplicationContext;
import ru.tinkoff.eclair.logger.SimpleLogger;

import java.util.Set;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

/**
 * @author Viacheslav Klapatniuk
 */
public class LoggerBeanNamesResolverTest {

    private final LoggerBeanNamesResolver loggerBeanNamesResolver = LoggerBeanNamesResolver.getInstance();

    @Test
    public void resolve() {
        // given
        String beanName = "simpleLogger";
        String emptyLoggerName = "";
        String alias = "logger";
        GenericApplicationContext applicationContext = new StaticApplicationContext();
        applicationContext.registerBeanDefinition(beanName, givenLoggerBeanDefinition());
        applicationContext.getBeanFactory().registerAlias(beanName, alias);
        // when
        Set<String> namesByBeanName = loggerBeanNamesResolver.resolve(applicationContext, beanName);
        // then
        assertThat(namesByBeanName, hasSize(3));
        assertThat(namesByBeanName, containsInAnyOrder(beanName, emptyLoggerName, alias));
    }

    @Test(expected = IllegalArgumentException.class)
    public void resolveByAlias() {
        // given
        String beanName = "simpleLogger";
        String alias = "logger";
        GenericApplicationContext applicationContext = new StaticApplicationContext();
        applicationContext.registerBeanDefinition(beanName, givenLoggerBeanDefinition());
        applicationContext.getBeanFactory().registerAlias(beanName, alias);
        // when
        loggerBeanNamesResolver.resolve(applicationContext, alias);
        // then expected exception
    }

    @Test(expected = IllegalArgumentException.class)
    public void resolveByUnknownBeanName() {
        // given
        String beanName = "simpleLogger";
        String alias = "logger";
        GenericApplicationContext applicationContext = new StaticApplicationContext();
        applicationContext.registerBeanDefinition(beanName, givenLoggerBeanDefinition());
        applicationContext.getBeanFactory().registerAlias(beanName, alias);
        // when
        loggerBeanNamesResolver.resolve(applicationContext, "unknown");
        // then expected exception
    }

    @Test(expected = IllegalArgumentException.class)
    public void resolveByWrongBeanTypeName() {
        // given
        String beanName = "simpleLogger";
        String alias = "logger";
        String wrongBeanTypeName = "string";
        GenericApplicationContext applicationContext = new StaticApplicationContext();
        applicationContext.registerBeanDefinition(beanName, givenLoggerBeanDefinition());
        applicationContext.getBeanFactory().registerAlias(beanName, alias);
        applicationContext.registerBeanDefinition(wrongBeanTypeName, givenStringBeanDefinition());
        // when
        loggerBeanNamesResolver.resolve(applicationContext, wrongBeanTypeName);
        // then expected exception
    }

    @Test
    public void resolveManyLoggers() {
        // given
        String beanName = "simpleLogger";
        String alias = "logger";
        String beanName2 = "simpleLogger2";
        String alias2 = "logger2";

        GenericApplicationContext applicationContext = new StaticApplicationContext();
        applicationContext.registerBeanDefinition(beanName, givenLoggerBeanDefinition());
        applicationContext.getBeanFactory().registerAlias(beanName, alias);
        applicationContext.registerBeanDefinition(beanName2, givenLoggerBeanDefinition());
        applicationContext.getBeanFactory().registerAlias(beanName2, alias2);
        // when
        Set<String> namesByBeanName = loggerBeanNamesResolver.resolve(applicationContext, beanName);
        // then
        assertThat(namesByBeanName, hasSize(2));
        assertThat(namesByBeanName, containsInAnyOrder(beanName, alias));
    }

    @Test
    public void resolveManyLoggersWithPrimary() {
        // given
        String beanName = "simpleLogger";
        String emptyLoggerName = "";
        String alias = "logger";
        String beanName2 = "simpleLogger2";
        String alias2 = "logger2";
        AbstractBeanDefinition primaryDefinition = givenLoggerBeanDefinition();
        primaryDefinition.setPrimary(true);
        GenericApplicationContext applicationContext = new StaticApplicationContext();
        applicationContext.registerBeanDefinition(beanName, primaryDefinition);
        applicationContext.getBeanFactory().registerAlias(beanName, alias);
        applicationContext.registerBeanDefinition(beanName2, givenLoggerBeanDefinition());
        applicationContext.getBeanFactory().registerAlias(beanName2, alias2);
        // when
        Set<String> namesByBeanName = loggerBeanNamesResolver.resolve(applicationContext, beanName);
        // then
        assertThat(namesByBeanName, hasSize(3));
        assertThat(namesByBeanName, containsInAnyOrder(beanName, emptyLoggerName, alias));
    }

    private AbstractBeanDefinition givenLoggerBeanDefinition() {
        return BeanDefinitionBuilder.genericBeanDefinition(SimpleLogger.class)
                .addConstructorArgValue(null)
                .addConstructorArgValue(null)
                .addConstructorArgValue(null)
                .addConstructorArgValue(null)
                .getBeanDefinition();
    }

    private AbstractBeanDefinition givenStringBeanDefinition() {
        return BeanDefinitionBuilder.genericBeanDefinition(String.class)
                .addConstructorArgValue("string")
                .getBeanDefinition();
    }
}
