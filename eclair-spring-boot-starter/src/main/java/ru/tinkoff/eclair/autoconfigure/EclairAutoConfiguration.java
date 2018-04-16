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

package ru.tinkoff.eclair.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
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
import ru.tinkoff.eclair.core.PrinterResolver;
import ru.tinkoff.eclair.logger.EclairLogger;
import ru.tinkoff.eclair.logger.SimpleLogger;
import ru.tinkoff.eclair.printer.*;
import ru.tinkoff.eclair.printer.processor.JaxbElementWrapper;

import java.util.List;

/**
 * @author Vyacheslav Klapatnyuk
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
    public EclairLogger simpleLogger() {
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
    public EclairProxyCreator eclairProxyCreator(List<Printer> orderedPrinters,
                                                 List<EclairLogger> orderedLoggers,
                                                 GenericApplicationContext applicationContext,
                                                 EclairProperties eclairProperties,
                                                 ExpressionEvaluator expressionEvaluator) {
        PrinterResolver printerResolver = new PrinterResolver(applicationContext, orderedPrinters);
        AnnotationDefinitionFactory annotationDefinitionFactory = new AnnotationDefinitionFactory(printerResolver);
        EclairProxyCreator eclairProxyCreator =
                new EclairProxyCreator(applicationContext, annotationDefinitionFactory, orderedLoggers, expressionEvaluator, printerResolver);
        eclairProxyCreator.setOrder(Ordered.HIGHEST_PRECEDENCE);
        eclairProxyCreator.setFrozen(false);
        eclairProxyCreator.setValidate(eclairProperties.isValidate());
        return eclairProxyCreator;
    }
}
