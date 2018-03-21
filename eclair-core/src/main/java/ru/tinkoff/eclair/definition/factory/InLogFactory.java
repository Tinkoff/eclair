package ru.tinkoff.eclair.definition.factory;

import ru.tinkoff.eclair.annotation.Log;
import ru.tinkoff.eclair.core.AnnotationAttribute;
import ru.tinkoff.eclair.definition.InLog;
import ru.tinkoff.eclair.printer.Printer;

/**
 * @author Viacheslav Klapatniuk
 */
public class InLogFactory {

    public static InLog newInstance(Log.in logIn, Printer printer) {
        return InLog.builder()
                .level(AnnotationAttribute.LEVEL.extract(logIn))
                .ifEnabledLevel(logIn.ifEnabled())
                .verboseLevel(logIn.verbose())
                .printer(printer)
                .build();
    }
}
