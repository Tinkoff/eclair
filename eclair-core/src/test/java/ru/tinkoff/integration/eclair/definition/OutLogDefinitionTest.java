package ru.tinkoff.integration.eclair.definition;

import org.junit.Test;
import ru.tinkoff.integration.eclair.annotation.Log;
import ru.tinkoff.integration.eclair.format.printer.Printer;
import ru.tinkoff.integration.eclair.format.printer.ToStringPrinter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonMap;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.springframework.boot.logging.LogLevel.WARN;
import static org.springframework.core.annotation.AnnotationUtils.synthesizeAnnotation;
import static ru.tinkoff.integration.eclair.annotation.Verbose.NEVER;

/**
 * @author Viacheslav Klapatniuk
 */
public class OutLogDefinitionTest {

    @Test
    public void newInstance() {
        // given
        Log.out logOut = givenLogOut();
        Printer printer = givenPrinter();
        // when
        OutLogDefinition definition = new OutLogDefinition(logOut, printer);
        // then
        assertThat(definition.getLevel(), is(WARN));
        assertThat(definition.getIfEnabledLevel(), is(WARN));
        assertThat(definition.getVerbosePolicy(), is(NEVER));
        assertThat(definition.getPrinter(), is(printer));
        List<String> maskExpressions = definition.getMaskExpressions();
        assertThat(maskExpressions, hasSize(1));
        assertThat(maskExpressions.get(0), is("mask"));
    }

    private Log.out givenLogOut() {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("level", WARN);
        attributes.put("ifEnabled", WARN);
        attributes.put("verbose", NEVER);
        attributes.put("mask", "mask");
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
        OutLogDefinition definition = new OutLogDefinition(logOut, printer);
        // then
        assertThat(definition.getLevel(), is(WARN));
    }

    private Log.out givenLogOutByValue() {
        return synthesizeAnnotation(singletonMap("value", WARN), Log.out.class, null);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void newInstanceImmutable() {
        // given
        Log.out logOut = givenLogOut();
        Printer printer = givenPrinter();
        // when
        OutLogDefinition definition = new OutLogDefinition(logOut, printer);
        // then
        definition.getMaskExpressions().add("mask");
    }
}
