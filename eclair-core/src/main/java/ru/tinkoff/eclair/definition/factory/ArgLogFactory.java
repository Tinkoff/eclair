package ru.tinkoff.eclair.definition.factory;

import ru.tinkoff.eclair.annotation.Log;
import ru.tinkoff.eclair.core.AnnotationAttribute;
import ru.tinkoff.eclair.definition.ArgLog;
import ru.tinkoff.eclair.printer.Printer;

/**
 * @author Viacheslav Klapatniuk
 */
public class ArgLogFactory {

    public static ArgLog newInstance(Log logArg, Printer printer) {
        return ArgLog.builder()
                .level(AnnotationAttribute.LEVEL.extract(logArg))
                .ifEnabledLevel(logArg.ifEnabled())
                .verboseLevel(logArg.verbose())
                .printer(printer)
                .build();
    }
}
