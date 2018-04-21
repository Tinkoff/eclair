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

package ru.tinkoff.eclair.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import ru.tinkoff.eclair.annotation.Log;
import ru.tinkoff.eclair.annotation.Logs;
import ru.tinkoff.eclair.annotation.Mdc;
import ru.tinkoff.eclair.definition.*;
import ru.tinkoff.eclair.printer.JacksonPrinter;
import ru.tinkoff.eclair.printer.Jaxb2Printer;
import ru.tinkoff.eclair.printer.Printer;
import ru.tinkoff.eclair.printer.resolver.AliasedPrinterResolver;
import ru.tinkoff.eclair.printer.resolver.PrinterResolver;

import java.lang.reflect.Method;
import java.util.*;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singleton;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.springframework.boot.logging.LogLevel.*;

/**
 * @author Vyacheslav Klapatnyuk
 */
public class AnnotationDefinitionFactoryTest {

    private final Printer xmlPrinter = new Jaxb2Printer(new Jaxb2Marshaller());
    private final Printer jsonPrinter = new JacksonPrinter(new ObjectMapper());

    private AnnotationDefinitionFactory annotationDefinitionFactory;

    @Before
    public void init() {
        Map<String, Printer> printers = new LinkedHashMap<>();
        printers.put("xml", xmlPrinter);
        printers.put("json", jsonPrinter);
        PrinterResolver printerResolver = new AliasedPrinterResolver(printers, emptyMap());
        annotationDefinitionFactory = new AnnotationDefinitionFactory(printerResolver);
    }

    @Test
    public void buildInLogByLogIn() throws NoSuchMethodException {
        // given
        Set<String> loggerNames = singleton("");
        Method method = LogInLoggableClass.class.getDeclaredMethod("logIn", String.class, String.class);
        // when
        InLog inLog = annotationDefinitionFactory.buildInLog(loggerNames, method);
        // then
        assertNotNull(inLog);
        assertThat(inLog.getLevel(), is(INFO));
    }

    @Test
    public void buildInLogByLog() throws NoSuchMethodException {
        // given
        Set<String> loggerNames = singleton("");
        Method method = LogInLoggableClass.class.getDeclaredMethod("log", String.class, String.class);
        // when
        InLog inLog = annotationDefinitionFactory.buildInLog(loggerNames, method);
        // then
        assertNotNull(inLog);
        assertThat(inLog.getLevel(), is(WARN));
    }

    @Test
    public void buildInLogByLogWithAlias() throws NoSuchMethodException {
        // given
        Set<String> loggerNames = singleton("");
        Method method = LogInLoggableClass.class.getDeclaredMethod("logWithAlias", String.class, String.class);
        // when
        InLog inLog = annotationDefinitionFactory.buildInLog(loggerNames, method);
        // then
        assertNotNull(inLog);
        assertThat(inLog.getLevel(), is(WARN));
    }

    @Test
    public void buildInLogEmpty() throws NoSuchMethodException {
        // given
        Set<String> loggerNames = singleton("");
        Method method = LogInLoggableClass.class.getDeclaredMethod("empty", String.class, String.class);
        // when
        InLog inLog = annotationDefinitionFactory.buildInLog(loggerNames, method);
        // then
        assertThat(inLog, nullValue());
    }

    @Test
    public void buildParameterLogs() throws NoSuchMethodException {
        // given
        Set<String> loggerNames = singleton("");
        Method method = LogInLoggableClass.class.getDeclaredMethod("parameterLog", String.class, String.class);
        // when
        List<ParameterLog> parameterLogs = annotationDefinitionFactory.buildParameterLogs(loggerNames, method);
        // then
        assertThat(parameterLogs, hasSize(2));
        assertThat(parameterLogs.get(0).getIfEnabledLevel(), is(WARN));
        assertThat(parameterLogs.get(1), nullValue());
    }

    @Test
    public void buildInLogByLogIns() throws NoSuchMethodException {
        // given
        Set<String> loggerNames = singleton("");
        Method method = LogInLoggableClass.class.getDeclaredMethod("logIns");
        // when
        InLog inLog = annotationDefinitionFactory.buildInLog(loggerNames, method);
        // then
        assertNotNull(inLog);
        assertThat(inLog.getLevel(), is(ERROR));
    }

    @Test
    public void buildInLogByLogs() throws NoSuchMethodException {
        // given
        Set<String> loggerNames = singleton("");
        Method method = LogInLoggableClass.class.getDeclaredMethod("logs");
        // when
        InLog inLog = annotationDefinitionFactory.buildInLog(loggerNames, method);
        // then
        assertNotNull(inLog);
        assertThat(inLog.getLevel(), is(ERROR));
    }

    @SuppressWarnings("unused")
    private static class LogInLoggableClass {

        @Log.in(INFO)
        @Log(WARN)
        void logIn(String a, String b) {
        }

        @Log(level = WARN)
        void log(String a, String b) {
        }

        @Log(WARN)
        void logWithAlias(String a, String b) {
        }

        void empty(String a, String b) {
        }

        void parameterLog(@Log(ifEnabled = WARN) String a, String b) {
        }

        @Log.ins(@Log.in(ERROR))
        void logIns() {
        }

        @Logs(@Log(ERROR))
        void logs() {
        }
    }

    @Test
    public void buildOutLog() throws NoSuchMethodException {
        // given
        Set<String> loggerNames = singleton("");
        Method method = LogOutLoggableClass.class.getDeclaredMethod("logOut");
        // when
        OutLog outLog = annotationDefinitionFactory.buildOutLog(loggerNames, method);
        // then
        assertNotNull(outLog);
        assertThat(outLog.getLevel(), is(INFO));
    }

    @Test
    public void buildOutLogByLog() throws NoSuchMethodException {
        // given
        Set<String> loggerNames = singleton("");
        Method method = LogOutLoggableClass.class.getDeclaredMethod("log");
        // when
        OutLog outLog = annotationDefinitionFactory.buildOutLog(loggerNames, method);
        // then
        assertNotNull(outLog);
        assertThat(outLog.getLevel(), is(WARN));
        assertThat(outLog.getIfEnabledLevel(), is(ERROR));
        assertThat(outLog.getVerboseLevel(), is(TRACE));
        assertThat(outLog.getPrinter(), is(jsonPrinter));
    }

    @Test
    public void buildOutLogByLogWithAlias() throws NoSuchMethodException {
        // given
        Set<String> loggerNames = singleton("");
        Method method = LogOutLoggableClass.class.getDeclaredMethod("logWithAlias");
        // when
        OutLog outLog = annotationDefinitionFactory.buildOutLog(loggerNames, method);
        // then
        assertNotNull(outLog);
        assertThat(outLog.getLevel(), is(WARN));
    }

    @Test
    public void buildOutLogEmpty() throws NoSuchMethodException {
        // given
        Set<String> loggerNames = singleton("");
        Method method = LogOutLoggableClass.class.getDeclaredMethod("empty");
        // when
        OutLog outLog = annotationDefinitionFactory.buildOutLog(loggerNames, method);
        // then
        assertThat(outLog, nullValue());
    }

    @Test
    public void buildOutLogByLogOuts() throws NoSuchMethodException {
        // given
        Set<String> loggerNames = singleton("");
        Method method = LogOutLoggableClass.class.getDeclaredMethod("logOuts");
        // when
        OutLog outLog = annotationDefinitionFactory.buildOutLog(loggerNames, method);
        // then
        assertNotNull(outLog);
        assertThat(outLog.getLevel(), is(ERROR));
    }

    @Test
    public void buildOutLogByLogs() throws NoSuchMethodException {
        // given
        Set<String> loggerNames = singleton("");
        Method method = LogOutLoggableClass.class.getDeclaredMethod("logs");
        // when
        OutLog outLog = annotationDefinitionFactory.buildOutLog(loggerNames, method);
        // then
        assertNotNull(outLog);
        assertThat(outLog.getLevel(), is(ERROR));
    }

    @SuppressWarnings("unused")
    private static class LogOutLoggableClass {

        @Log.out(INFO)
        @Log(WARN)
        void logOut() {
        }

        @Log(level = WARN, ifEnabled = ERROR, verbose = TRACE, printer = "json")
        void log() {
        }

        @Log(value = WARN)
        void logWithAlias() {
        }

        void empty() {
        }

        @Log.outs(@Log.out(ERROR))
        void logOuts() {
        }

        @Logs(@Log(ERROR))
        void logs() {
        }
    }

    @Test
    public void buildErrorLogs() throws NoSuchMethodException {
        // given
        Set<String> loggerNames = singleton("");
        Method method = LogErrorLoggableClass.class.getDeclaredMethod("logError");
        // when
        Set<ErrorLog> errorLogs = annotationDefinitionFactory.buildErrorLogs(loggerNames, method);
        // then
        assertThat(errorLogs, hasSize(2));
        Iterator<ErrorLog> iterator = errorLogs.iterator();
        assertThat(iterator.next().getLevel(), is(WARN));
        assertThat(iterator.next().getLevel(), is(INFO));
    }

    @Test
    public void buildErrorLogsEmpty() throws NoSuchMethodException {
        // given
        Set<String> loggerNames = singleton("");
        Method method = LogErrorLoggableClass.class.getDeclaredMethod("empty");
        // when
        Set<ErrorLog> errorLogs = annotationDefinitionFactory.buildErrorLogs(loggerNames, method);
        // then
        assertThat(errorLogs, is(empty()));
    }

    @SuppressWarnings("unused")
    private static class LogErrorLoggableClass {

        @Log.error(WARN)
        @Log.error(INFO)
        void logError() {
        }

        void empty() {
        }
    }

    @Test
    public void buildMethodParameterMdcs() throws NoSuchMethodException {
        // given
        Method method = MdcLoggableClass.class.getDeclaredMethod("mdc", String.class, String.class);
        // when
        Set<ParameterMdc> parameterMdcs = annotationDefinitionFactory.buildMethodParameterMdcs(method);
        // then
        assertThat(parameterMdcs, hasSize(1));
        assertThat(parameterMdcs.iterator().next().getKey(), is("method"));
    }

    @Test
    public void buildMethodParameterMdcsEmpty() throws NoSuchMethodException {
        // given
        Method method = MdcLoggableClass.class.getDeclaredMethod("empty");
        // when
        Set<ParameterMdc> parameterMdcs = annotationDefinitionFactory.buildMethodParameterMdcs(method);
        // then
        assertThat(parameterMdcs, is(empty()));
    }

    @Test
    public void buildParameterMdcs() throws NoSuchMethodException {
        // given
        Method method = MdcLoggableClass.class.getDeclaredMethod("mdc", String.class, String.class);
        // when
        List<Set<ParameterMdc>> parameterMdcs = annotationDefinitionFactory.buildParameterMdcs(method);
        // then
        assertThat(parameterMdcs, hasSize(2));
        assertThat(parameterMdcs.get(0), hasSize(1));
        assertThat(parameterMdcs.get(0).iterator().next().getKey(), is("a"));
        assertThat(parameterMdcs.get(1), is(empty()));
    }

    @Test
    public void buildParameterMdcsEmpty() throws NoSuchMethodException {
        // given
        Method method = MdcLoggableClass.class.getDeclaredMethod("empty");
        // when
        List<Set<ParameterMdc>> parameterMdcs = annotationDefinitionFactory.buildParameterMdcs(method);
        // then
        assertThat(parameterMdcs, is(empty()));
    }

    @SuppressWarnings("unused")
    private static class MdcLoggableClass {

        @Mdc(key = "method")
        void mdc(@Mdc(key = "a") String a, String b) {
        }

        void empty() {
        }
    }
}
