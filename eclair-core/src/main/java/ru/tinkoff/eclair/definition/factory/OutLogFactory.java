package ru.tinkoff.eclair.definition.factory;

import ru.tinkoff.eclair.annotation.Log;
import ru.tinkoff.eclair.core.AnnotationAttribute;
import ru.tinkoff.eclair.definition.OutLog;
import ru.tinkoff.eclair.printer.Printer;

/**
 * @author Viacheslav Klapatniuk
 */
public class OutLogFactory {

    public static OutLog newInstance(Log.out logOut, Printer printer) {
        return OutLog.builder()
                .level(AnnotationAttribute.LEVEL.extract(logOut))
                .ifEnabledLevel(logOut.ifEnabled())
                .verboseLevel(logOut.verbose())
                .printer(printer)
                .build();
    }
}
