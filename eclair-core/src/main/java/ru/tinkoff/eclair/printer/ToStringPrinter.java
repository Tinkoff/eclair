package ru.tinkoff.eclair.printer;

import java.util.Arrays;

/**
 * @author Viacheslav Klapatniuk
 */
public class ToStringPrinter extends Printer {

    @Override
    protected String serialize(Object input) {
        if (input instanceof String) {
            return "\"" + input + "\"";
        }
        if (input.getClass().isArray()) {
            // in the expected descending order of popularity
            if (input instanceof byte[]) {
                return Arrays.toString((byte[]) input);
            }
            if (input instanceof char[]) {
                return Arrays.toString((char[]) input);
            }
            if (input instanceof int[]) {
                return Arrays.toString((int[]) input);
            }
            if (input instanceof boolean[]) {
                return Arrays.toString((boolean[]) input);
            }
            if (input instanceof short[]) {
                return Arrays.toString((short[]) input);
            }
            if (input instanceof long[]) {
                return Arrays.toString((long[]) input);
            }
            if (input instanceof float[]) {
                return Arrays.toString((float[]) input);
            }
            if (input instanceof double[]) {
                return Arrays.toString((double[]) input);
            }
            return Arrays.toString((Object[]) input);
        }
        return input.toString();
    }
}
