package ru.tinkoff.eclair.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.SpelCompilerMode;
import org.springframework.expression.spel.SpelParserConfiguration;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import ru.tinkoff.eclair.aop.EclairProxyCreator;
import ru.tinkoff.eclair.core.AnnotationDefinitionFactory;
import ru.tinkoff.eclair.core.ExpressionEvaluator;
import ru.tinkoff.eclair.core.LoggerMapSorter;
import ru.tinkoff.eclair.core.PrinterResolver;
import ru.tinkoff.eclair.logger.EclairLogger;
import ru.tinkoff.eclair.logger.SimpleLogger;
import ru.tinkoff.eclair.printer.*;
import ru.tinkoff.eclair.printer.processor.JaxbElementWrapper;
import ru.tinkoff.eclair.validate.BeanClassValidator;

import java.util.List;
import java.util.Map;

/**
 * @author Viacheslav Klapatniuk
 */
@Configuration
@EnableConfigurationProperties(EclairProperties.class)
@ComponentScan("ru.tinkoff.eclair")
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
    public EclairLogger eclairLogger() {
        return new SimpleLogger();
    }

    @Bean
    @ConditionalOnMissingBean
    public ExpressionEvaluator expressionEvaluator(GenericApplicationContext applicationContext) {
        ExpressionParser expressionParser = new SpelExpressionParser(new SpelParserConfiguration(SpelCompilerMode.MIXED, null));
        StandardEvaluationContext evaluationContext = new StandardEvaluationContext();
        evaluationContext.setBeanResolver(new BeanFactoryResolver(applicationContext));
        return new ExpressionEvaluator(expressionParser, evaluationContext);
    }

    @Bean
    public EclairProxyCreator eclairProxyCreator(List<Printer> printerList,
                                                 Map<String, EclairLogger> loggerMap,
                                                 GenericApplicationContext applicationContext,
                                                 BeanClassValidator beanClassValidator,
                                                 EclairProperties eclairProperties,
                                                 ExpressionEvaluator expressionEvaluator) {
        PrinterResolver printerResolver = new PrinterResolver(applicationContext, printerList);
        AnnotationDefinitionFactory annotationDefinitionFactory = new AnnotationDefinitionFactory(printerResolver);
        Map<String, EclairLogger> loggers = new LoggerMapSorter().sort(loggerMap);

        EclairProxyCreator eclairProxyCreator =
                new EclairProxyCreator(applicationContext, annotationDefinitionFactory, loggers, beanClassValidator, expressionEvaluator);
        eclairProxyCreator.setOrder(Ordered.HIGHEST_PRECEDENCE);
        eclairProxyCreator.setFrozen(false);
        eclairProxyCreator.setValidate(eclairProperties.isValidate());
        return eclairProxyCreator;
    }
}
