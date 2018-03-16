package ru.tinkoff.eclair.core;

import org.junit.Test;

/**
 * TODO: implement
 *
 * @author Viacheslav Klapatniuk
 */
public class LoggerNameBuilderTest {

    private final LoggerNameBuilder loggerNameBuilder = LoggerNameBuilder.getInstance();

    @Test
    public void buildOuter() throws NoSuchMethodException {
        /*// given
        Method method = Outer.class.getMethod("method");
        // when
        String name = loggerNameBuilder.build(method);
        // then
        assertThat(name, is("ru.tinkoff.eclair.core.Outer.method"));*/
    }

    @Test
    public void buildNested() throws NoSuchMethodException {
        /*// given
        Method method = Nested.class.getMethod("method");
        // when
        String name = loggerNameBuilder.build(method);
        // then
        assertThat(name, is("ru.tinkoff.eclair.core.LoggerNameBuilderTest$Nested.method"));*/
    }

    private static class Nested {

        @SuppressWarnings("unused")
        public void method() {
        }
    }

    @Test
    public void buildByStacktrace() {
        /*// given
        StackTraceElement stackTraceElement = new StackTraceElement(
                LoggerNameBuilderTest.class.getName(),
                "buildByStacktrace",
                null,
                45
        );
        // when
        String name = loggerNameBuilder.build(stackTraceElement);
        // then
        assertThat(name, is("ru.tinkoff.eclair.core.LoggerNameBuilderTest.buildByStacktrace"));*/
    }
}

class Outer {

    @SuppressWarnings("unused")
    public void method() {
    }
}
