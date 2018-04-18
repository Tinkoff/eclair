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
import org.springframework.beans.factory.ObjectProvider;
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
import ru.tinkoff.eclair.printer.resolver.BeanFactoryPrinterResolver;
import ru.tinkoff.eclair.printer.resolver.PrinterResolver;
import ru.tinkoff.eclair.logger.EclairLogger;
import ru.tinkoff.eclair.logger.SimpleLogger;
import ru.tinkoff.eclair.printer.*;
import ru.tinkoff.eclair.printer.processor.JaxbElementWrapper;

import java.util.List;

import static java.util.Collections.singletonList;
import static java.util.Objects.isNull;

/**
 * @author Vyacheslav Klapatnyuk
 */
@Configuration
@EnableConfigurationProperties(EclairProperties.class)
public class EclairAutoConfiguration {

    private final GenericApplicationContext applicationContext;

    public EclairAutoConfiguration(GenericApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Bean
    @ConditionalOnMissingBean
    public EclairLogger simpleLogger() {
        return new SimpleLogger();
    }

    @Bean
    @ConditionalOnMissingBean
    public ExpressionEvaluator expressionEvaluator() {
        ExpressionParser expressionParser = new SpelExpressionParser(new SpelParserConfiguration(SpelCompilerMode.MIXED, null));
        StandardEvaluationContext evaluationContext = new StandardEvaluationContext();
        evaluationContext.setBeanResolver(new BeanFactoryResolver(applicationContext));
        return new ExpressionEvaluator(expressionParser, evaluationContext);
    }

    @Bean
    public EclairProxyCreator eclairProxyCreator(PrinterResolver printerResolver,
                                                 List<EclairLogger> orderedLoggers,
                                                 EclairProperties eclairProperties,
                                                 ExpressionEvaluator expressionEvaluator) {
        AnnotationDefinitionFactory annotationDefinitionFactory = new AnnotationDefinitionFactory(printerResolver);
        EclairProxyCreator eclairProxyCreator =
                new EclairProxyCreator(applicationContext, annotationDefinitionFactory, orderedLoggers, expressionEvaluator, printerResolver);
        eclairProxyCreator.setOrder(Ordered.HIGHEST_PRECEDENCE);
        eclairProxyCreator.setFrozen(false);
        eclairProxyCreator.setValidate(eclairProperties.isValidate());
        return eclairProxyCreator;
    }

    @Configuration
    static class PrinterConfiguration {

        private static final String XML_AUTO_CONFIGURED_PRINTER_NAME = "jaxb2Printer";
        private static final String JSON_AUTO_CONFIGURED_PRINTER_NAME = "jacksonPrinter";

        @Bean
        @ConditionalOnMissingBean
        @Order(0)
        public OverriddenToStringPrinter overriddenToStringPrinter() {
            return new OverriddenToStringPrinter();
        }

        @Bean(XML_AUTO_CONFIGURED_PRINTER_NAME)
        @ConditionalOnClass(Jaxb2Marshaller.class)
        @ConditionalOnSingleCandidate(Jaxb2Marshaller.class)
        @ConditionalOnMissingBean(Jaxb2Printer.class)
        @Order(100)
        public Printer jaxb2Printer(ObjectProvider<Jaxb2Marshaller> jaxb2Marshaller) {
            Jaxb2Marshaller marshaller = jaxb2Marshaller.getObject();
            return new Jaxb2Printer(marshaller)
                    .addPreProcessor(new JaxbElementWrapper(marshaller));
        }

        @Bean(JSON_AUTO_CONFIGURED_PRINTER_NAME)
        @ConditionalOnClass(ObjectMapper.class)
        @ConditionalOnSingleCandidate(ObjectMapper.class)
        @ConditionalOnMissingBean
        @Order(200)
        public JacksonPrinter jacksonPrinter(ObjectProvider<ObjectMapper> objectMapper) {
            return new JacksonPrinter(objectMapper.getObject());
        }

        @Bean
        @ConditionalOnMissingBean(ignored = OverriddenToStringPrinter.class)
        @Order(300)
        public ToStringPrinter toStringPrinter() {
            return new ToStringPrinter();
        }

        @Bean
        @ConditionalOnMissingBean
        public PrinterResolver printerResolver(GenericApplicationContext applicationContext,
                                               ObjectProvider<List<Printer>> orderedPrinters) {
            List<Printer> resolvedOrderedPrinters = orderedPrinters.getIfAvailable();
            if (isNull(resolvedOrderedPrinters)) {
                return new BeanFactoryPrinterResolver(applicationContext, singletonList(PrinterResolver.defaultPrinter));
            }
            BeanFactoryPrinterResolver printerResolver = new BeanFactoryPrinterResolver(applicationContext, resolvedOrderedPrinters);
            printerResolver.putAlias("xml", XML_AUTO_CONFIGURED_PRINTER_NAME);
            printerResolver.putAlias("json", JSON_AUTO_CONFIGURED_PRINTER_NAME);
            return printerResolver;
        }
    }
}
