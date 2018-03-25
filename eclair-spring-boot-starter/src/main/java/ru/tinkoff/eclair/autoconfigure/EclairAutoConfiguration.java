package ru.tinkoff.eclair.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.logging.LoggingSystem;
import org.springframework.boot.logging.Slf4JLoggingSystem;
import org.springframework.boot.logging.java.JavaLoggingSystem;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import ru.tinkoff.eclair.aop.EclairProxyCreator;
import ru.tinkoff.eclair.logger.EclairLogger;
import ru.tinkoff.eclair.logger.SimpleLogger;
import ru.tinkoff.eclair.logger.facade.JavaLoggerFacadeFactory;
import ru.tinkoff.eclair.logger.facade.LoggerFacadeFactory;
import ru.tinkoff.eclair.logger.facade.Slf4JLoggerFacadeFactory;
import ru.tinkoff.eclair.printer.*;
import ru.tinkoff.eclair.printer.processor.JaxbElementWrapper;
import ru.tinkoff.eclair.validate.BeanClassValidator;

import java.util.List;
import java.util.Map;

/**
 * TODO: add test to Printer ordering
 *
 * @author Viacheslav Klapatniuk
 */
@Configuration
@EnableConfigurationProperties(EclairProperties.class)
public class EclairAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @Order(0)
    public OverriddenToStringPrinter overriddenToStringPrinter() {
        return new OverriddenToStringPrinter();
    }

    @Bean
    @ConditionalOnClass(Jaxb2Marshaller.class)
    @ConditionalOnMissingBean(Jaxb2Printer.class)
    @ConditionalOnSingleCandidate(Jaxb2Marshaller.class)
    @Order(100)
    public Printer jaxb2Printer(Jaxb2Marshaller jaxb2Marshaller) {
        return new Jaxb2Printer(jaxb2Marshaller)
                .addPreProcessor(new JaxbElementWrapper(jaxb2Marshaller));
    }

    @Bean
    @ConditionalOnClass(ObjectMapper.class)
    @ConditionalOnSingleCandidate(ObjectMapper.class)
    @ConditionalOnMissingBean
    @Order(200)
    public JacksonPrinter jacksonPrinter(ObjectMapper objectMapper) {
        return new JacksonPrinter(objectMapper);
    }

    @Bean
    @ConditionalOnMissingBean(ignored = OverriddenToStringPrinter.class)
    @Order(300)
    public ToStringPrinter toStringPrinter() {
        return new ToStringPrinter();
    }

    @Bean
    @ConditionalOnMissingBean
    public LoggerFacadeFactory loggerFactory() {
        LoggingSystem loggingSystem = LoggingSystem.get(EclairAutoConfiguration.class.getClassLoader());
        if (loggingSystem instanceof Slf4JLoggingSystem) {
            return new Slf4JLoggerFacadeFactory();
        }
        if (loggingSystem instanceof JavaLoggingSystem) {
            return new JavaLoggerFacadeFactory();
        }
        throw new IllegalStateException("No suitable logging system");
    }

    @Bean
    @ConditionalOnMissingBean
    public EclairLogger eclairLogger(LoggerFacadeFactory loggerFacadeFactory) {
        return new SimpleLogger(loggerFacadeFactory);
    }

    @Bean
    public EclairProxyCreator eclairProxyCreator(List<Printer> printerList,
                                                 Map<String, EclairLogger> loggers,
                                                 GenericApplicationContext genericApplicationContext,
                                                 BeanClassValidator beanClassValidator,
                                                 EclairProperties eclairProperties) {
        EclairProxyCreator eclairProxyCreator = new EclairProxyCreator(printerList, loggers, genericApplicationContext, beanClassValidator);
        eclairProxyCreator.setOrder(Ordered.HIGHEST_PRECEDENCE);
        eclairProxyCreator.setFrozen(false);
        eclairProxyCreator.setValidate(eclairProperties.isValidate());
        return eclairProxyCreator;
    }
}
