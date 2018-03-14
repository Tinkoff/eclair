package ru.tinkoff.integration.eclair.format.printer;

/**
 * @author Viacheslav Klapatniuk
 */
public interface Printer {

    default boolean supports(Class<?> clazz) {
        return true;
    }

    /**
     * @param input never {@code null}
     */
    String print(Object input);
}
