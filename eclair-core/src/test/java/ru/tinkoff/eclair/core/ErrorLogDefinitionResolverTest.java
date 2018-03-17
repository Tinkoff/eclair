package ru.tinkoff.eclair.core;

import org.junit.Test;
import ru.tinkoff.eclair.definition.ErrorLogDefinition;
import ru.tinkoff.eclair.definition.ErrorLogDefinitionFactory;

import java.util.LinkedHashSet;
import java.util.Set;

import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * @author Viacheslav Klapatniuk
 */
public class ErrorLogDefinitionResolverTest {

    private final ErrorLogDefinitionResolver errorLogDefinitionResolver = ErrorLogDefinitionResolver.getInstance();

    @Test
    public void resolve() {
        // given
        ErrorLogDefinition aDefinition = ErrorLogDefinitionFactory.newInstance(
                new Class<?>[]{NullPointerException.class, IndexOutOfBoundsException.class},
                new Class<?>[]{StringIndexOutOfBoundsException.class});
        Set<ErrorLogDefinition> errorLogDefinitions = singleton(aDefinition);
        Class<?> causeClass = ArrayIndexOutOfBoundsException.class;
        // when
        ErrorLogDefinition definition = errorLogDefinitionResolver.resolve(errorLogDefinitions, causeClass);
        // then
        assertThat(definition, is(aDefinition));
    }

    @Test
    public void resolveExclude() {
        // given
        ErrorLogDefinition aDefinition = ErrorLogDefinitionFactory.newInstance(new Class<?>[]{Throwable.class}, new Class<?>[]{RuntimeException.class});
        Set<ErrorLogDefinition> errorLogDefinitions = singleton(aDefinition);
        Class<?> causeClass = NullPointerException.class;
        // when
        ErrorLogDefinition definition = errorLogDefinitionResolver.resolve(errorLogDefinitions, causeClass);
        // then
        assertThat(definition, nullValue());
    }

    @Test
    public void resolveNotFound() {
        // given
        ErrorLogDefinition aDefinition = ErrorLogDefinitionFactory.newInstance(new Class<?>[]{Exception.class}, new Class<?>[]{RuntimeException.class});
        Set<ErrorLogDefinition> errorLogDefinitions = singleton(aDefinition);
        Class<?> causeClass = Throwable.class;
        // when
        ErrorLogDefinition definition = errorLogDefinitionResolver.resolve(errorLogDefinitions, causeClass);
        // then
        assertThat(definition, nullValue());
    }

    @Test
    public void resolveMostSpecific() {
        // given
        ErrorLogDefinition aDefinition = ErrorLogDefinitionFactory.newInstance(
                new Class<?>[]{NullPointerException.class, IndexOutOfBoundsException.class},
                new Class<?>[]{StringIndexOutOfBoundsException.class});
        ErrorLogDefinition bDefinition = ErrorLogDefinitionFactory.newInstance(
                new Class<?>[]{ArrayIndexOutOfBoundsException.class},
                new Class<?>[]{});
        Set<ErrorLogDefinition> errorLogDefinitions = new LinkedHashSet<>(asList(aDefinition, bDefinition));
        Class<?> causeClass = ArrayIndexOutOfBoundsException.class;
        // when
        ErrorLogDefinition definition = errorLogDefinitionResolver.resolve(errorLogDefinitions, causeClass);
        // then
        assertThat(definition, is(bDefinition));
    }
}
