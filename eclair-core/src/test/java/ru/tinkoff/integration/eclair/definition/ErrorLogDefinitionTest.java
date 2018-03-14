package ru.tinkoff.integration.eclair.definition;

import org.hamcrest.Matchers;
import org.junit.Test;
import ru.tinkoff.integration.eclair.annotation.Log;

import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.singletonMap;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.springframework.boot.logging.LogLevel.WARN;
import static org.springframework.core.annotation.AnnotationUtils.synthesizeAnnotation;

/**
 * @author Viacheslav Klapatniuk
 */
public class ErrorLogDefinitionTest {

    @Test
    public void newInstance() {
        // given
        Log.error logError = givenLogError();
        // when
        ErrorLogDefinition definition = new ErrorLogDefinition(logError);
        // then
        assertThat(definition.getLevel(), is(WARN));
        assertThat(definition.getIfEnabledLevel(), is(WARN));
        assertThat(definition.getIncludes(), hasSize(1));
        assertThat(definition.getIncludes(), Matchers.<Class<?>>contains(RuntimeException.class));
        assertThat(definition.getExcludes(), hasSize(1));
        assertThat(definition.getExcludes(), Matchers.<Class<?>>contains(NullPointerException.class));
    }

    private Log.error givenLogError() {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("level", WARN);
        attributes.put("ifEnabled", WARN);
        attributes.put("ofType", RuntimeException.class);
        attributes.put("exclude", NullPointerException.class);
        return synthesizeAnnotation(attributes, Log.error.class, null);
    }

    @Test
    public void newInstanceByValue() {
        // given
        Log.error logError = givenLogErrorByValue();
        // when
        ErrorLogDefinition definition = new ErrorLogDefinition(logError);
        // then
        assertThat(definition.getLevel(), is(WARN));
    }

    private Log.error givenLogErrorByValue() {
        return synthesizeAnnotation(singletonMap("value", WARN), Log.error.class, null);
    }
}
