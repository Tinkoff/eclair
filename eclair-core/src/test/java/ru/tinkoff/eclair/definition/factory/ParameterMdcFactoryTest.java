package ru.tinkoff.eclair.definition.factory;

import org.junit.Test;
import ru.tinkoff.eclair.annotation.Mdc;
import ru.tinkoff.eclair.definition.ParameterMdc;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.springframework.core.annotation.AnnotationUtils.synthesizeAnnotation;

/**
 * @author Viacheslav Klapatniuk
 */
public class ParameterMdcFactoryTest {

    @Test
    public void newInstance() {
        // given
        Mdc mdc = givenMdc();
        // when
        ParameterMdc definition = ParameterMdcFactory.newInstance(mdc);
        // then
        assertThat(definition.getKey(), is("key"));
        assertThat(definition.getValue(), is("value"));
        assertTrue(definition.isGlobal());
    }

    private Mdc givenMdc() {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("key", "key");
        attributes.put("value", "value");
        attributes.put("global", true);
        return synthesizeAnnotation(attributes, Mdc.class, null);
    }
}
