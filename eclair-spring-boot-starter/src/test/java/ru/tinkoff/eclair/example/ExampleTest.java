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
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.logging.LogLevel;
import org.springframework.boot.logging.LoggingSystem;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import ru.tinkoff.eclair.autoconfigure.EclairAutoConfiguration;
import ru.tinkoff.eclair.core.LoggerNameBuilder;
import ru.tinkoff.eclair.logger.EclairLogger;
import ru.tinkoff.eclair.logger.SimpleLogger;
import ru.tinkoff.eclair.logger.collector.*;
import ru.tinkoff.eclair.logger.facade.LoggerFacadeFactory;
import ru.tinkoff.eclair.logger.facade.Slf4JLoggerFacadeFactory;
import ru.tinkoff.eclair.printer.Jaxb2Printer;
import ru.tinkoff.eclair.printer.Printer;
import ru.tinkoff.eclair.printer.processor.JaxbElementWrapper;
import ru.tinkoff.eclair.printer.processor.XPathMasker;

import javax.xml.bind.Marshaller;
import javax.xml.transform.OutputKeys;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonMap;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.*;
import static org.junit.Assert.assertEquals;
import static org.springframework.boot.logging.LogLevel.DEBUG;
import static org.springframework.boot.logging.LogLevel.values;

/**
 * @author Vyacheslav Klapatnyuk
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {
        ExampleTest.TestConfiguration.class,
        EclairAutoConfiguration.class
})
public class ExampleTest {

    private static final LoggingSystem loggingSystem = LoggingSystem.get(ExampleTest.class.getClassLoader());
    private static final ExampleAppender exampleAppender = new ExampleAppender();
    private static final PatternLayout patternLayout = new PatternLayout();
    private static final ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
    private static final LoggerFacadeFactory loggerFacadeFactory = new ExampleLoggerFacadeFactory(asList(exampleAppender, listAppender));
    private static final ExampleTableBuilder exampleTableBuilder = new ExampleTableBuilder();
    private static final ExampleCodeBuilder exampleCodeBuilder = new ExampleCodeBuilder();
    private static final LoggerNameBuilder loggerNameBuilder = LoggerNameBuilder.getInstance();
    private static final String ROOT_LOGGER_NAME = "ru.tinkoff.eclair.example.Example";

    private static final String PATTERN = "%-5level [%X] %logger{35} %msg%n";

    @Autowired
    private Example example;

    @BeforeClass
    public static void init() {
        exampleAppender.start();
        listAppender.start();

        patternLayout.setPattern(PATTERN);
        patternLayout.setContext((LoggerContext) LoggerFactory.getILoggerFactory());
        patternLayout.start();

        exampleTableBuilder.setPatternLayout(patternLayout);
        exampleCodeBuilder.setPatternLayout(patternLayout);
    }

    @Test
    public void simple() throws NoSuchMethodException {
        // given, when
        invokeForEachLevel(() -> example.simple());
        // then
        String expected = ExampleTableBuilder.TABLE_HEADER +
                " `TRACE` `DEBUG`    | `DEBUG [] r.t.eclair.example.Example.simple >`<br>`DEBUG [] r.t.eclair.example.Example.simple <`\n" +
                " `INFO` .. `OFF`    | -";
        String loggerName = loggerNameBuilder.build(Example.class.getDeclaredMethod("simple"));
        String actual = exampleTableBuilder.buildTable(groupLevels(loggerName));
        System.out.println(actual);
        assertEquals(expected, actual);
    }

    @Test
    public void simpleError() throws NoSuchMethodException {
        // given, when
        invokeForEachLevel(() -> example.simpleError());
        // then
        String expected = ExampleTableBuilder.TABLE_HEADER +
                " `TRACE` `DEBUG`    | `DEBUG [] r.t.e.example.Example.simpleError >`<br>`DEBUG [] r.t.e.example.Example.simpleError !`\n" +
                " `INFO` .. `OFF`    | -";
        String loggerName = loggerNameBuilder.build(Example.class.getDeclaredMethod("simpleError"));
        String actual = exampleTableBuilder.buildTable(groupLevels(loggerName));
        System.out.println(actual);
        assertEquals(expected, actual);
    }

    @Test
    public void level() throws NoSuchMethodException {
        // given, when
        invokeForEachLevel(() -> example.level());
        // then
        String expected = ExampleTableBuilder.TABLE_HEADER +
                " `TRACE` .. `INFO`  | `INFO  [] r.t.eclair.example.Example.level >`<br>`INFO  [] r.t.eclair.example.Example.level <`\n" +
                " `WARN` .. `OFF`    | -";
        String loggerName = loggerNameBuilder.build(Example.class.getDeclaredMethod("level"));
        String actual = exampleTableBuilder.buildTable(groupLevels(loggerName));
        System.out.println(actual);
        assertEquals(expected, actual);
    }

    @Test
    public void ifEnabled() throws NoSuchMethodException {
        // given, when
        invokeForEachLevel(() -> example.ifEnabled());
        // then
        String expected = ExampleTableBuilder.TABLE_HEADER +
                " `TRACE` `DEBUG`    | `INFO  [] r.t.e.example.Example.ifEnabled >`<br>`INFO  [] r.t.e.example.Example.ifEnabled <`\n" +
                " `INFO` .. `OFF`    | -";
        String loggerName = loggerNameBuilder.build(Example.class.getDeclaredMethod("ifEnabled"));
        String actual = exampleTableBuilder.buildTable(groupLevels(loggerName));
        System.out.println(actual);
        assertEquals(expected, actual);
    }

    @Test
    public void verboseLevel() throws NoSuchMethodException {
        // given
        String s = "s";
        Integer i = 4;
        Double d = 5.6;
        // when
        invokeForEachLevel(() -> example.verbose(s, i, d));
        // then
        String expected = ExampleTableBuilder.TABLE_HEADER +
                " `TRACE` `DEBUG`    | `INFO  [] r.t.eclair.example.Example.verbose > s=\"s\", i=4, d=5.6`<br>`INFO  [] r.t.eclair.example.Example.verbose < false`\n" +
                " `INFO`             | `INFO  [] r.t.eclair.example.Example.verbose >`<br>`INFO  [] r.t.eclair.example.Example.verbose <`\n" +
                " `WARN` .. `OFF`    | -";
        String loggerName = loggerNameBuilder.build(Example.class.getDeclaredMethod("verbose", String.class, Integer.class, Double.class));
        String actual = exampleTableBuilder.buildTable(groupLevels(loggerName));
        System.out.println(actual);
        assertEquals(expected, actual);
    }

    @Test
    public void verboseDisabled() throws NoSuchMethodException {
        // given
        String s = "f";
        Integer i = 9;
        Double d = 3.1;
        // when
        invokeForEachLevel(() -> example.verboseDisabled(s, i, d));
        // then
        String expected = ExampleTableBuilder.TABLE_HEADER +
                " `TRACE` `DEBUG`    | `DEBUG [] r.t.e.e.Example.verboseDisabled >`<br>`DEBUG [] r.t.e.e.Example.verboseDisabled <`\n" +
                " `INFO` .. `OFF`    | -";
        String loggerName = loggerNameBuilder.build(Example.class.getDeclaredMethod("verboseDisabled", String.class, Integer.class, Double.class));
        String actual = exampleTableBuilder.buildTable(groupLevels(loggerName));
        System.out.println(actual);
        assertEquals(expected, actual);
    }

    @Test
    public void verboseJson() throws NoSuchMethodException {
        // given
        Dto json = new Dto(2, "r");
        Integer i = 8;
        // when
        invokeForEachLevel(() -> example.json(json, i));
        // then
        String expected = ExampleTableBuilder.TABLE_HEADER +
                " `TRACE` `DEBUG`    | `DEBUG [] r.t.eclair.example.Example.json > dto={\"i\":2,\"s\":\"r\"}, i=8`<br>`DEBUG [] r.t.eclair.example.Example.json <`\n" +
                " `INFO` .. `OFF`    | -";
        String loggerName = loggerNameBuilder.build(Example.class.getDeclaredMethod("json", Dto.class, Integer.class));
        String actual = exampleTableBuilder.buildTable(groupLevels(loggerName));
        System.out.println(actual);
        assertEquals(expected, actual);
    }

    @Test
    public void verboseXml() throws NoSuchMethodException {
        // given
        Dto xml = new Dto(4, "k");
        Integer i = 7;
        // when
        invokeForEachLevel(() -> example.xml(xml, i));
        // then
        String expected = ExampleTableBuilder.TABLE_HEADER +
                " `TRACE` `DEBUG`    | `DEBUG [] r.t.eclair.example.Example.xml > dto=<dto><i>4</i><s>k</s></dto>, i=7`<br>`DEBUG [] r.t.eclair.example.Example.xml <`\n" +
                " `INFO` .. `OFF`    | -";
        String loggerName = loggerNameBuilder.build(Example.class.getDeclaredMethod("xml", Dto.class, Integer.class));
        String actual = exampleTableBuilder.buildTable(groupLevels(loggerName));
        System.out.println(actual);
        assertEquals(expected, actual);
    }

    @Test
    public void inOut() throws NoSuchMethodException {
        // given
        Dto dto = new Dto(3, "m");
        String s = "s";
        Integer i = 3;
        // when
        invokeForEachLevel(() -> example.inOut(dto, s, i));
        // then
        String expected = ExampleTableBuilder.TABLE_HEADER +
                " `TRACE`            | `INFO  [] r.t.eclair.example.Example.inOut > dto=Dto{i=3, s='m'}, s=\"s\", i=3`<br>`TRACE [] r.t.eclair.example.Example.inOut <`\n" +
                " `DEBUG`            | `INFO  [] r.t.eclair.example.Example.inOut > dto=Dto{i=3, s='m'}, s=\"s\", i=3`\n" +
                " `INFO`             | `INFO  [] r.t.eclair.example.Example.inOut >`\n" +
                " `WARN` .. `OFF`    | -";
        String loggerName = loggerNameBuilder.build(Example.class.getDeclaredMethod("inOut", Dto.class, String.class, Integer.class));
        String actual = exampleTableBuilder.buildTable(groupLevels(loggerName));
        System.out.println(actual);
        assertEquals(expected, actual);
    }

    @Test
    public void error() throws NoSuchMethodException {
        // given, when
        invokeForEachLevel(() -> example.error());
        // then
        String expected = ExampleTableBuilder.TABLE_HEADER +
                " `TRACE` .. `FATAL` | `ERROR [] r.t.eclair.example.Example.error ! java.lang.RuntimeException: Something strange happened`<br>`java.lang.RuntimeException: Something strange happened`<br>`\tat ru.tinkoff.eclair.example.Example.error(Example.java:0)`<br>..\n" +
                " `OFF`              | -";
        String loggerName = loggerNameBuilder.build(Example.class.getDeclaredMethod("error"));
        String actual = exampleTableBuilder.buildTable(groupLevels(loggerName));
        System.out.println(actual);
        assertEquals(expected, actual);
    }

    @Test
    public void warningOnDebug() throws NoSuchMethodException {
        // given, when
        invokeForEachLevel(() -> example.warningOnDebug());
        // then
        String expected = ExampleTableBuilder.TABLE_HEADER +
                " `TRACE` `DEBUG`    | `WARN  [] r.t.e.e.Example.warningOnDebug ! java.lang.RuntimeException: Something strange happened, but it doesn't matter`<br>`java.lang.RuntimeException: Something strange happened, but it doesn't matter`<br>`\tat ru.tinkoff.eclair.example.Example.warningOnDebug(Example.java:0)`<br>..\n" +
                " `INFO` .. `OFF`    | -";
        String loggerName = loggerNameBuilder.build(Example.class.getDeclaredMethod("warningOnDebug"));
        String actual = exampleTableBuilder.buildTable(groupLevels(loggerName));
        System.out.println(actual);
        assertEquals(expected, actual);
    }

    @Test
    public void filterErrors() throws NoSuchMethodException {
        // given
        Throwable throwable = new NullPointerException();
        Throwable throwable1 = new Exception();
        Throwable throwable2 = new Error();
        // when
        invokeForEachLevel(() -> example.filterErrors(throwable));
        invokeForEachLevel(() -> example.filterErrors(throwable1));
        invokeForEachLevel(() -> example.filterErrors(throwable2));
        // then
        String expected = ExampleTableBuilder.TABLE_HEADER +
                " `TRACE` .. `WARN`  | `WARN  [] r.t.e.example.Example.filterErrors ! java.lang.NullPointerException`<br>`java.lang.NullPointerException: null`<br>`\tat ru.tinkoff.eclair.example.ExampleTest.filterErrors(ExampleTest.java:0)`<br>..<br>`ERROR [] r.t.e.example.Example.filterErrors ! java.lang.Exception`<br>`java.lang.Exception: null`<br>`\tat ru.tinkoff.eclair.example.ExampleTest.filterErrors(ExampleTest.java:0)`<br>..\n" +
                " `ERROR` `FATAL`    | `ERROR [] r.t.e.example.Example.filterErrors ! java.lang.Exception`<br>`java.lang.Exception: null`<br>`\tat ru.tinkoff.eclair.example.ExampleTest.filterErrors(ExampleTest.java:0)`<br>..\n" +
                " `OFF`              | -";
        String loggerName = loggerNameBuilder.build(Example.class.getDeclaredMethod("filterErrors", Throwable.class));
        String actual = exampleTableBuilder.buildTable(groupLevels(loggerName));
        System.out.println(actual);
        assertEquals(expected, actual);
    }

    @Test
    public void mostSpecific() throws NoSuchMethodException {
        // given, when
        invokeForEachLevel(() -> example.mostSpecific());
        // then
        String expected = ExampleTableBuilder.TABLE_HEADER +
                " `TRACE` .. `WARN`  | `WARN  [] r.t.e.example.Example.mostSpecific ! java.lang.IllegalArgumentException`<br>`java.lang.IllegalArgumentException: null`<br>`\tat ru.tinkoff.eclair.example.Example.mostSpecific(Example.java:0)`<br>..\n" +
                " `ERROR` .. `OFF`   | -";
        String loggerName = loggerNameBuilder.build(Example.class.getDeclaredMethod("mostSpecific"));
        String actual = exampleTableBuilder.buildTable(groupLevels(loggerName));
        System.out.println(actual);
        assertEquals(expected, actual);
    }

    @Test
    public void parameter() throws NoSuchMethodException {
        // given
        Dto dto = new Dto(0, "u");
        String s = "s";
        Integer i = 3;
        // when
        invokeForEachLevel(() -> example.parameter(dto, s, i));
        // then
        String expected = ExampleTableBuilder.TABLE_HEADER +
                " `TRACE` `DEBUG`    | `INFO  [] r.t.e.example.Example.parameter > dto=Dto{i=0, s='u'}`\n" +
                " `INFO`             | `INFO  [] r.t.e.example.Example.parameter > Dto{i=0, s='u'}`\n" +
                " `WARN` .. `OFF`    | -";
        String loggerName = loggerNameBuilder.build(Example.class.getDeclaredMethod("parameter", Dto.class, String.class, Integer.class));
        String actual = exampleTableBuilder.buildTable(groupLevels(loggerName));
        System.out.println(actual);
        assertEquals(expected, actual);
    }

    @Test
    public void printers() throws NoSuchMethodException {
        // given
        Dto dto = new Dto(5, "password");
        Integer i = 4;
        // when
        invokeForEachLevel(() -> example.printers(dto, dto, i));
        // then
        String expected = ExampleTableBuilder.TABLE_HEADER +
                " `TRACE` `DEBUG`    | `DEBUG [] r.t.eclair.example.Example.printers > xml=<dto><i>5</i><s>********</s></dto>, json={\"i\":5,\"s\":\"password\"}`<br>`DEBUG [] r.t.eclair.example.Example.printers < <dto><i>5</i><s>********</s></dto>`\n" +
                " `INFO` .. `OFF`    | -";
        String loggerName = loggerNameBuilder.build(Example.class.getDeclaredMethod("printers", Dto.class, Dto.class, Integer.class));
        String actual = exampleTableBuilder.buildTable(groupLevels(loggerName));
        System.out.println(actual);
        assertEquals(expected, actual);
    }

    @Test
    public void parameterLevels() throws NoSuchMethodException {
        // given
        Double d = 9.4;
        String s = "v";
        Integer i = 7;
        // when
        invokeForEachLevel(() -> example.parameterLevels(d, s, i));
        // then
        String expected = ExampleTableBuilder.TABLE_HEADER +
                " `TRACE`            | `INFO  [] r.t.e.e.Example.parameterLevels > d=9.4, s=\"v\", i=7`\n" +
                " `DEBUG`            | `INFO  [] r.t.e.e.Example.parameterLevels > d=9.4, s=\"v\"`\n" +
                " `INFO`             | `INFO  [] r.t.e.e.Example.parameterLevels > 9.4`\n" +
                " `WARN` .. `OFF`    | -";
        String loggerName = loggerNameBuilder.build(Example.class.getDeclaredMethod("parameterLevels", Double.class, String.class, Integer.class));
        String actual = exampleTableBuilder.buildTable(groupLevels(loggerName));
        System.out.println(actual);
        assertEquals(expected, actual);
    }

    @Test
    public void mix() throws NoSuchMethodException {
        // given
        Dto xml = new Dto(5, "a");
        Dto json = new Dto(7, "b");
        Integer i = 1;
        // when
        invokeForEachLevel(() -> example.mix(xml, json, i));
        // then
        String expected = ExampleTableBuilder.TABLE_HEADER +
                " `TRACE`            | `INFO  [] r.t.eclair.example.Example.mix > xml=<dto><i>5</i><s>a</s></dto>, json={\"i\":7,\"s\":\"b\"}, i=1`<br>`WARN  [] r.t.eclair.example.Example.mix ! java.lang.IllegalArgumentException: Something strange happened`<br>`java.lang.IllegalArgumentException: Something strange happened`<br>`\tat ru.tinkoff.eclair.example.Example.mix(Example.java:0)`<br>..\n" +
                " `DEBUG`            | `INFO  [] r.t.eclair.example.Example.mix > xml=<dto><i>5</i><s>a</s></dto>, i=1`<br>`WARN  [] r.t.eclair.example.Example.mix ! java.lang.IllegalArgumentException: Something strange happened`<br>`java.lang.IllegalArgumentException: Something strange happened`<br>`\tat ru.tinkoff.eclair.example.Example.mix(Example.java:0)`<br>..\n" +
                " `INFO`             | `INFO  [] r.t.eclair.example.Example.mix >`<br>`WARN  [] r.t.eclair.example.Example.mix ! java.lang.IllegalArgumentException: Something strange happened`<br>`java.lang.IllegalArgumentException: Something strange happened`<br>`\tat ru.tinkoff.eclair.example.Example.mix(Example.java:0)`<br>..\n" +
                " `WARN`             | `WARN  [] r.t.eclair.example.Example.mix ! java.lang.IllegalArgumentException: Something strange happened`<br>`java.lang.IllegalArgumentException: Something strange happened`<br>`\tat ru.tinkoff.eclair.example.Example.mix(Example.java:0)`<br>..\n" +
                " `ERROR` .. `OFF`   | -";
        String loggerName = loggerNameBuilder.build(Example.class.getDeclaredMethod("mix", Dto.class, Dto.class, Integer.class));
        String actual = exampleTableBuilder.buildTable(groupLevels(loggerName));
        System.out.println(actual);
        assertEquals(expected, actual);
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

    @Test
    public void mdc() throws NoSuchMethodException {
        // given
        listAppender.list.clear();
        // when
        invoke(() -> example.outer(), DEBUG);
        // then
        String expected = "```\n" +
                "DEBUG [] r.t.eclair.example.Example.outer >\n" +
                "DEBUG [beanReference={\"i\":0,\"s\":null}, sum=2, static=string, staticMethod=01234567-89ab-cdef-ghij-klmnopqrstuv] r.t.eclair.example.Example.mdc >\n" +
                "DEBUG [beanReference={\"i\":0,\"s\":null}, sum=2, static=string, staticMethod=01234567-89ab-cdef-ghij-klmnopqrstuv] r.t.eclair.example.Example.inner >\n" +
                "DEBUG [beanReference={\"i\":0,\"s\":null}, sum=2, static=string, staticMethod=01234567-89ab-cdef-ghij-klmnopqrstuv] r.t.eclair.example.Example.mdc <\n" +
                "DEBUG [sum=2] r.t.eclair.example.Example.outer <\n" +
                "```";
        String actual = exampleCodeBuilder.buildMultilineBlock(listAppender.list);
        MDC.clear();
        listAppender.list.clear();
        System.out.println(actual);
        assertEquals(expected, actual);
    }

    @Test
    public void mdcByArgument() throws NoSuchMethodException {
        // given
        listAppender.list.clear();
        Dto dto = new Dto(12, "password");
        // when
        invoke(() -> example.mdcByArgument(dto), DEBUG);
        // then
        String expected = "```\n" +
                "DEBUG [length=8, dto=Dto{i=12, s='password'}] r.t.e.example.Example.mdcByArgument > dto=Dto{i=12, s='password'}\n" +
                "```";
        String actual = exampleCodeBuilder.buildMultilineBlock(listAppender.list);
        MDC.clear();
        listAppender.list.clear();
        System.out.println(actual);
        assertEquals(expected, actual);
    }

    @Test
    public void mdcByDefault() throws NoSuchMethodException {
        // given
        listAppender.list.clear();
        String first = "content";
        Double second = Math.PI;
        // when
        invoke(() -> example.mdcByDefault(first, second), DEBUG);
        // then
        String expected = "```\n" +
                "DEBUG [first=content, second=3.141592653589793] r.t.e.example.Example.mdcByDefault > first=\"content\", second=3.141592653589793\n" +
                "```";
        String actual = exampleCodeBuilder.buildMultilineBlock(listAppender.list);
        MDC.clear();
        listAppender.list.clear();
        System.out.println(actual);
        assertEquals(expected, actual);
    }

    @Test
    public void manual() throws NoSuchMethodException {
        // given, when
        invokeForEachLevel(() -> example.manual());
        // then
        String expected = ExampleTableBuilder.TABLE_HEADER +
                " `TRACE` `DEBUG`    | `DEBUG [] r.t.eclair.example.Example.manual >`<br>`INFO  [] r.t.eclair.example.Example.manual - Eager logging: 3.141592653589793`<br>`DEBUG [] r.t.eclair.example.Example.manual - Lazy logging: 3.141592653589793`<br>`DEBUG [] r.t.eclair.example.Example.manual <`\n" +
                " `INFO`             | `INFO  [] r.t.eclair.example.Example.manual - Eager logging: 3.141592653589793`\n" +
                " `WARN` .. `OFF`    | -";
        String loggerName = loggerNameBuilder.build(Example.class.getDeclaredMethod("manual"));
        String actual = exampleTableBuilder.buildTable(groupLevels(loggerName));
        System.out.println(actual);
        assertEquals(expected, actual);
    }

    @Test
    public void manualLevel() throws NoSuchMethodException {
        // given, when
        invokeForEachLevel(() -> example.manualLevel());
        // then
        String expected = ExampleTableBuilder.TABLE_HEADER +
                " `TRACE`            | `DEBUG [] r.t.e.example.Example.manualLevel >`<br>`ERROR [] r.t.e.example.Example.manualLevel - ERROR`<br>`ERROR [] r.t.e.example.Example.manualLevel - ERROR if WARN enabled`<br>`ERROR [] r.t.e.example.Example.manualLevel - ERROR if INFO enabled`<br>`ERROR [] r.t.e.example.Example.manualLevel - ERROR if DEBUG enabled`<br>`ERROR [] r.t.e.example.Example.manualLevel - ERROR if TRACE enabled`<br>`WARN  [] r.t.e.example.Example.manualLevel - WARN`<br>`WARN  [] r.t.e.example.Example.manualLevel - WARN if INFO enabled`<br>`WARN  [] r.t.e.example.Example.manualLevel - WARN if DEBUG enabled`<br>`WARN  [] r.t.e.example.Example.manualLevel - WARN if TRACE enabled`<br>`INFO  [] r.t.e.example.Example.manualLevel - INFO`<br>`INFO  [] r.t.e.example.Example.manualLevel - INFO if DEBUG enabled`<br>`INFO  [] r.t.e.example.Example.manualLevel - INFO if TRACE enabled`<br>`DEBUG [] r.t.e.example.Example.manualLevel - DEBUG`<br>`DEBUG [] r.t.e.example.Example.manualLevel - DEBUG if TRACE enabled`<br>`TRACE [] r.t.e.example.Example.manualLevel - TRACE`<br>`DEBUG [] r.t.e.example.Example.manualLevel <`\n" +
                " `DEBUG`            | `DEBUG [] r.t.e.example.Example.manualLevel >`<br>`ERROR [] r.t.e.example.Example.manualLevel - ERROR`<br>`ERROR [] r.t.e.example.Example.manualLevel - ERROR if WARN enabled`<br>`ERROR [] r.t.e.example.Example.manualLevel - ERROR if INFO enabled`<br>`ERROR [] r.t.e.example.Example.manualLevel - ERROR if DEBUG enabled`<br>`WARN  [] r.t.e.example.Example.manualLevel - WARN`<br>`WARN  [] r.t.e.example.Example.manualLevel - WARN if INFO enabled`<br>`WARN  [] r.t.e.example.Example.manualLevel - WARN if DEBUG enabled`<br>`INFO  [] r.t.e.example.Example.manualLevel - INFO`<br>`INFO  [] r.t.e.example.Example.manualLevel - INFO if DEBUG enabled`<br>`DEBUG [] r.t.e.example.Example.manualLevel - DEBUG`<br>`DEBUG [] r.t.e.example.Example.manualLevel <`\n" +
                " `INFO`             | `ERROR [] r.t.e.example.Example.manualLevel - ERROR`<br>`ERROR [] r.t.e.example.Example.manualLevel - ERROR if WARN enabled`<br>`ERROR [] r.t.e.example.Example.manualLevel - ERROR if INFO enabled`<br>`WARN  [] r.t.e.example.Example.manualLevel - WARN`<br>`WARN  [] r.t.e.example.Example.manualLevel - WARN if INFO enabled`<br>`INFO  [] r.t.e.example.Example.manualLevel - INFO`\n" +
                " `WARN`             | `ERROR [] r.t.e.example.Example.manualLevel - ERROR`<br>`ERROR [] r.t.e.example.Example.manualLevel - ERROR if WARN enabled`<br>`WARN  [] r.t.e.example.Example.manualLevel - WARN`\n" +
                " `ERROR` `FATAL`    | `ERROR [] r.t.e.example.Example.manualLevel - ERROR`\n" +
                " `OFF`              | -";
        String loggerName = loggerNameBuilder.build(Example.class.getDeclaredMethod("manualLevel"));
        String actual = exampleTableBuilder.buildTable(groupLevels(loggerName));
        System.out.println(actual);
        assertEquals(expected, actual);
    }

    private void invokeForEachLevel(Runnable runnable) {
        Stream.of(values()).forEach(level -> invoke(runnable, level));
    }

    private void invoke(Runnable runnable, LogLevel level) {
        loggingSystem.setLogLevel(ROOT_LOGGER_NAME, level);
        exampleAppender.setLevel(level);
        try {
            runnable.run();
        } catch (Throwable e) {
            // do nothing
        }
    }

    private interface Runnable {

        void run() throws Throwable;
    }

    @Configuration
    static class TestConfiguration {

        @Bean
        public Example example() {
            return new Example();
        }

        @Bean
        public Jaxb2Marshaller jaxb2Marshaller() {
            Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
            marshaller.setClassesToBeBound(Dto.class);
            marshaller.setMarshallerProperties(singletonMap(Marshaller.JAXB_FRAGMENT, true));
            return marshaller;
        }

        @Bean
        @Order(99)
        public Printer maskJaxb2Printer(Jaxb2Marshaller jaxb2Marshaller) {
            XPathMasker xPathMasker = new XPathMasker("//s");
            xPathMasker.setReplacement("********");
            xPathMasker.setOutputProperties(singletonMap(OutputKeys.OMIT_XML_DECLARATION, "yes"));
            return new Jaxb2Printer(jaxb2Marshaller)
                    .addPreProcessor(new JaxbElementWrapper(jaxb2Marshaller))
                    .addPostProcessor(xPathMasker);
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
        @Primary
        public EclairLogger simpleLogger(LogInCollectorFactory<?> logInCollectorFactory, LogOutCollector<?> logOutCollector) {
            return new SimpleLogger(
                    loggerFacadeFactory,
                    LoggingSystem.get(SimpleLogger.class.getClassLoader()),
                    logInCollectorFactory,
                    logOutCollector
            );
        }

        @Bean
        public EclairLogger auditLogger() {
            return new SimpleLogger();
        }
    }
}
