package ru.tinkoff.eclair.definition;

import lombok.Getter;
import org.springframework.boot.logging.LogLevel;
import ru.tinkoff.eclair.core.AnnotationAttribute;
import ru.tinkoff.eclair.format.printer.Printer;
import ru.tinkoff.eclair.annotation.Log;

/**
 * @author Viacheslav Klapatniuk
 */
@Getter
public class OutLogDefinition implements EventLogDefinition {

    private final LogLevel level;
    private final LogLevel ifEnabledLevel;
    private final LogLevel verboseLevel;
    private final Printer printer;

    public OutLogDefinition(Log.out logOut, Printer printer) {
        this.level = AnnotationAttribute.LEVEL.extract(logOut);
        this.ifEnabledLevel = logOut.ifEnabled();
        this.verboseLevel = logOut.verbose();
        this.printer = printer;
    }
}
