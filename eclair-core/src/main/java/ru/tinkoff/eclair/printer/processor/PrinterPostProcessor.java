package ru.tinkoff.eclair.printer.processor;

/**
 * @author Viacheslav Klapatniuk
 */
public interface PrinterPostProcessor {

    /**
     * @param string never {@code null}
     * @return never {@code null}
     */
    String process(String string);
}
