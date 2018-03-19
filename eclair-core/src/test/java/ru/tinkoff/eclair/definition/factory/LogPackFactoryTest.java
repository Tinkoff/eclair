package ru.tinkoff.eclair.definition.factory;

import org.junit.Assert;
import org.junit.Test;
import ru.tinkoff.eclair.annotation.Log;
import ru.tinkoff.eclair.definition.*;
import ru.tinkoff.eclair.printer.ToStringPrinter;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

import static java.util.Collections.*;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.springframework.core.annotation.AnnotationUtils.synthesizeAnnotation;

/**
 * @author Viacheslav Klapatniuk
 */
public class LogPackFactoryTest {

    @Test
    public void newInstance() throws NoSuchMethodException {
        // given
        Method method = LogPackFactoryTest.class.getMethod("newInstance");
        InLog inLog = givenInLog();
        OutLog outLog = givenOutLog();
        Set<ErrorLog> errorLogs = singleton(TestErrorLogFactory.newInstance(new Class<?>[]{Throwable.class}, new Class<?>[]{}));
        // when
        LogPack logPack = LogPackFactory.newInstance(method, inLog, outLog, errorLogs);
        // then
        Assert.assertThat(logPack.getMethod(), is(method));
        Assert.assertThat(logPack.getInLog(), is(inLog));
        Assert.assertThat(logPack.getOutLog(), is(outLog));
    }

    @Test
    public void newInstanceNull() throws NoSuchMethodException {
        // given
        Method method = LogPackFactoryTest.class.getMethod("newInstance");
        InLog inLog = null;
        OutLog outLog = null;
        Set<ErrorLog> errorLogs = emptySet();
        // when
        LogPack logPack = LogPackFactory.newInstance(method, inLog, outLog, errorLogs);
        // then
        Assert.assertThat(logPack, nullValue());
    }

    @Test
    public void findErrorLog() throws NoSuchMethodException {
        // given
        Method method = LogPackFactoryTest.class.getMethod("newInstance");
        InLog inLog = givenInLog();
        OutLog outLog = givenOutLog();
        ErrorLog errorLog = TestErrorLogFactory.newInstance(new Class<?>[]{Exception.class}, new Class<?>[]{Error.class});
        Set<ErrorLog> errorLogs = singleton(errorLog);
        // when
        LogPack logPack = LogPackFactory.newInstance(method, inLog, outLog, errorLogs);
        // then
        Assert.assertThat(logPack.findErrorLog(Throwable.class), nullValue());
        Assert.assertThat(logPack.findErrorLog(RuntimeException.class), is(errorLog));
        Assert.assertThat(logPack.findErrorLog(RuntimeException.class), is(errorLog));
        Assert.assertThat(logPack.findErrorLog(OutOfMemoryError.class), nullValue());
    }

    private InLog givenInLog() {
        Log.in logIn = synthesizeAnnotation(Log.in.class);
        List<ArgLog> argLogs = singletonList(ArgLogFactory.newInstance(synthesizeAnnotation(Log.arg.class), new ToStringPrinter()));
        return InLogFactory.newInstance(logIn, argLogs);
    }

    private OutLog givenOutLog() {
        Log.out logOut = synthesizeAnnotation(Log.out.class);
        return OutLogFactory.newInstance(logOut, new ToStringPrinter());
    }
}
