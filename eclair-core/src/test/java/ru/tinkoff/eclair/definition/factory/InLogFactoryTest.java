package ru.tinkoff.eclair.definition.factory;

import org.junit.Test;
import ru.tinkoff.eclair.annotation.Log;
import ru.tinkoff.eclair.definition.InLog;
import ru.tinkoff.eclair.printer.Printer;
import ru.tinkoff.eclair.printer.ToStringPrinter;

import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.singletonMap;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.springframework.boot.logging.LogLevel.TRACE;
import static org.springframework.boot.logging.LogLevel.WARN;
import static org.springframework.core.annotation.AnnotationUtils.synthesizeAnnotation;

/**
 * @author Viacheslav Klapatniuk
 */
public class InLogFactoryTest {

    @Test
    public void newInstance() {
        // given
        Log.in logIn = givenLogIn();
        Printer printer = new ToStringPrinter();
        // when
        InLog inLog = InLogFactory.newInstance(logIn, printer);
        // then
        assertThat(inLog.getLevel(), is(WARN));
        assertThat(inLog.getIfEnabledLevel(), is(WARN));
        assertThat(inLog.getVerboseLevel(), is(TRACE));
        assertThat(inLog.getPrinter(), is(printer));
    }

    private Log.in givenLogIn() {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("level", WARN);
        attributes.put("ifEnabled", WARN);
        attributes.put("verbose", TRACE);
        attributes.put("printer", "json");
        return synthesizeAnnotation(attributes, Log.in.class, null);
    }

    @Test
    public void newInstanceByValue() {
        // given
        Log.in logIn = givenLogInByValue();
        Printer printer = new ToStringPrinter();
        // when
        InLog inLog = InLogFactory.newInstance(logIn, printer);
        // then
        assertThat(inLog.getLevel(), is(WARN));
    }

    private Log.in givenLogInByValue() {
        return synthesizeAnnotation(singletonMap("value", WARN), Log.in.class, null);
    }
}
