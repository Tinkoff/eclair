package ru.tinkoff.integration.eclair.definition;

import org.junit.Assert;
import org.junit.Test;
import ru.tinkoff.integration.eclair.annotation.Log;
import ru.tinkoff.integration.eclair.format.printer.ToStringPrinter;

import java.lang.reflect.Method;
import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.springframework.core.annotation.AnnotationUtils.synthesizeAnnotation;

/**
 * @author Viacheslav Klapatniuk
 */
public class LogDefinitionTest {

    @Test
    public void newInstance() throws NoSuchMethodException {
        // given
        Method method = LogDefinitionTest.class.getMethod("newInstance");
        InLogDefinition inLogDefinition = givenInLogDefinition();
        OutLogDefinition outLogDefinition = givenOutLogDefinition();
        List<ErrorLogDefinition> errorLogDefinitions =
                singletonList(ErrorLogDefinitionFactory.newInstance(new Class<?>[]{Throwable.class}, new Class<?>[]{}));
        // when
        LogDefinition definition = LogDefinition.newInstance(method, inLogDefinition, outLogDefinition, errorLogDefinitions);
        // then
        Assert.assertThat(definition.getMethod(), is(method));
        Assert.assertThat(definition.getInLogDefinition(), is(inLogDefinition));
        Assert.assertThat(definition.getOutLogDefinition(), is(outLogDefinition));
    }

    @Test
    public void newInstanceNull() throws NoSuchMethodException {
        // given
        Method method = LogDefinitionTest.class.getMethod("newInstance");
        InLogDefinition inLogDefinition = null;
        OutLogDefinition outLogDefinition = null;
        List<ErrorLogDefinition> errorLogDefinitions = emptyList();
        // when
        LogDefinition definition = LogDefinition.newInstance(method, inLogDefinition, outLogDefinition, errorLogDefinitions);
        // then
        Assert.assertThat(definition, nullValue());
    }

    @Test
    public void findErrorLogDefinition() throws NoSuchMethodException {
        // given
        Method method = LogDefinitionTest.class.getMethod("newInstance");
        InLogDefinition inLogDefinition = givenInLogDefinition();
        OutLogDefinition outLogDefinition = givenOutLogDefinition();
        ErrorLogDefinition errorLogDefinition = ErrorLogDefinitionFactory.newInstance(new Class<?>[]{Exception.class}, new Class<?>[]{Error.class});
        List<ErrorLogDefinition> errorLogDefinitions = singletonList(errorLogDefinition);
        // when
        LogDefinition definition = LogDefinition.newInstance(method, inLogDefinition, outLogDefinition, errorLogDefinitions);
        // then
        Assert.assertThat(definition.findErrorLogDefinition(Throwable.class), nullValue());
        Assert.assertThat(definition.findErrorLogDefinition(RuntimeException.class), is(errorLogDefinition));
        Assert.assertThat(definition.findErrorLogDefinition(RuntimeException.class), is(errorLogDefinition));
        Assert.assertThat(definition.findErrorLogDefinition(OutOfMemoryError.class), nullValue());
    }

    private InLogDefinition givenInLogDefinition() {
        Log.in logIn = synthesizeAnnotation(Log.in.class);
        List<ArgLogDefinition> argLogDefinitions = singletonList(new ArgLogDefinition(synthesizeAnnotation(Log.arg.class), new ToStringPrinter()));
        return InLogDefinition.newInstance(logIn, argLogDefinitions);
    }

    private OutLogDefinition givenOutLogDefinition() {
        Log.out logOut = synthesizeAnnotation(Log.out.class);
        return new OutLogDefinition(logOut, new ToStringPrinter());
    }
}
