package ru.tinkoff.eclair.core;

import org.junit.Test;
import org.springframework.boot.logging.LogLevel;
import ru.tinkoff.eclair.annotation.Log;
import ru.tinkoff.eclair.definition.LogDefinition;
import ru.tinkoff.eclair.definition.OutLog;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.springframework.boot.logging.LogLevel.*;
import static org.springframework.core.annotation.AnnotationUtils.synthesizeAnnotation;

/**
 * @author Viacheslav Klapatniuk
 */
public class ExpectedLevelResolverTest {

    private final ExpectedLevelResolver expectedLevelResolver = ExpectedLevelResolver.getInstance();

    @Test
    public void applyErrorWarn() {
        // given
        LogDefinition definition = givenLogDefinition(ERROR, WARN);
        // when
        LogLevel level = expectedLevelResolver.apply(definition);
        // then
        assertThat(level, is(WARN));
    }

    @Test
    public void applyTraceDebug() {
        // given
        LogDefinition definition = givenLogDefinition(TRACE, DEBUG);
        // when
        LogLevel level = expectedLevelResolver.apply(definition);
        // then
        assertThat(level, is(TRACE));
    }

    @Test
    public void applyInfoInfo() {
        // given
        LogDefinition definition = givenLogDefinition(INFO, INFO);
        // when
        LogLevel level = expectedLevelResolver.apply(definition);
        // then
        assertThat(level, is(INFO));
    }

    private LogDefinition givenLogDefinition(LogLevel level, LogLevel ifEnabled) {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("level", level);
        attributes.put("ifEnabled", ifEnabled);
        return new OutLog(synthesizeAnnotation(attributes, Log.out.class, null), null);
    }
}
