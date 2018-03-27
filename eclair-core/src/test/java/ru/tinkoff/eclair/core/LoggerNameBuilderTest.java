package ru.tinkoff.eclair.core;

import org.aopalliance.intercept.MethodInvocation;
import org.junit.Test;
import org.springframework.boot.logging.LogLevel;
import ru.tinkoff.eclair.logger.ManualLogger;

import java.lang.reflect.Method;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * TODO: implement
 *
 * @author Viacheslav Klapatniuk
 */
public class LoggerNameBuilderTest {

    private final LoggerNameBuilder loggerNameBuilder = LoggerNameBuilder.getInstance();

    @Test
    public void build() throws NoSuchMethodException {
        // given
        Method method = LoggerNameBuilderTest.class.getMethod("build");
        MethodInvocation invocation = mock(MethodInvocation.class);
        when(invocation.getMethod()).thenReturn(method);
        // when
        String name = loggerNameBuilder.build(invocation);
        // then
        assertThat(name, is("ru.tinkoff.eclair.core.LoggerNameBuilderTest.build"));
    }

    @Test
    public void buildNested() throws NoSuchMethodException {
        // given
        Method method = Nested.class.getMethod("method");
        MethodInvocation invocation = mock(MethodInvocation.class);
        when(invocation.getMethod()).thenReturn(method);
        // when
        String name = loggerNameBuilder.build(invocation);
        // then
        assertThat(name, is("ru.tinkoff.eclair.core.LoggerNameBuilderTest$Nested.method"));
    }

    private static class Nested {

        @SuppressWarnings("unused")
        public void method() {
        }
    }

    @Test
    public void buildByInvoker() {
        // given
        ManualLogger logger = new TestLogger();
        // when
        logger.log(null, null);
    }

    private static class TestLogger implements ManualLogger {

        @Override
        public boolean isLogEnabled(LogLevel level) {
            return false;
        }

        @Override
        public void log(LogLevel level, String format, Object... arguments) {
            // then
            String name = LoggerNameBuilder.getInstance().buildByInvoker();
            assertThat(name, is("ru.tinkoff.eclair.core.LoggerNameBuilderTest.buildByInvoker"));
        }

        @Override
        public void log(LogLevel level, LogLevel ifEnabledLevel, String format, Object... arguments) {
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void buildByInvokerInvalid() {
        // when
        loggerNameBuilder.buildByInvoker();
        // then expected exception
    }
}
