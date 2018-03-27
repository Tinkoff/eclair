package ru.tinkoff.eclair.definition.factory;

import org.junit.Test;
import ru.tinkoff.eclair.annotation.Log;
import ru.tinkoff.eclair.definition.ParameterLog;
import ru.tinkoff.eclair.printer.Printer;
import ru.tinkoff.eclair.printer.ToStringPrinter;

import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.singletonMap;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.springframework.boot.logging.LogLevel.ERROR;
import static org.springframework.boot.logging.LogLevel.INFO;
import static org.springframework.boot.logging.LogLevel.WARN;
import static org.springframework.core.annotation.AnnotationUtils.synthesizeAnnotation;

/**
 * @author Viacheslav Klapatniuk
 */
public class ParameterLogFactoryTest {

    @Test
    public void newInstance() {
        // given
        Log log = givenLog();
        Printer printer = givenPrinter();
        // when
        ParameterLog parameterLog = ParameterLogFactory.newInstance(log, printer);
        // then
        assertThat(parameterLog.getLevel(), is(INFO));
        assertThat(parameterLog.getIfEnabledLevel(), is(WARN));
        assertThat(parameterLog.getVerboseLevel(), is(ERROR));
        assertThat(parameterLog.getPrinter(), is(printer));
    }

    private Log givenLog() {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("level", INFO);
        attributes.put("ifEnabled", WARN);
        attributes.put("verbose", ERROR);
        attributes.put("printer", "json");
        return synthesizeAnnotation(attributes, Log.class, null);
    }

    private Printer givenPrinter() {
        return new ToStringPrinter();
    }

    /**
     * TODO: Incorrect test, because annotation always synthesized correct (according to {@link org.springframework.core.annotation.AliasFor})
     */
    @Test
    public void newInstanceByValue() {
        // given
        Log log = givenLogByValue();
        Printer printer = givenPrinter();
        // when
        ParameterLog parameterLog = ParameterLogFactory.newInstance(log, printer);
        // then
        assertThat(parameterLog.getLevel(), is(WARN));
    }

    private Log givenLogByValue() {
        return synthesizeAnnotation(singletonMap("value", WARN), Log.class, null);
    }

    @Test(expected = NullPointerException.class)
    public void newInstanceNull() {
        // given
        Log log = null;
        Printer printer = givenPrinter();
        // when
        ParameterLog parameterLog = ParameterLogFactory.newInstance(log, printer);
        // then
        assertThat(parameterLog, nullValue());
    }
}
