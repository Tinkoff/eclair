package ru.tinkoff.eclair.definition;

import lombok.Getter;
import org.springframework.boot.logging.LogLevel;
import ru.tinkoff.eclair.annotation.Log;
import ru.tinkoff.eclair.core.AnnotationAttribute;

import java.util.List;

import static java.util.Collections.unmodifiableList;
import static java.util.Objects.isNull;

/**
 * @author Viacheslav Klapatniuk
 */
@Getter
public class InLog implements LogDefinition {

    private final LogLevel level;
    private final LogLevel ifEnabledLevel;
    private final LogLevel verboseLevel;
    private final List<ArgLog> argLogs;

    private InLog(Log.in logIn, List<ArgLog> argLogs) {
        this.level = AnnotationAttribute.LEVEL.extract(logIn);
        this.ifEnabledLevel = logIn.ifEnabled();
        this.verboseLevel = logIn.verbose();
        this.argLogs = unmodifiableList(argLogs);
    }

    /**
     * @param logIn may be {@code null}
     * @return Instantiated {@link InLog} or {@code null}
     */
    public static InLog newInstance(Log.in logIn, List<ArgLog> argLogs) {
        return isNull(logIn) ? null : new InLog(logIn, argLogs);
    }
}
