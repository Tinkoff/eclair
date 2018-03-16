package ru.tinkoff.eclair.core;

import org.junit.Test;
import org.springframework.boot.logging.LogLevel;
import ru.tinkoff.eclair.annotation.Log;

import java.lang.annotation.Annotation;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.springframework.boot.logging.LogLevel.DEBUG;
import static org.springframework.boot.logging.LogLevel.OFF;
import static org.springframework.core.annotation.AnnotationUtils.synthesizeAnnotation;

/**
 * @author Viacheslav Klapatniuk
 */
public class AnnotationAttributeTest {

    @Test
    public void extract() {
        // given
        Annotation annotation = synthesizeAnnotation(Log.class);
        // when
        Object level = AnnotationAttribute.LEVEL.extract(annotation);
        Object ifEnabled = AnnotationAttribute.IF_ENABLED.extract(annotation);
        Object logger = AnnotationAttribute.LOGGER.extract(annotation);
        // then
        assertTrue(LogLevel.class.isInstance(level));
        assertThat(level, is(DEBUG));
        assertTrue(LogLevel.class.isInstance(ifEnabled));
        assertThat(ifEnabled, is(OFF));
        assertTrue(String.class.isInstance(logger));
        assertThat(logger, is(""));
    }
}
