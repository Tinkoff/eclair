package ru.tinkoff.eclair.definition.factory;

import org.junit.Test;
import ru.tinkoff.eclair.annotation.Mdc;
import ru.tinkoff.eclair.definition.ParameterMdc;
import ru.tinkoff.eclair.definition.MethodMdc;

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
public class MethodMdcFactoryTest {

    @Test
    public void newInstance() throws NoSuchMethodException {
        // given
        Method method = givenMethod();
        Set<Mdc> methodMdcs = givenMethodMdcs();
        List<Set<Mdc>> argumentMdcs = givenArgumentMdcs();
        // when
        MethodMdc methodMdc = MethodMdcFactory.newInstance(method, methodMdcs, argumentMdcs);
        // then
        assertThat(methodMdc.getMethod(), is(method));
        thenMethodDefinition(methodMdc.getMethodDefinitions());
        thenParameterDefinitions(methodMdc.getParameterDefinitions());
    }

    private Method givenMethod() throws NoSuchMethodException {
        return MethodMdcFactoryTest.class.getMethod("annotatedMethod", String.class, String.class);
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

    private void thenMethodDefinition(Set<ParameterMdc> definitions) {
        assertThat(definitions, hasSize(1));
        assertThat(definitions.iterator().next(), notNullValue());
    }

    private void thenParameterDefinitions(List<Set<ParameterMdc>> definitions) {
        assertThat(definitions, hasSize(2));
        thenAParameterDefinition(definitions.get(0));
        thenBParameterDefinition(definitions.get(1));
    }

    private void thenAParameterDefinition(Set<ParameterMdc> definitions) {
        assertThat(definitions, hasSize(1));
        assertThat(definitions.iterator().next().getKey(), is("a"));
    }

    private void thenBParameterDefinition(Set<ParameterMdc> definitions) {
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
        MethodMdc methodMdc = MethodMdcFactory.newInstance(method, methodMdcs, argumentMdcs);
        // then
        assertThat(methodMdc, nullValue());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void newInstanceImmutableMethodDefinitions() throws NoSuchMethodException {
        // given
        Method method = givenMethod();
        Set<Mdc> methodMdcs = givenMethodMdcs();
        List<Set<Mdc>> argumentMdcs = givenArgumentMdcs();
        // when
        MethodMdc methodMdc = MethodMdcFactory.newInstance(method, methodMdcs, argumentMdcs);
        // then
        methodMdc.getMethodDefinitions().add(ParameterMdcFactory.newInstance(givenMdc("")));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void newInstanceImmutableParameterDefinitions() throws NoSuchMethodException {
        // given
        Method method = givenMethod();
        Set<Mdc> methodMdcs = givenMethodMdcs();
        List<Set<Mdc>> argumentMdcs = givenArgumentMdcs();
        // when
        MethodMdc methodMdc = MethodMdcFactory.newInstance(method, methodMdcs, argumentMdcs);
        // then
        methodMdc.getParameterDefinitions().add(emptySet());
    }
}
