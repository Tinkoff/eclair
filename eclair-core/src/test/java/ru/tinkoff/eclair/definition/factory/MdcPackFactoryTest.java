package ru.tinkoff.eclair.definition.factory;

import org.junit.Test;
import ru.tinkoff.eclair.annotation.Mdc;
import ru.tinkoff.eclair.definition.MdcDefinition;
import ru.tinkoff.eclair.definition.MdcPack;

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
public class MdcPackFactoryTest {

    @Test
    public void newInstance() throws NoSuchMethodException {
        // given
        Method method = givenMethod();
        Set<Mdc> methodMdcs = givenMethodMdcs();
        List<Set<Mdc>> argumentMdcs = givenArgumentMdcs();
        // when
        MdcPack mdcPack = MdcPackFactory.newInstance(method, methodMdcs, argumentMdcs);
        // then
        assertThat(mdcPack.getMethod(), is(method));
        thenMethodDefinition(mdcPack.getMethodDefinitions());
        thenParameterDefinitions(mdcPack.getParameterDefinitions());
    }

    private Method givenMethod() throws NoSuchMethodException {
        return MdcPackFactoryTest.class.getMethod("annotatedMethod", String.class, String.class);
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

    private void thenMethodDefinition(Set<MdcDefinition> definitions) {
        assertThat(definitions, hasSize(1));
        assertThat(definitions.iterator().next(), notNullValue());
    }

    private void thenParameterDefinitions(List<Set<MdcDefinition>> definitions) {
        assertThat(definitions, hasSize(2));
        thenAParameterDefinition(definitions.get(0));
        thenBParameterDefinition(definitions.get(1));
    }

    private void thenAParameterDefinition(Set<MdcDefinition> definitions) {
        assertThat(definitions, hasSize(1));
        assertThat(definitions.iterator().next().getKey(), is("a"));
    }

    private void thenBParameterDefinition(Set<MdcDefinition> definitions) {
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
        MdcPack mdcPack = MdcPackFactory.newInstance(method, methodMdcs, argumentMdcs);
        // then
        assertThat(mdcPack, nullValue());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void newInstanceImmutableMethodDefinitions() throws NoSuchMethodException {
        // given
        Method method = givenMethod();
        Set<Mdc> methodMdcs = givenMethodMdcs();
        List<Set<Mdc>> argumentMdcs = givenArgumentMdcs();
        // when
        MdcPack mdcPack = MdcPackFactory.newInstance(method, methodMdcs, argumentMdcs);
        // then
        mdcPack.getMethodDefinitions().add(MdcDefinitionFactory.newInstance(givenMdc("")));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void newInstanceImmutableParameterDefinitions() throws NoSuchMethodException {
        // given
        Method method = givenMethod();
        Set<Mdc> methodMdcs = givenMethodMdcs();
        List<Set<Mdc>> argumentMdcs = givenArgumentMdcs();
        // when
        MdcPack mdcPack = MdcPackFactory.newInstance(method, methodMdcs, argumentMdcs);
        // then
        mdcPack.getParameterDefinitions().add(emptySet());
    }
}
