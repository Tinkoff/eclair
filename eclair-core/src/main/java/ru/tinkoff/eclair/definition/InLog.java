package ru.tinkoff.eclair.definition;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Singular;
import org.springframework.boot.logging.LogLevel;

import java.util.List;

/**
 * @author Viacheslav Klapatniuk
 */
@Getter
@Builder
public class InLog implements LogDefinition {

    @NonNull
    private LogLevel level;

    @NonNull
    private LogLevel ifEnabledLevel;

    /**
     * TODO: not used?
     */
    @NonNull
    private LogLevel verboseLevel;

    @NonNull
    @Singular
    private List<ArgLog> argLogs;
}
