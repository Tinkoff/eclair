package ru.tinkoff.eclair.printer.processor;

/**
 * @author Viacheslav Klapatniuk
 */
public interface PrinterPreProcessor {

    /**
     * @param input never {@code null}
     * @return never {@code null}
     */
    Object process(Object input);
}
