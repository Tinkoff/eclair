package ru.tinkoff.eclair.definition;

import lombok.Getter;
import org.springframework.boot.logging.LogLevel;
import ru.tinkoff.eclair.core.AnnotationAttribute;
import ru.tinkoff.eclair.printer.Printer;
import ru.tinkoff.eclair.annotation.Log;

/**
 * TODO: extract instantiation logic to factory, generate 'builder' methods
 *
 * @author Viacheslav Klapatniuk
 */
@Getter
public class OutLog implements LogDefinition {

    private final LogLevel level;
    private final LogLevel ifEnabledLevel;
    private final LogLevel verboseLevel;
    private final Printer printer;

    public OutLog(Log.out logOut, Printer printer) {
        this.level = AnnotationAttribute.LEVEL.extract(logOut);
        this.ifEnabledLevel = logOut.ifEnabled();
        this.verboseLevel = logOut.verbose();
        this.printer = printer;
    }
}
