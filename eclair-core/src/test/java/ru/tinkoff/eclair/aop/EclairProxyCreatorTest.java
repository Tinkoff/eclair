package ru.tinkoff.eclair.aop;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import ru.tinkoff.eclair.annotation.Log;
import ru.tinkoff.eclair.core.ExpressionEvaluator;
import ru.tinkoff.eclair.logger.SimpleLogger;
import ru.tinkoff.eclair.printer.Printer;
import ru.tinkoff.eclair.printer.ToStringPrinter;
import ru.tinkoff.eclair.validate.BeanClassValidator;
import ru.tinkoff.eclair.validate.BeanMethodValidator;
import ru.tinkoff.eclair.validate.log.group.*;
import ru.tinkoff.eclair.validate.log.single.*;
import ru.tinkoff.eclair.validate.mdc.MdcValidator;
import ru.tinkoff.eclair.validate.mdc.MdcsValidator;
import ru.tinkoff.eclair.validate.mdc.MergedMdcsValidator;

import java.util.Map;

import static org.junit.Assert.assertTrue;

/**
 * @author Viacheslav Klapatniuk
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {
        EclairProxyCreator.class,
        ExpressionEvaluator.class,

        BeanClassValidator.class,
        BeanMethodValidator.class,
        LogsValidator.class,
        LogValidator.class,
        LogInsValidator.class,
        LogInValidator.class,
        LogOutsValidator.class,
        LogOutValidator.class,
        LogErrorsValidator.class,
        LogErrorValidator.class,
        ParameterLogsValidator.class,
        ParameterLogValidator.class,
        MdcsValidator.class,
        MdcValidator.class,
        MergedMdcsValidator.class,

        EclairProxyCreatorTest.InfrastructureTestConfiguration.class,
        EclairProxyCreatorTest.TestConfiguration.class
})
public class EclairProxyCreatorTest {

    @Autowired
    private EclairProxyCreator eclairProxyCreator;

    @Configuration
    public static class InfrastructureTestConfiguration {

        @Bean
        public Printer printer() {
            return new ToStringPrinter();
        }

        @Bean
        public SimpleLogger simpleLogger() {
            return new SimpleLogger();
        }
    }

    @Configuration
    public static class TestConfiguration {

        @Bean
        public Simple simple() {
            return new Simple();
        }

        @Bean
        public Parent parent() {
            return new Child();
        }

        @Bean
        public FactoryBean<Simple> simpleFactoryBean() {
            return new FactoryBean<Simple>() {
                @Override
                public Simple getObject() throws Exception {
                    return new Simple();
                }

                @Override
                public Class<?> getObjectType() {
                    return Simple.class;
                }

                @Override
                public boolean isSingleton() {
                    return true;
                }
            };
        }
    }

    public static class Simple {

        @Log
        public void method() {
        }
    }

    interface Parent {

        @Log
        void method();
    }

    public static class Child implements Parent {

        @Override
        public void method() {
        }
    }

    @Test
    public void getBeanClassCache() {
        // given
        // when
        Map<String, Class<?>> beanClassCache = eclairProxyCreator.getBeanClassCache();
        // then
        assertTrue(beanClassCache.get("eclairProxyCreatorTest.TestConfiguration") == TestConfiguration.class);
        assertTrue(beanClassCache.get("simple") == Simple.class);
        assertTrue(beanClassCache.get("parent") == Child.class);
        assertTrue(beanClassCache.get("simpleFactoryBean") == Simple.class);
    }
}
