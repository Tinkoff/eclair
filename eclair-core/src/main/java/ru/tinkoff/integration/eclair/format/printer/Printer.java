package ru.tinkoff.integration.eclair.format.printer;

/**
 * @author Viacheslav Klapatniuk
 */
public abstract class Printer {

    public boolean supports(Class<?> clazz) {
        return true;
    }

    /**
     * @param input never {@code null}
     */
    public String print(Object input) {
        return serialize(input);
    }

    protected abstract String serialize(Object input);
}
