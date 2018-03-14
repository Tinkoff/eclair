package ru.tinkoff.integration.eclair.core;

import ru.tinkoff.integration.eclair.format.printer.Printer;
import ru.tinkoff.integration.eclair.format.printer.ToStringPrinter;

import java.util.Map;

import static org.springframework.util.StringUtils.hasText;

/**
 * TODO: add tests
 * TODO: resolve by aliases
 *
 * @author Viacheslav Klapatniuk
 */
public class PrinterResolver {

    private static final Printer defaultPrinter = new ToStringPrinter();

    private final Map<String, Printer> printers;

    public PrinterResolver(Map<String, Printer> printers) {
        this.printers = printers;
    }

    public Printer resolve(String printerName, Class<?> parameterType) {
        if (hasText(printerName)) {
            return printers.getOrDefault(printerName, defaultPrinter);
        }
        return printers.values().stream()
                .filter(item -> item.supports(parameterType))
                .findFirst()
                .orElse(defaultPrinter);
    }

    Printer getDefaultPrinter() {
        return defaultPrinter;
    }
}
