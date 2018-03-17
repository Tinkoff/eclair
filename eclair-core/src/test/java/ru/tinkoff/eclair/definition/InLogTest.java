package ru.tinkoff.eclair.definition;

import org.junit.Test;
import ru.tinkoff.eclair.annotation.Log;
import ru.tinkoff.eclair.printer.ToStringPrinter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.*;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.springframework.boot.logging.LogLevel.TRACE;
import static org.springframework.boot.logging.LogLevel.WARN;
import static org.springframework.core.annotation.AnnotationUtils.synthesizeAnnotation;

/**
 * @author Viacheslav Klapatniuk
 */
public class InLogTest {

    @Test
    public void newInstance() {
        // given
        Log.in logIn = givenLogIn();
        ArgLog argLog = givenArgLog();
        List<ArgLog> argLogs = singletonList(argLog);
        // when
        InLog inLog = InLog.newInstance(logIn, argLogs);
        // then
        assertThat(inLog.getLevel(), is(WARN));
        assertThat(inLog.getIfEnabledLevel(), is(WARN));
        assertThat(inLog.getVerboseLevel(), is(TRACE));
        assertThat(inLog.getArgLogs(), hasSize(1));
        assertThat(inLog.getArgLogs().get(0), is(argLog));
    }

    private Log.in givenLogIn() {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("level", WARN);
        attributes.put("ifEnabled", WARN);
        attributes.put("verbose", TRACE);
        attributes.put("printer", "json");
        return synthesizeAnnotation(attributes, Log.in.class, null);
    }

    private ArgLog givenArgLog() {
        return new ArgLog(synthesizeAnnotation(Log.arg.class), new ToStringPrinter());
    }

    @Test
    public void newInstanceByValue() {
        // given
        Log.in logIn = givenLogInByValue();
        List<ArgLog> argLogs = singletonList(givenArgLog());
        // when
        InLog inLog = InLog.newInstance(logIn, argLogs);
        // then
        assertThat(inLog.getLevel(), is(WARN));
    }

    private Log.in givenLogInByValue() {
        return synthesizeAnnotation(singletonMap("value", WARN), Log.in.class, null);
    }

    @Test
    public void newInstanceNull() {
        // given
        Log.in logIn = null;
        List<ArgLog> argLogs = emptyList();
        // when
        InLog inLog = InLog.newInstance(logIn, argLogs);
        // then
        assertThat(inLog, nullValue());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void newInstanceImmutable() {
        // given
        Log.in logIn = givenLogIn();
        List<ArgLog> argLogs = new ArrayList<>();
        // when
        InLog inLog = InLog.newInstance(logIn, argLogs);
        // then
        inLog.getArgLogs().add(givenArgLog());
    }
}
