package ru.tinkoff.integration.eclair.definition;

import org.junit.Test;
import ru.tinkoff.integration.eclair.annotation.Log;
import ru.tinkoff.integration.eclair.format.printer.Printer;
import ru.tinkoff.integration.eclair.format.printer.ToStringPrinter;

import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.singletonMap;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.springframework.boot.logging.LogLevel.WARN;
import static org.springframework.core.annotation.AnnotationUtils.synthesizeAnnotation;

/**
 * @author Viacheslav Klapatniuk
 */
public class ArgLogDefinitionTest {

    @Test
    public void newInstance() {
        // given
        Log.arg logArg = givenLogArg();
        Printer printer = givenPrinter();
        // when
        ArgLogDefinition definition = new ArgLogDefinition(logArg, printer);
        // then
        assertThat(definition.getIfEnabledLevel(), is(WARN));
        assertThat(definition.getPrinter(), is(printer));
        assertThat(definition.getMaskExpressions(), hasSize(1));
        assertThat(definition.getMaskExpressions().get(0), is("mask"));
    }

    private Log.arg givenLogArg() {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("ifEnabled", WARN);
        attributes.put("printer", "json");
        attributes.put("mask", "mask");
        return synthesizeAnnotation(attributes, Log.arg.class, null);
    }

    private Printer givenPrinter() {
        return new ToStringPrinter();
    }

    @Test
    public void newInstanceByValue() {
        // given
        Log.arg logArg = givenLogArgByValue();
        Printer printer = givenPrinter();
        // when
        ArgLogDefinition definition = new ArgLogDefinition(logArg, printer);
        // then
        assertThat(definition.getIfEnabledLevel(), is(WARN));
    }

    private Log.arg givenLogArgByValue() {
        return synthesizeAnnotation(singletonMap("value", WARN), Log.arg.class, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void newInstanceNull() {
        // given
        Log.arg logArg = null;
        Printer printer = givenPrinter();
        // when
        ArgLogDefinition definition = new ArgLogDefinition(logArg, printer);
        // then
        assertThat(definition, nullValue());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void newInstanceImmutable() {
        // given
        Log.arg logArg = givenLogArg();
        Printer printer = givenPrinter();
        // when
        ArgLogDefinition definition = new ArgLogDefinition(logArg, printer);
        // then
        definition.getMaskExpressions().add("additional");
    }
}
