package ru.tinkoff.eclair.definition.factory;

import ru.tinkoff.eclair.annotation.Log;
import ru.tinkoff.eclair.core.AnnotationAttribute;
import ru.tinkoff.eclair.definition.ParameterLog;
import ru.tinkoff.eclair.printer.Printer;

/**
 * @author Viacheslav Klapatniuk
 */
public class ParameterLogFactory {

    public static ParameterLog newInstance(Log log, Printer printer) {
        return ParameterLog.builder()
                .level(AnnotationAttribute.LEVEL.extract(log))
                .ifEnabledLevel(log.ifEnabled())
                .verboseLevel(log.verbose())
                .printer(printer)
                .build();
    }
}
