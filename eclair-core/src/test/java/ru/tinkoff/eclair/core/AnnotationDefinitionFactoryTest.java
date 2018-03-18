package ru.tinkoff.eclair.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.support.StaticListableBeanFactory;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import ru.tinkoff.eclair.annotation.Log;
import ru.tinkoff.eclair.annotation.Logs;
import ru.tinkoff.eclair.annotation.Mdc;
import ru.tinkoff.eclair.definition.ErrorLog;
import ru.tinkoff.eclair.definition.InLog;
import ru.tinkoff.eclair.definition.OutLog;
import ru.tinkoff.eclair.printer.JacksonPrinter;
import ru.tinkoff.eclair.printer.Jaxb2Printer;
import ru.tinkoff.eclair.printer.Printer;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.springframework.boot.logging.LogLevel.*;

/**
 * @author Viacheslav Klapatniuk
 */
public class AnnotationDefinitionFactoryTest {

    private final Printer xmlPrinter = new Jaxb2Printer(new Jaxb2Marshaller());
    private final Printer jsonPrinter = new JacksonPrinter(new ObjectMapper());

    private AnnotationDefinitionFactory annotationDefinitionFactory;

    @Before
    public void init() {
        Map<String, Object> printers = new LinkedHashMap<>();
        printers.put("xml", xmlPrinter);
        printers.put("json", jsonPrinter);
        PrinterResolver printerResolver = new PrinterResolver(new StaticListableBeanFactory(printers), asList(xmlPrinter, jsonPrinter));
        annotationDefinitionFactory = new AnnotationDefinitionFactory(printerResolver);
    }

    @Test
    public void buildInLogByLogIn() throws NoSuchMethodException {
        // given
        Set<String> loggerNames = singleton("");
        Method method = LogInLoggableClass.class.getMethod("logIn", String.class, String.class);
        // when
        InLog inLog = annotationDefinitionFactory.buildInLog(loggerNames, method);
        // then
        assertThat(inLog.getLevel(), is(INFO));
        assertThat(inLog.getArgLogs(), hasSize(2));
        assertThat(inLog.getArgLogs().get(0).getPrinter(), is(jsonPrinter));
        assertThat(inLog.getArgLogs().get(1).getPrinter(), is(jsonPrinter));
    }

    @Test
    public void buildInLogByLogInLogArg() throws NoSuchMethodException {
        // given
        Set<String> loggerNames = singleton("");
        Method method = LogInLoggableClass.class.getMethod("logInLogArg", String.class, String.class);
        // when
        InLog inLog = annotationDefinitionFactory.buildInLog(loggerNames, method);
        // then
        assertThat(inLog.getLevel(), is(INFO));
        assertThat(inLog.getArgLogs().get(0).getIfEnabledLevel(), is(WARN));
        assertThat(inLog.getArgLogs().get(0).getPrinter(), is(xmlPrinter));
        assertThat(inLog.getArgLogs().get(1).getPrinter(), is(jsonPrinter));
    }

    @Test
    public void buildInLogByLog() throws NoSuchMethodException {
        // given
        Set<String> loggerNames = singleton("");
        Method method = LogInLoggableClass.class.getMethod("log", String.class, String.class);
        // when
        InLog inLog = annotationDefinitionFactory.buildInLog(loggerNames, method);
        // then
        assertThat(inLog.getLevel(), is(WARN));
        assertThat(inLog.getIfEnabledLevel(), is(ERROR));
        assertThat(inLog.getVerboseLevel(), is(TRACE));
        assertThat(inLog.getArgLogs(), hasSize(2));
        assertThat(inLog.getArgLogs().get(0).getPrinter(), is(jsonPrinter));
        assertThat(inLog.getArgLogs().get(1).getPrinter(), is(jsonPrinter));
    }

    @Test
    public void buildInLogByLogWithAlias() throws NoSuchMethodException {
        // given
        Set<String> loggerNames = singleton("");
        Method method = LogInLoggableClass.class.getMethod("logWithAlias", String.class, String.class);
        // when
        InLog inLog = annotationDefinitionFactory.buildInLog(loggerNames, method);
        // then
        assertThat(inLog.getLevel(), is(WARN));
    }

    @Test
    public void buildInLogEmpty() throws NoSuchMethodException {
        // given
        Set<String> loggerNames = singleton("");
        Method method = LogInLoggableClass.class.getMethod("empty", String.class, String.class);
        // when
        InLog inLog = annotationDefinitionFactory.buildInLog(loggerNames, method);
        // then
        assertThat(inLog, nullValue());
    }

    @Test
    public void buildInLogByLogArg() throws NoSuchMethodException {
        // given
        Set<String> loggerNames = singleton("");
        Method method = LogInLoggableClass.class.getMethod("logArg", String.class, String.class);
        // when
        InLog inLog = annotationDefinitionFactory.buildInLog(loggerNames, method);
        // then
        assertThat(inLog.getLevel(), is(DEBUG));
        assertThat(inLog.getArgLogs(), hasSize(2));
        assertThat(inLog.getArgLogs().get(0).getIfEnabledLevel(), is(WARN));
        assertThat(inLog.getArgLogs().get(1), nullValue());
    }

    @Test
    public void buildInLogByLogIns() throws NoSuchMethodException {
        // given
        Set<String> loggerNames = singleton("");
        Method method = LogInLoggableClass.class.getMethod("logIns");
        // when
        InLog inLog = annotationDefinitionFactory.buildInLog(loggerNames, method);
        // then
        assertThat(inLog.getLevel(), is(ERROR));
    }

    @Test
    public void buildInLogByLogs() throws NoSuchMethodException {
        // given
        Set<String> loggerNames = singleton("");
        Method method = LogInLoggableClass.class.getMethod("logs");
        // when
        InLog inLog = annotationDefinitionFactory.buildInLog(loggerNames, method);
        // then
        assertThat(inLog.getLevel(), is(ERROR));
    }

    @SuppressWarnings("unused")
    private static class LogInLoggableClass {

        @Log.in(level = INFO, printer = "json")
        @Log(WARN)
        public void logIn(String a, String b) {
        }

        @Log.in(level = INFO, printer = "json")
        public void logInLogArg(@Log.arg(ifEnabled = WARN, printer = "xml") String a, String b) {
        }

        @Log(level = WARN, ifEnabled = ERROR, verbose = TRACE, printer = "json")
        public void log(String a, String b) {
        }

        @Log(WARN)
        public void logWithAlias(String a, String b) {
        }

        public void empty(String a, String b) {
        }

        public void logArg(@Log.arg(ifEnabled = WARN) String a, String b) {
        }

        @Log.ins(@Log.in(ERROR))
        public void logIns() {
        }

        @Logs(@Log(ERROR))
        public void logs() {
        }
    }

    @Test
    public void buildOutLog() throws NoSuchMethodException {
        // given
        Set<String> loggerNames = singleton("");
        Method method = LogOutLoggableClass.class.getMethod("logOut");
        // when
        OutLog outLog = annotationDefinitionFactory.buildOutLog(loggerNames, method);
        // then
        assertThat(outLog.getLevel(), is(INFO));
    }

    @Test
    public void buildOutLogByLog() throws NoSuchMethodException {
        // given
        Set<String> loggerNames = singleton("");
        Method method = LogOutLoggableClass.class.getMethod("log");
        // when
        OutLog outLog = annotationDefinitionFactory.buildOutLog(loggerNames, method);
        // then
        assertThat(outLog.getLevel(), is(WARN));
        assertThat(outLog.getIfEnabledLevel(), is(ERROR));
        assertThat(outLog.getVerboseLevel(), is(TRACE));
        assertThat(outLog.getPrinter(), is(jsonPrinter));
    }

    @Test
    public void buildOutLogByLogWithAlias() throws NoSuchMethodException {
        // given
        Set<String> loggerNames = singleton("");
        Method method = LogOutLoggableClass.class.getMethod("logWithAlias");
        // when
        OutLog outLog = annotationDefinitionFactory.buildOutLog(loggerNames, method);
        // then
        assertThat(outLog.getLevel(), is(WARN));
    }

    @Test
    public void buildOutLogEmpty() throws NoSuchMethodException {
        // given
        Set<String> loggerNames = singleton("");
        Method method = LogOutLoggableClass.class.getMethod("empty");
        // when
        OutLog outLog = annotationDefinitionFactory.buildOutLog(loggerNames, method);
        // then
        assertThat(outLog, nullValue());
    }

    @Test
    public void buildOutLogByLogOuts() throws NoSuchMethodException {
        // given
        Set<String> loggerNames = singleton("");
        Method method = LogOutLoggableClass.class.getMethod("logOuts");
        // when
        OutLog outLog = annotationDefinitionFactory.buildOutLog(loggerNames, method);
        // then
        assertThat(outLog.getLevel(), is(ERROR));
    }

    @Test
    public void buildOutLogByLogs() throws NoSuchMethodException {
        // given
        Set<String> loggerNames = singleton("");
        Method method = LogOutLoggableClass.class.getMethod("logs");
        // when
        OutLog outLog = annotationDefinitionFactory.buildOutLog(loggerNames, method);
        // then
        assertThat(outLog.getLevel(), is(ERROR));
    }

    @SuppressWarnings("unused")
    private static class LogOutLoggableClass {

        @Log.out(INFO)
        @Log(WARN)
        public void logOut() {
        }

        @Log(level = WARN, ifEnabled = ERROR, verbose = TRACE, printer = "json")
        public void log() {
        }

        @Log(value = WARN)
        public void logWithAlias() {
        }

        public void empty() {
        }

        @Log.outs(@Log.out(ERROR))
        public void logOuts() {
        }

        @Logs(@Log(ERROR))
        public void logs() {
        }
    }

    @Test
    public void buildErrorLogs() throws NoSuchMethodException {
        // given
        Set<String> loggerNames = singleton("");
        Method method = LogErrorLoggableClass.class.getMethod("logError");
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
        Method method = LogErrorLoggableClass.class.getMethod("empty");
        // when
        Set<ErrorLog> errorLogs = annotationDefinitionFactory.buildErrorLogs(loggerNames, method);
        // then
        assertThat(errorLogs, is(empty()));
    }

    @SuppressWarnings("unused")
    private static class LogErrorLoggableClass {

        @Log.error(WARN)
        @Log.error(INFO)
        public void logError() {
        }

        public void empty() {
        }
    }

    /**
     * TODO: implement
     */
    @Test
    public void buildMdcPack() throws NoSuchMethodException {
        // given
//        Method method = MdcLoggableClass.class.getMethod("mdc", String.class, String.class);
        // when
//        MdcPack mdcPack = annotationDefinitionFactory.buildMdcPack(method);
        // then
//        assertThat(mdcPack.getMethod());
    }

    private static class MdcLoggableClass {

        @Mdc(key = "method", value = "")
        public void mdc(@Mdc(key = "a", value = "") String a, String b) {
        }

        public void empty() {
        }
    }
}
