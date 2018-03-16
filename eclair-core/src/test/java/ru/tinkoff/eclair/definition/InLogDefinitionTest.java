package ru.tinkoff.eclair.definition;

import org.junit.Test;
import ru.tinkoff.eclair.annotation.Log;
import ru.tinkoff.eclair.format.printer.ToStringPrinter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.*;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.springframework.boot.logging.LogLevel.TRACE;
import static org.springframework.boot.logging.LogLevel.WARN;
import static org.springframework.core.annotation.AnnotationUtils.synthesizeAnnotation;

/**
 * @author Viacheslav Klapatniuk
 */
public class InLogDefinitionTest {

    @Test
    public void newInstance() {
        // given
        Log.in logIn = givenLogIn();
        ArgLogDefinition argLogDefinition = givenArgLogDefinition();
        List<ArgLogDefinition> argLogDefinitions = singletonList(argLogDefinition);
        // when
        InLogDefinition definition = InLogDefinition.newInstance(logIn, argLogDefinitions);
        // then
        assertThat(definition.getLevel(), is(WARN));
        assertThat(definition.getIfEnabledLevel(), is(WARN));
        assertThat(definition.getVerboseLevel(), is(TRACE));
        assertThat(definition.getArgLogDefinitions(), hasSize(1));
        assertThat(definition.getArgLogDefinitions().get(0), is(argLogDefinition));
    }

    private Log.in givenLogIn() {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("level", WARN);
        attributes.put("ifEnabled", WARN);
        attributes.put("verbose", TRACE);
        attributes.put("printer", "json");
        return synthesizeAnnotation(attributes, Log.in.class, null);
    }

    private ArgLogDefinition givenArgLogDefinition() {
        return new ArgLogDefinition(synthesizeAnnotation(Log.arg.class), new ToStringPrinter());
    }

    @Test
    public void newInstanceByValue() {
        // given
        Log.in logIn = givenLogInByValue();
        List<ArgLogDefinition> argLogDefinitions = singletonList(givenArgLogDefinition());
        // when
        InLogDefinition definition = InLogDefinition.newInstance(logIn, argLogDefinitions);
        // then
        assertThat(definition.getLevel(), is(WARN));
    }

    private Log.in givenLogInByValue() {
        return synthesizeAnnotation(singletonMap("value", WARN), Log.in.class, null);
    }

    @Test
    public void newInstanceNull() {
        // given
        Log.in logIn = null;
        List<ArgLogDefinition> argLogDefinitions = emptyList();
        // when
        InLogDefinition definition = InLogDefinition.newInstance(logIn, argLogDefinitions);
        // then
        assertThat(definition, nullValue());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void newInstanceImmutable() {
        // given
        Log.in logIn = givenLogIn();
        List<ArgLogDefinition> argLogDefinitions = new ArrayList<>();
        // when
        InLogDefinition definition = InLogDefinition.newInstance(logIn, argLogDefinitions);
        // then
        definition.getArgLogDefinitions().add(givenArgLogDefinition());
    }
}
