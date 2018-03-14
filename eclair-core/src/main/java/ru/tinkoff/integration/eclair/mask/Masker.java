package ru.tinkoff.integration.eclair.mask;

import ru.tinkoff.integration.eclair.format.printer.Printer;

import java.util.List;
import java.util.function.Supplier;

public interface Masker {

    String mask(Object argument, List<String> maskExpressions, Supplier<Printer> printerSupplier);
}
