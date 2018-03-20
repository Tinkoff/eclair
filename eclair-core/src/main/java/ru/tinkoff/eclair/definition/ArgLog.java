package ru.tinkoff.eclair.definition;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import org.springframework.boot.logging.LogLevel;
import ru.tinkoff.eclair.printer.Printer;

/**
 * @author Viacheslav Klapatniuk
 */
@Getter
@Builder
public class ArgLog {

    @NonNull
    private LogLevel ifEnabledLevel;

    @NonNull
    private Printer printer;
}
