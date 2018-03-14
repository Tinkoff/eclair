package ru.tinkoff.integration.eclair.definition;

import org.junit.Test;
import ru.tinkoff.integration.eclair.annotation.Mdc;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.springframework.core.annotation.AnnotationUtils.synthesizeAnnotation;
import static ru.tinkoff.integration.eclair.annotation.Scope.GLOBAL;

/**
 * @author Viacheslav Klapatniuk
 */
public class MdcDefinitionTest {

    @Test
    public void newInstance() {
        // given
        Mdc mdc = givenMdc();
        // when
        MdcDefinition definition = new MdcDefinition(mdc);
        // then
        assertThat(definition.getKey(), is("key"));
        assertThat(definition.getValue(), is("value"));
        assertThat(definition.getScope(), is(GLOBAL));
    }

    private Mdc givenMdc() {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("key", "key");
        attributes.put("value", "value");
        attributes.put("scope", GLOBAL);
        return synthesizeAnnotation(attributes, Mdc.class, null);
    }
}
