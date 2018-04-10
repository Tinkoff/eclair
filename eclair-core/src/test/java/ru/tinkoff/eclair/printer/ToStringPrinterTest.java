package ru.tinkoff.eclair.printer;

import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * @author Viacheslav Klapatniuk
 */
public class ToStringPrinterTest {

    private final ToStringPrinter printer = new ToStringPrinter();

    @Test
    public void serializeString() {
        // given
        String string = "string";
        // when
        String result = printer.serialize(string);
        // then
        assertThat(result, is("\"string\""));
    }

    @Test
    public void serializeBytes() {
        // given
        byte[] bytes = new byte[]{1, 2, 3};
        // when
        String result = printer.serialize(bytes);
        // then
        assertThat(result, is("[1, 2, 3]"));
    }

    @Test
    public void serializeChars() {
        // given
        char[] chars = new char[]{'a', 'b', 'c'};
        // when
        String result = printer.serialize(chars);
        // then
        assertThat(result, is("[a, b, c]"));
    }

    @Test
    public void serializeInts() {
        // given
        int[] ints = new int[]{1, 2, 3};
        // when
        String result = printer.serialize(ints);
        // then
        assertThat(result, is("[1, 2, 3]"));
    }

    @Test
    public void serializeBooleans() {
        // given
        boolean[] booleans = new boolean[]{false, true, false};
        // when
        String result = printer.serialize(booleans);
        // then
        assertThat(result, is("[false, true, false]"));
    }

    @Test
    public void serializeShorts() {
        // given
        short[] shorts = new short[]{1, 2, 3};
        // when
        String result = printer.serialize(shorts);
        // then
        assertThat(result, is("[1, 2, 3]"));
    }

    @Test
    public void serializeLongs() {
        // given
        long[] longs = new long[]{1, 2, 3};
        // when
        String result = printer.serialize(longs);
        // then
        assertThat(result, is("[1, 2, 3]"));
    }

    @Test
    public void serializeFloats() {
        // given
        float[] floats = new float[]{1, 2, 3};
        // when
        String result = printer.serialize(floats);
        // then
        assertThat(result, is("[1.0, 2.0, 3.0]"));
    }

    @Test
    public void serializeDoubles() {
        // given
        double[] doubles = new double[]{1, 2, 3};
        // when
        String result = printer.serialize(doubles);
        // then
        assertThat(result, is("[1.0, 2.0, 3.0]"));
    }

    @Test
    public void serializeObjects() {
        // given
        Object[] objects = new Object[]{new TestObject(), new TestObject(), new TestObject()};
        // when
        String result = printer.serialize(objects);
        // then
        assertThat(result, is("[!, !, !]"));
    }

    @Test
    public void serializeObject() {
        // given
        Object object = new TestObject();
        // when
        String result = printer.serialize(object);
        // then
        assertThat(result, is("!"));
    }

    private static class TestObject {

        @Override
        public String toString() {
            return "!";
        }
    }
}
