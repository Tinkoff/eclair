package ru.tinkoff.integration.eclair.format.printer;

/**
 * any.toString -> throws
 */
public class ToStringPrinter implements Printer {

    @Override
    public String print(Object input) {
        if (input instanceof String) {
            return "\"" + input + "\"";
        }
        return input.toString();
    }
}
