package ru.tinkoff.eclair.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.support.StaticListableBeanFactory;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import ru.tinkoff.eclair.annotation.Log;
import ru.tinkoff.eclair.annotation.Mdc;
import ru.tinkoff.eclair.definition.ErrorLogDefinition;
import ru.tinkoff.eclair.definition.InLogDefinition;
import ru.tinkoff.eclair.definition.OutLogDefinition;
import ru.tinkoff.eclair.format.printer.JacksonPrinter;
import ru.tinkoff.eclair.format.printer.Jaxb2Printer;
import ru.tinkoff.eclair.format.printer.Printer;

import java.lang.reflect.Method;
import java.util.*;

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

    private PrinterResolver printerResolver;
    private AnnotationDefinitionFactory annotationDefinitionFactory;

    @Before
    public void init() {
        Map<String, Object> printers = new LinkedHashMap<>();
        printers.put("xml", xmlPrinter);
        printers.put("json", jsonPrinter);
        printerResolver = new PrinterResolver(new StaticListableBeanFactory(printers), asList(xmlPrinter, jsonPrinter));
        annotationDefinitionFactory = new AnnotationDefinitionFactory(printerResolver);
    }

    @Test
    public void buildInLogDefinitionByLogIn() throws NoSuchMethodException {
        // given
        Set<String> loggerNames = singleton("");
        Method method = LogInLoggableClass.class.getMethod("logIn", String.class, String.class);
        // when
        InLogDefinition definition = annotationDefinitionFactory.buildInLogDefinition(loggerNames, method);
        // then
        assertThat(definition.getLevel(), is(INFO));
        assertThat(definition.getArgLogDefinitions(), hasSize(2));
        assertThat(definition.getArgLogDefinitions().get(0).getPrinter(), is(jsonPrinter));
        assertThat(definition.getArgLogDefinitions().get(1).getPrinter(), is(jsonPrinter));
    }

    @Test
    public void buildInLogDefinitionByLogInLogArg() throws NoSuchMethodException {
        // given
        Set<String> loggerNames = singleton("");
        Method method = LogInLoggableClass.class.getMethod("logInLogArg", String.class, String.class);
        // when
        InLogDefinition definition = annotationDefinitionFactory.buildInLogDefinition(loggerNames, method);
        // then
        assertThat(definition.getLevel(), is(INFO));
        assertThat(definition.getArgLogDefinitions().get(0).getIfEnabledLevel(), is(WARN));
        assertThat(definition.getArgLogDefinitions().get(0).getPrinter(), is(xmlPrinter));
        assertThat(definition.getArgLogDefinitions().get(1).getPrinter(), is(jsonPrinter));
    }

    @Test
    public void buildInLogDefinitionByLog() throws NoSuchMethodException {
        // given
        Set<String> loggerNames = singleton("");
        Method method = LogInLoggableClass.class.getMethod("log", String.class, String.class);
        // when
        InLogDefinition definition = annotationDefinitionFactory.buildInLogDefinition(loggerNames, method);
        // then
        assertThat(definition.getLevel(), is(WARN));
        assertThat(definition.getIfEnabledLevel(), is(ERROR));
        assertThat(definition.getVerboseLevel(), is(TRACE));
        assertThat(definition.getArgLogDefinitions(), hasSize(2));
        assertThat(definition.getArgLogDefinitions().get(0).getPrinter(), is(jsonPrinter));
        assertThat(definition.getArgLogDefinitions().get(1).getPrinter(), is(jsonPrinter));
    }

    @Test
    public void buildInLogDefinitionEmpty() throws NoSuchMethodException {
        // given
        Set<String> loggerNames = singleton("");
        Method method = LogInLoggableClass.class.getMethod("empty", String.class, String.class);
        // when
        InLogDefinition definition = annotationDefinitionFactory.buildInLogDefinition(loggerNames, method);
        // then
        assertThat(definition, nullValue());
    }

    @Test
    public void buildInLogDefinitionByLogArg() throws NoSuchMethodException {
        // given
        Set<String> loggerNames = singleton("");
        Method method = LogInLoggableClass.class.getMethod("logArg", String.class, String.class);
        // when
        InLogDefinition definition = annotationDefinitionFactory.buildInLogDefinition(loggerNames, method);
        // then
        assertThat(definition.getLevel(), is(DEBUG));
        assertThat(definition.getArgLogDefinitions(), hasSize(2));
        assertThat(definition.getArgLogDefinitions().get(0).getIfEnabledLevel(), is(WARN));
        assertThat(definition.getArgLogDefinitions().get(1), nullValue());
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

        public void empty(String a, String b) {
        }

        public void logArg(@Log.arg(ifEnabled = WARN) String a, String b) {
        }
    }

    @Test
    public void buildOutLogDefinition() throws NoSuchMethodException {
        // given
        Set<String> loggerNames = singleton("");
        Method method = LogOutLoggableClass.class.getMethod("logOut");
        // when
        OutLogDefinition definition = annotationDefinitionFactory.buildOutLogDefinition(loggerNames, method);
        // then
        assertThat(definition.getLevel(), is(INFO));
    }

    @Test
    public void buildOutLogDefinitionByLog() throws NoSuchMethodException {
        // given
        Set<String> loggerNames = singleton("");
        Method method = LogOutLoggableClass.class.getMethod("log");
        // when
        OutLogDefinition definition = annotationDefinitionFactory.buildOutLogDefinition(loggerNames, method);
        // then
        assertThat(definition.getLevel(), is(WARN));
        assertThat(definition.getIfEnabledLevel(), is(ERROR));
        assertThat(definition.getVerboseLevel(), is(TRACE));
        assertThat(definition.getPrinter(), is(printerResolver.getDefaultPrinter()));
    }

    @Test
    public void buildOutLogDefinitionEmpty() throws NoSuchMethodException {
        // given
        Set<String> loggerNames = singleton("");
        Method method = LogOutLoggableClass.class.getMethod("empty");
        // when
        OutLogDefinition definition = annotationDefinitionFactory.buildOutLogDefinition(loggerNames, method);
        // then
        assertThat(definition, nullValue());
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

        public void empty() {
        }
    }

    @Test
    public void buildErrorLogDefinitions() throws NoSuchMethodException {
        // given
        Set<String> loggerNames = singleton("");
        Method method = LogErrorLoggableClass.class.getMethod("logError");
        // when
        Set<ErrorLogDefinition> errorLogDefinitions = annotationDefinitionFactory.buildErrorLogDefinitions(loggerNames, method);
        // then
        assertThat(errorLogDefinitions, hasSize(2));
        Iterator<ErrorLogDefinition> iterator = errorLogDefinitions.iterator();
        assertThat(iterator.next().getLevel(), is(WARN));
        assertThat(iterator.next().getLevel(), is(INFO));
    }

    @Test
    public void buildErrorLogDefinitionsEmpty() throws NoSuchMethodException {
        // given
        Set<String> loggerNames = singleton("");
        Method method = LogErrorLoggableClass.class.getMethod("empty");
        // when
        Set<ErrorLogDefinition> errorLogDefinitions = annotationDefinitionFactory.buildErrorLogDefinitions(loggerNames, method);
        // then
        assertThat(errorLogDefinitions, is(empty()));
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
    public void buildMdcPackDefinition() throws NoSuchMethodException {
        // given
//        Method method = MdcLoggableClass.class.getMethod("mdc", String.class, String.class);
        // when
//        MdcPackDefinition definition = annotationDefinitionFactory.buildMdcPackDefinition(method);
        // then
//        assertThat(definition.getMethod());
    }

    private static class MdcLoggableClass {

        @Mdc(key = "method", value = "")
        public void mdc(@Mdc(key = "a", value = "") String a, String b) {
        }

        public void empty() {
        }
    }
}
