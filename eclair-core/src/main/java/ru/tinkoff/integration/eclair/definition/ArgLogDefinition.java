package ru.tinkoff.integration.eclair.definition;

import lombok.Getter;
import org.springframework.boot.logging.LogLevel;
import ru.tinkoff.integration.eclair.annotation.Log;
import ru.tinkoff.integration.eclair.core.AnnotationAttribute;
import ru.tinkoff.integration.eclair.format.printer.Printer;

import static java.util.Objects.isNull;

/**
 * @author Viacheslav Klapatniuk
 */
@Getter
public class ArgLogDefinition {

    private final LogLevel ifEnabledLevel;
    private final Printer printer;

    public ArgLogDefinition(Log.arg logArg, Printer printer) {
        if (isNull(logArg)) {
            throw new IllegalArgumentException("'logArg' could not be null");
        }
        if (isNull(printer)) {
            throw new IllegalArgumentException("'printer' could not be null");
        }
        this.ifEnabledLevel = AnnotationAttribute.IF_ENABLED.extract(logArg);
        this.printer = printer;
    }
}
