package ru.tinkoff.integration.eclair.autoconfigure;

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
import org.springframework.core.annotation.Order;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import ru.tinkoff.integration.eclair.configuration.LogProperties;
import ru.tinkoff.integration.eclair.format.printer.JacksonPrinter;
import ru.tinkoff.integration.eclair.format.printer.Jaxb2Printer;
import ru.tinkoff.integration.eclair.format.printer.OverriddenToStringPrinter;
import ru.tinkoff.integration.eclair.format.printer.ToStringPrinter;
import ru.tinkoff.integration.eclair.logger.SimpleLogger;
import ru.tinkoff.integration.eclair.logger.facade.JavaLoggerFacadeFactory;
import ru.tinkoff.integration.eclair.logger.facade.LoggerFacadeFactory;
import ru.tinkoff.integration.eclair.logger.facade.Slf4JLoggerFacadeFactory;
import ru.tinkoff.integration.eclair.mask.DoUndoMasker;
import ru.tinkoff.integration.eclair.mask.MaskRegistry;
import ru.tinkoff.integration.eclair.mask.Masker;

/**
 * TODO: extract to 'starter'
 * TODO: add test to Printer ordering
 *
 * @author Viacheslav Klapatniuk
 */
@Configuration
@EnableConfigurationProperties(LogProperties.class)
public class LogAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @Order(0)
    public OverriddenToStringPrinter overriddenToStringPrinter() {
        return new OverriddenToStringPrinter();
    }

    @Bean
    @ConditionalOnClass(Jaxb2Marshaller.class)
    @ConditionalOnMissingBean
    @ConditionalOnSingleCandidate(Jaxb2Marshaller.class)
    @Order(100)
    public Jaxb2Printer jaxb2Printer(Jaxb2Marshaller jaxb2Marshaller) {
        return new Jaxb2Printer(jaxb2Marshaller);
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
    @ConditionalOnMissingBean
    @Order(300)
    public ToStringPrinter toStringPrinter() {
        return new ToStringPrinter();
    }

    @Bean
    @ConditionalOnMissingBean
    public LoggerFacadeFactory loggerFactory() {
        LoggingSystem loggingSystem = LoggingSystem.get(LogAutoConfiguration.class.getClassLoader());
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
    public Masker masker() {
        return new DoUndoMasker(new MaskRegistry());
    }

    @Bean
    @ConditionalOnMissingBean
    public SimpleLogger simpleLogger(Masker masker,
                                     LoggerFacadeFactory loggerFacadeFactory,
                                     LogProperties logProperties) {
        return new SimpleLogger(masker, loggerFacadeFactory, logProperties);
    }
}
