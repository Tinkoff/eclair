package ru.tinkoff.eclair.definition.factory;

import org.junit.Test;
import ru.tinkoff.eclair.annotation.Log;
import ru.tinkoff.eclair.definition.*;
import ru.tinkoff.eclair.printer.ToStringPrinter;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.Arrays.asList;
import static java.util.Collections.*;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.springframework.core.annotation.AnnotationUtils.synthesizeAnnotation;

/**
 * @author Viacheslav Klapatniuk
 */
public class LogPackFactoryTest {

    @Test
    public void newInstance() throws NoSuchMethodException {
        // given
        Method method = LogPackFactoryTest.class.getMethod("newInstance");
        List<String> parameterNames = singletonList("parameterName");
        InLog inLog = givenInLog();
        ArgLog argLog = givenArgLog();
        List<ArgLog> argLogs = singletonList(argLog);
        OutLog outLog = givenOutLog();
        Set<ErrorLog> errorLogs = singleton(TestErrorLogFactory.newInstance(new Class<?>[]{Throwable.class}, new Class<?>[]{}));
        // when
        LogPack logPack = LogPackFactory.newInstance(method, parameterNames, inLog, argLogs, outLog, errorLogs);
        // then
        assertThat(logPack.getMethod(), is(method));
        assertThat(logPack.getParameterNames(), hasSize(1));
        assertThat(logPack.getParameterNames().get(0), is("parameterName"));
        assertThat(logPack.getInLog(), is(inLog));
        assertThat(logPack.getArgLogs(), hasSize(1));
        assertThat(logPack.getArgLogs().get(0), is(argLog));
        assertThat(logPack.getOutLog(), is(outLog));
    }

    @Test
    public void newInstanceNull() throws NoSuchMethodException {
        // given
        Method method = LogPackFactoryTest.class.getMethod("newInstance");
        List<String> parameterNames = emptyList();
        InLog inLog = null;
        List<ArgLog> argLogs = asList(null, null, null);
        OutLog outLog = null;
        Set<ErrorLog> errorLogs = emptySet();
        // when
        LogPack logPack = LogPackFactory.newInstance(method, parameterNames, inLog, argLogs, outLog, errorLogs);
        // then
        assertThat(logPack, nullValue());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void newInstanceArgLogsImmutable() throws NoSuchMethodException {
        // given
        Method method = LogPackFactoryTest.class.getMethod("newInstance");
        List<String> parameterNames = emptyList();
        InLog inLog = givenInLog();
        List<ArgLog> argLogs = new ArrayList<>();
        OutLog outLog = givenOutLog();
        Set<ErrorLog> errorLogs = new HashSet<>();
        // when
        LogPack logPack = LogPackFactory.newInstance(method, parameterNames, inLog, argLogs, outLog, errorLogs);
        logPack.getArgLogs().add(givenArgLog());
        // then expected exception
    }

    @Test(expected = UnsupportedOperationException.class)
    public void newInstanceParameterNamesImmutable() throws NoSuchMethodException {
        // given
        Method method = LogPackFactoryTest.class.getMethod("newInstance");
        List<String> parameterNames = emptyList();
        InLog inLog = givenInLog();
        List<ArgLog> argLogs = new ArrayList<>();
        OutLog outLog = givenOutLog();
        Set<ErrorLog> errorLogs = new HashSet<>();
        // when
        LogPack logPack = LogPackFactory.newInstance(method, parameterNames, inLog, argLogs, outLog, errorLogs);
        logPack.getParameterNames().add("name");
        // then expected exception
    }

    @Test
    public void findErrorLog() throws NoSuchMethodException {
        // given
        Method method = LogPackFactoryTest.class.getMethod("newInstance");
        List<String> parameterNames = emptyList();
        InLog inLog = givenInLog();
        List<ArgLog> argLogs = emptyList();
        OutLog outLog = givenOutLog();
        ErrorLog errorLog = TestErrorLogFactory.newInstance(new Class<?>[]{Exception.class}, new Class<?>[]{Error.class});
        Set<ErrorLog> errorLogs = singleton(errorLog);
        // when
        LogPack logPack = LogPackFactory.newInstance(method, parameterNames, inLog, argLogs, outLog, errorLogs);
        // then
        assertThat(logPack.findErrorLog(Throwable.class), nullValue());
        assertThat(logPack.findErrorLog(RuntimeException.class), is(errorLog));
        assertThat(logPack.findErrorLog(RuntimeException.class), is(errorLog));
        assertThat(logPack.findErrorLog(OutOfMemoryError.class), nullValue());
    }

    private InLog givenInLog() {
        Log.in logIn = synthesizeAnnotation(Log.in.class);
        return InLogFactory.newInstance(logIn, new ToStringPrinter());
    }

    private ArgLog givenArgLog() {
        return ArgLogFactory.newInstance(synthesizeAnnotation(Log.class), new ToStringPrinter());
    }

    private OutLog givenOutLog() {
        Log.out logOut = synthesizeAnnotation(Log.out.class);
        return OutLogFactory.newInstance(logOut, new ToStringPrinter());
    }
}
