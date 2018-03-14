package ru.tinkoff.integration.eclair.definition;

import lombok.Getter;
import org.springframework.boot.logging.LogLevel;
import ru.tinkoff.integration.eclair.annotation.Log;
import ru.tinkoff.integration.eclair.annotation.Verbose;
import ru.tinkoff.integration.eclair.core.AnnotationAttribute;
import ru.tinkoff.integration.eclair.format.printer.Printer;

import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;

/**
 * @author Viacheslav Klapatniuk
 */
@Getter
public class OutLogDefinition {

    private final LogLevel level;
    private final LogLevel ifEnabledLevel;
    private final Verbose verbosePolicy;
    private final Printer printer;
    private final List<String> maskExpressions;

    public OutLogDefinition(Log.out logOut, Printer printer) {
        this.level = AnnotationAttribute.LEVEL.extract(logOut);
        this.ifEnabledLevel = logOut.ifEnabled();
        this.verbosePolicy = logOut.verbose();
        this.printer = printer;
        this.maskExpressions = unmodifiableList(asList(logOut.mask()));
    }
}
