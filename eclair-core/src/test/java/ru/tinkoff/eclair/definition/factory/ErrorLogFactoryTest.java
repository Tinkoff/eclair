package ru.tinkoff.eclair.definition.factory;

import org.hamcrest.Matchers;
import org.junit.Test;
import ru.tinkoff.eclair.annotation.Log;
import ru.tinkoff.eclair.definition.ErrorLog;

import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.singletonMap;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.springframework.boot.logging.LogLevel.WARN;
import static org.springframework.core.annotation.AnnotationUtils.synthesizeAnnotation;

/**
 * TODO: add verbose level check
 *
 * @author Viacheslav Klapatniuk
 */
public class ErrorLogFactoryTest {

    @Test
    public void newInstance() {
        // given
        Log.error logError = givenLogError();
        // when
        ErrorLog errorLog = ErrorLogFactory.newInstance(logError);
        // then
        assertThat(errorLog.getLevel(), is(WARN));
        assertThat(errorLog.getIfEnabledLevel(), is(WARN));
        assertThat(errorLog.getIncludes(), hasSize(1));
        assertThat(errorLog.getIncludes(), Matchers.<Class<?>>contains(RuntimeException.class));
        assertThat(errorLog.getExcludes(), hasSize(1));
        assertThat(errorLog.getExcludes(), Matchers.<Class<?>>contains(NullPointerException.class));
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
        ErrorLog errorLog = ErrorLogFactory.newInstance(logError);
        // then
        assertThat(errorLog.getLevel(), is(WARN));
    }

    private Log.error givenLogErrorByValue() {
        return synthesizeAnnotation(singletonMap("value", WARN), Log.error.class, null);
    }
}
