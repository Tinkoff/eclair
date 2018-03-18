package ru.tinkoff.eclair.definition;

import lombok.Getter;
import org.springframework.boot.logging.LogLevel;
import ru.tinkoff.eclair.annotation.Log;
import ru.tinkoff.eclair.core.AnnotationAttribute;
import ru.tinkoff.eclair.printer.Printer;

import static java.util.Objects.isNull;

/**
 * TODO: extract instantiation logic to factory, generate 'builder' methods
 *
 * @author Viacheslav Klapatniuk
 */
@Getter
public class ArgLog {

    private final LogLevel ifEnabledLevel;
    private final Printer printer;

    public ArgLog(Log.arg logArg, Printer printer) {
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
