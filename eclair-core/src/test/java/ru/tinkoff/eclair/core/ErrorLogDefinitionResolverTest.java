package ru.tinkoff.eclair.core;

import org.junit.Test;
import ru.tinkoff.eclair.definition.ErrorLogDefinition;
import ru.tinkoff.eclair.definition.ErrorLogDefinitionFactory;

import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
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
        List<ErrorLogDefinition> errorLogDefinitions = singletonList(aDefinition);
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
        List<ErrorLogDefinition> errorLogDefinitions = singletonList(aDefinition);
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
        List<ErrorLogDefinition> errorLogDefinitions = singletonList(aDefinition);
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
        List<ErrorLogDefinition> errorLogDefinitions = asList(aDefinition, bDefinition);
        Class<?> causeClass = ArrayIndexOutOfBoundsException.class;
        // when
        ErrorLogDefinition definition = errorLogDefinitionResolver.resolve(errorLogDefinitions, causeClass);
        // then
        assertThat(definition, is(bDefinition));
    }
}
