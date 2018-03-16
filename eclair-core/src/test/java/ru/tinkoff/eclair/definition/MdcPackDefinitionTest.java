package ru.tinkoff.eclair.definition;

import org.junit.Test;
import ru.tinkoff.eclair.annotation.Mdc;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Arrays.asList;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.springframework.core.annotation.AnnotationUtils.synthesizeAnnotation;

/**
 * @author Viacheslav Klapatniuk
 */
public class MdcPackDefinitionTest {

    @Test
    public void newInstance() throws NoSuchMethodException {
        // given
        Method method = givenMethod();
        Set<Mdc> methodMdcs = givenMethodMdcs();
        List<Set<Mdc>> argumentMdcs = givenArgumentMdcs();
        // when
        MdcPackDefinition definition = MdcPackDefinition.newInstance(method, methodMdcs, argumentMdcs);
        // then
        assertThat(definition.getMethod(), is(method));
        thenMdcPackDefinition(definition.getMethodDefinitions());
        thenArgumentDefinitions(definition.getArgumentDefinitions());
    }

    private Method givenMethod() throws NoSuchMethodException {
        return MdcPackDefinitionTest.class.getMethod("annotatedMethod", String.class, String.class);
    }

    @SuppressWarnings("unused")
    public void annotatedMethod(String a, String b) {
    }

    private Set<Mdc> givenMethodMdcs() {
        return singleton(givenMdc("method"));
    }

    private List<Set<Mdc>> givenArgumentMdcs() {
        return asList(singleton(givenMdc("a")), singleton(givenMdc("b")));
    }

    private Mdc givenMdc(String key) {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("key", key);
        attributes.put("value", "value");
        return synthesizeAnnotation(attributes, Mdc.class, null);
    }

    private void thenMdcPackDefinition(Set<MdcDefinition> methodDefinitions) {
        assertThat(methodDefinitions, hasSize(1));
        assertThat(methodDefinitions.iterator().next(), notNullValue());
    }

    private void thenArgumentDefinitions(List<Set<MdcDefinition>> argumentDefinitions) {
        assertThat(argumentDefinitions, hasSize(2));
        thenAArgumentDefinition(argumentDefinitions.get(0));
        thenBArgumentDefinition(argumentDefinitions.get(1));
    }

    private void thenAArgumentDefinition(Set<MdcDefinition> definitions) {
        assertThat(definitions, hasSize(1));
        assertThat(definitions.iterator().next().getKey(), is("a"));
    }

    private void thenBArgumentDefinition(Set<MdcDefinition> definitions) {
        assertThat(definitions, hasSize(1));
        assertThat(definitions.iterator().next().getKey(), is("b"));
    }

    @Test
    public void newInstanceNull() throws NoSuchMethodException {
        // given
        Method method = givenMethod();
        Set<Mdc> methodMdcs = emptySet();
        List<Set<Mdc>> argumentMdcs = asList(emptySet(), emptySet());
        // when
        MdcPackDefinition definition = MdcPackDefinition.newInstance(method, methodMdcs, argumentMdcs);
        // then
        assertThat(definition, nullValue());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void newInstanceImmutableMethodMdcDefinitions() throws NoSuchMethodException {
        // given
        Method method = givenMethod();
        Set<Mdc> methodMdcs = givenMethodMdcs();
        List<Set<Mdc>> argumentMdcs = givenArgumentMdcs();
        // when
        MdcPackDefinition definition = MdcPackDefinition.newInstance(method, methodMdcs, argumentMdcs);
        // then
        definition.getMethodDefinitions().add(new MdcDefinition(givenMdc("")));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void newInstanceImmutableArgumentMdcDefinitions() throws NoSuchMethodException {
        // given
        Method method = givenMethod();
        Set<Mdc> methodMdcs = givenMethodMdcs();
        List<Set<Mdc>> argumentMdcs = givenArgumentMdcs();
        // when
        MdcPackDefinition definition = MdcPackDefinition.newInstance(method, methodMdcs, argumentMdcs);
        // then
        definition.getArgumentDefinitions().add(emptySet());
    }
}
