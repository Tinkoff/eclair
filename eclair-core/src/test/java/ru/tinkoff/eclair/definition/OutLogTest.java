package ru.tinkoff.eclair.definition;

import org.junit.Test;
import ru.tinkoff.eclair.annotation.Log;
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
public class OutLogTest {

    @Test
    public void newInstance() {
        // given
        Log.out logOut = givenLogOut();
        Printer printer = givenPrinter();
        // when
        OutLog outLog = new OutLog(logOut, printer);
        // then
        assertThat(outLog.getLevel(), is(WARN));
        assertThat(outLog.getIfEnabledLevel(), is(WARN));
        assertThat(outLog.getVerboseLevel(), is(TRACE));
        assertThat(outLog.getPrinter(), is(printer));
    }

    private Log.out givenLogOut() {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("level", WARN);
        attributes.put("ifEnabled", WARN);
        attributes.put("verbose", TRACE);
        return synthesizeAnnotation(attributes, Log.out.class, null);
    }

    private Printer givenPrinter() {
        return new ToStringPrinter();
    }

    @Test
    public void newInstanceByValue() {
        // given
        Log.out logOut = givenLogOutByValue();
        Printer printer = givenPrinter();
        // when
        OutLog outLog = new OutLog(logOut, printer);
        // then
        assertThat(outLog.getLevel(), is(WARN));
    }

    private Log.out givenLogOutByValue() {
        return synthesizeAnnotation(singletonMap("value", WARN), Log.out.class, null);
    }
}
