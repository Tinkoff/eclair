package ru.tinkoff.eclair.format.printer;

/**
 * any.toString -> throws
 *
 * @author Viacheslav Klapatniuk
 */
public class ToStringPrinter extends Printer {

    @Override
    protected String serialize(Object input) {
        if (input instanceof String) {
            return "\"" + input + "\"";
        }
        return input.toString();
    }
}
