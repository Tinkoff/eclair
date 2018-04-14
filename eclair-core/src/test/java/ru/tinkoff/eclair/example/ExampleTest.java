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

package ru.tinkoff.eclair.example;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.PatternLayout;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.logging.LogLevel;
import org.springframework.boot.logging.LoggingSystem;
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
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import ru.tinkoff.eclair.aop.EclairProxyCreator;
import ru.tinkoff.eclair.core.*;
import ru.tinkoff.eclair.logger.EclairLogger;
import ru.tinkoff.eclair.logger.SimpleLogger;
import ru.tinkoff.eclair.logger.facade.LoggerFacadeFactory;
import ru.tinkoff.eclair.printer.*;
import ru.tinkoff.eclair.printer.processor.JaxbElementWrapper;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.*;
import static org.junit.Assert.assertEquals;
import static org.springframework.boot.logging.LogLevel.values;

/**
 * @author Vyacheslav Klapatnyuk
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = ExampleTest.TestConfiguration.class)
public class ExampleTest {

    private static final LoggingSystem loggingSystem = LoggingSystem.get(ExampleTest.class.getClassLoader());
    private static final ExampleAppender exampleAppender = new ExampleAppender();
    private static final PatternLayout patternLayout = new PatternLayout();
    private static final LoggerFacadeFactory loggerFacadeFactory = new ExampleLoggerFacadeFactory(exampleAppender);
    private static final ExampleTableBuilder exampleTableBuilder = new ExampleTableBuilder();
    private static final LoggerNameBuilder loggerNameBuilder = LoggerNameBuilder.getInstance();
    private static final String ROOT_LOGGER_NAME = "ru.tinkoff.eclair.example.Example";

    private static final String PATTERN = "%-5level [%X] %logger{35} %msg";

    @Autowired
    private Example example;

    @BeforeClass
    public static void init() {
        exampleAppender.start();

        patternLayout.setPattern(PATTERN);
        patternLayout.setContext((LoggerContext) LoggerFactory.getILoggerFactory());
        patternLayout.start();

        exampleTableBuilder.setPatternLayout(patternLayout);
    }

    @Test
    public void simple() throws NoSuchMethodException {
        // given
        // when
        forEachLevel(() -> example.simple());
        // then
        String expected = ExampleTableBuilder.TABLE_HEADER +
                "`TRACE` `DEBUG`|`DEBUG [] r.t.eclair.example.Example.simple >`<br>`DEBUG [] r.t.eclair.example.Example.simple <`\n" +
                "`INFO` .. `OFF`|-";
        String loggerName = loggerNameBuilder.build(Example.class.getMethod("simple"));
        String actual = exampleTableBuilder.buildTable(groupLevels(loggerName));
        System.out.println(actual);
        assertEquals(expected, actual);
    }

    @Test
    public void simpleWithError() throws NoSuchMethodException {
        // given
        // when
        forEachLevel(() -> example.simpleError());
        // then
        String expected = ExampleTableBuilder.TABLE_HEADER +
                "`TRACE` `DEBUG`|`DEBUG [] r.t.e.example.Example.simpleError >`<br>`DEBUG [] r.t.e.example.Example.simpleError !`\n" +
                "`INFO` .. `OFF`|-";
        String loggerName = loggerNameBuilder.build(Example.class.getMethod("simpleError"));
        String actual = exampleTableBuilder.buildTable(groupLevels(loggerName));
        System.out.println(actual);
        assertEquals(expected, actual);
    }

    @Test
    public void level() throws NoSuchMethodException {
        // given
        // when
        forEachLevel(() -> example.level());
        // then
        String expected = ExampleTableBuilder.TABLE_HEADER +
                "`TRACE` .. `INFO`|`INFO  [] r.t.eclair.example.Example.level >`<br>`INFO  [] r.t.eclair.example.Example.level <`\n" +
                "`WARN` .. `OFF`|-";
        String loggerName = loggerNameBuilder.build(Example.class.getMethod("level"));
        String actual = exampleTableBuilder.buildTable(groupLevels(loggerName));
        System.out.println(actual);
        assertEquals(expected, actual);
    }

    @Test
    public void ifEnabled() throws NoSuchMethodException {
        // given
        // when
        forEachLevel(() -> example.ifEnabled());
        // then
        String expected = ExampleTableBuilder.TABLE_HEADER +
                "`TRACE` `DEBUG`|`INFO  [] r.t.e.example.Example.ifEnabled >`<br>`INFO  [] r.t.e.example.Example.ifEnabled <`\n" +
                "`INFO` .. `OFF`|-";
        String loggerName = loggerNameBuilder.build(Example.class.getMethod("ifEnabled"));
        String actual = exampleTableBuilder.buildTable(groupLevels(loggerName));
        System.out.println(actual);
        assertEquals(expected, actual);
    }

    @Test
    public void parameterLevels() throws NoSuchMethodException {
        // given
        // when
        forEachLevel(() -> example.parameterLevels(0.0, "s", 0));
        // then
        String expected = ExampleTableBuilder.TABLE_HEADER +
                "`TRACE`|`INFO  [] r.t.e.e.Example.parameterLevels > d=0.0, s=\"s\", i=0`\n" +
                "`DEBUG`|`INFO  [] r.t.e.e.Example.parameterLevels > d=0.0, s=\"s\"`\n" +
                "`INFO`|`INFO  [] r.t.e.e.Example.parameterLevels > 0.0`\n" +
                "`WARN` .. `OFF`|-";
        String loggerName = loggerNameBuilder.build(Example.class.getMethod("parameterLevels", Double.class, String.class, Integer.class));
        String actual = exampleTableBuilder.buildTable(groupLevels(loggerName));
        System.out.println(actual);
        assertEquals(expected, actual);
    }

    private void forEachLevel(Runnable runnable) {
        Stream.of(values()).forEach(level -> {
            loggingSystem.setLogLevel(ROOT_LOGGER_NAME, level);
            exampleAppender.setLevel(level);
            try {
                runnable.run();
            } catch (Exception e) {
                // do nothing
            }
        });
    }

    private Map<String, List<LogLevel>> groupLevels(String loggerName) {
        return exampleAppender.getLoggerEvents(loggerName).entrySet().stream()
                .collect(groupingBy(
                        o -> exampleTableBuilder.buildSampleCell(o.getValue()),
                        LinkedHashMap::new,
                        mapping(identity(), toList())
                )).entrySet().stream().collect(toMap(
                        Map.Entry::getKey,
                        o -> o.getValue().stream().map(Map.Entry::getKey).collect(toList()),
                        (logLevels, logLevels2) -> {
                            throw new RuntimeException();
                        },
                        LinkedHashMap::new
                ));
    }

    @Configuration
    static class TestConfiguration {

        @Bean
        public Example example() {
            return new Example();
        }

        @Bean
        @Order(0)
        public OverriddenToStringPrinter overriddenToStringPrinter() {
            return new OverriddenToStringPrinter();
        }

        @Bean
        public Jaxb2Marshaller jaxb2Marshaller() {
            Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
            marshaller.setClassesToBeBound(Dto.class);
            return marshaller;
        }

        @Bean
        @Order(100)
        public Printer jaxb2Printer(Jaxb2Marshaller jaxb2Marshaller) {
            return new Jaxb2Printer(jaxb2Marshaller)
                    .addPreProcessor(new JaxbElementWrapper(jaxb2Marshaller));
        }

        @Bean
        public ObjectMapper objectMapper() {
            return new ObjectMapper();
        }

        @Bean
        @Order(200)
        public JacksonPrinter jacksonPrinter(ObjectMapper objectMapper) {
            return new JacksonPrinter(objectMapper);
        }

        @Bean
        @Order(300)
        public ToStringPrinter toStringPrinter() {
            return new ToStringPrinter();
        }

        @Bean
        public EclairLogger eclairLogger() {
            return new SimpleLogger(loggerFacadeFactory, LoggingSystem.get(SimpleLogger.class.getClassLoader()));
        }

        @Bean
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
//                                                     BeanClassValidator beanClassValidator,
                                                     ExpressionEvaluator expressionEvaluator) {
            PrinterResolver printerResolver = new PrinterResolver(applicationContext, printerList);
            AnnotationDefinitionFactory annotationDefinitionFactory = new AnnotationDefinitionFactory(printerResolver);
            Map<String, EclairLogger> loggers = new LoggerMapSorter().sort(loggerMap);

            EclairProxyCreator eclairProxyCreator =
                    new EclairProxyCreator(applicationContext, annotationDefinitionFactory, loggers, /*beanClassValidator, */expressionEvaluator);
            eclairProxyCreator.setOrder(Ordered.HIGHEST_PRECEDENCE);
            eclairProxyCreator.setFrozen(false);
//            eclairProxyCreator.setValidate(false);
            return eclairProxyCreator;
        }
    }
}
